/*
 DeviceTrustServiceTest.java

 Covers the parts of "Remember me" that would silently weaken the second factor
 if they were wrong: that a token is never stored readably, that it is bound to
 one account, that expiry and revocation are honoured, and that only remembered
 devices get a sliding window.

 @DataJpaTest so it exercises the real repository against H2, with the config
 values DeviceTrustService needs supplied directly.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 05 September 2026
*/

package za.ac.cput.service.identity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import za.ac.cput.domain.identity.TrustedDevice;
import za.ac.cput.factory.identity.TrustedDeviceFactory;
import za.ac.cput.repository.identity.TrustedDeviceRepository;

@DataJpaTest
class DeviceTrustServiceTest {

    private static final long USER_ID = 42L;
    private static final long OTHER_USER_ID = 43L;
    private static final String AGENT = "Mozilla/5.0 (Macintosh)";

    private static final long REMEMBERED_DAYS = 30;
    private static final long SESSION_HOURS = 12;

    @Autowired
    private TrustedDeviceRepository repository;

    private DeviceTrustService service;

    @BeforeEach
    void setUp() {
        this.service = new DeviceTrustService(this.repository, REMEMBERED_DAYS, SESSION_HOURS);
    }

    @Test
    void issuesAnOpaqueToken() {
        String token = this.service.issue(USER_ID, AGENT, true);

        assertNotNull(token);
        // 32 bytes, Base64url without padding.
        assertEquals(43, token.length(), "expected a 256-bit token, got " + token);
        assertTrue(token.matches("[A-Za-z0-9_-]+"), "expected Base64url, got " + token);
    }

    @Test
    void issuesADifferentTokenEveryTime() {
        assertNotEquals(this.service.issue(USER_ID, AGENT, true),
                this.service.issue(USER_ID, AGENT, true));
    }

    @Test
    void neverStoresTheTokenInPlaintext() {
        String token = this.service.issue(USER_ID, AGENT, true);
        TrustedDevice stored = this.repository.findAll().getFirst();

        assertNotEquals(token, stored.getTokenHash());
        assertEquals(64, stored.getTokenHash().length(), "expected hex SHA-256");
        assertTrue(stored.getTokenHash().matches("[0-9a-f]{64}"));
    }

    @Test
    void keepsTheTokenOutOfToString() {
        this.service.issue(USER_ID, AGENT, true);
        TrustedDevice stored = this.repository.findAll().getFirst();

        // toString ends up in logs, so the credential must not be in it.
        assertFalse(stored.toString().contains(stored.getTokenHash()));
    }

    @Test
    void trustsADeviceItJustIssued() {
        String token = this.service.issue(USER_ID, AGENT, true);

        assertTrue(this.service.isTrusted(USER_ID, token));
    }

    @Test
    void doesNotTrustAnUnknownToken() {
        this.service.issue(USER_ID, AGENT, true);

        assertFalse(this.service.isTrusted(USER_ID, "not-a-real-token"));
    }

    @Test
    void doesNotTrustAMissingToken() {
        assertFalse(this.service.isTrusted(USER_ID, null));
        assertFalse(this.service.isTrusted(USER_ID, ""));
        assertFalse(this.service.isTrusted(USER_ID, "   "));
    }

    @Test
    void doesNotLetOneAccountUseAnothersDevice() {
        String token = this.service.issue(USER_ID, AGENT, true);

        // The whole point of binding the token to a user: holding someone else's
        // trusted-device token must not skip your own second factor.
        assertFalse(this.service.isTrusted(OTHER_USER_ID, token));
    }

    @Test
    void doesNotTrustAnExpiredDevice() {
        String token = "expired-token";
        this.repository.save(TrustedDeviceFactory.createTrustedDevice(
                USER_ID, sha256Hex(token), AGENT, true, LocalDateTime.now().minusMinutes(1)));

        assertFalse(this.service.isTrusted(USER_ID, token));
    }

    @Test
    void doesNotTrustARevokedDevice() {
        String token = this.service.issue(USER_ID, AGENT, true);
        this.repository.save(TrustedDeviceFactory.revoke(this.repository.findAll().getFirst()));

        assertFalse(this.service.isTrusted(USER_ID, token));
    }

    @Test
    void revokeAllStopsEveryDeviceForThatUserOnly() {
        String mine = this.service.issue(USER_ID, AGENT, true);
        String alsoMine = this.service.issue(USER_ID, AGENT, true);
        String theirs = this.service.issue(OTHER_USER_ID, AGENT, true);

        this.service.revokeAll(USER_ID);

        assertFalse(this.service.isTrusted(USER_ID, mine));
        assertFalse(this.service.isTrusted(USER_ID, alsoMine));
        assertTrue(this.service.isTrusted(OTHER_USER_ID, theirs), "another user's device is untouched");
    }

    @Test
    void aRememberedDeviceGetsTheLongLifetime() {
        this.service.issue(USER_ID, AGENT, true);
        TrustedDevice stored = this.repository.findAll().getFirst();

        assertTrue(stored.isPersistent());
        assertTrue(stored.getExpiresAt().isAfter(LocalDateTime.now().plusDays(REMEMBERED_DAYS - 1)),
                "expected roughly " + REMEMBERED_DAYS + " days, got " + stored.getExpiresAt());
    }

    @Test
    void anUntickedDeviceGetsTheShortLifetime() {
        this.service.issue(USER_ID, AGENT, false);
        TrustedDevice stored = this.repository.findAll().getFirst();

        assertFalse(stored.isPersistent());
        assertTrue(stored.getExpiresAt().isBefore(LocalDateTime.now().plusHours(SESSION_HOURS + 1)),
                "expected roughly " + SESSION_HOURS + " hours, got " + stored.getExpiresAt());
    }

    @Test
    void usingARememberedDeviceSlidesItsExpiryForward() {
        String token = this.service.issue(USER_ID, AGENT, true);

        // Backdate it so a sliding window is visibly different from the original.
        TrustedDevice aged = this.repository.save(TrustedDeviceFactory.touch(
                this.repository.findAll().getFirst(), LocalDateTime.now().plusDays(1)));
        LocalDateTime before = aged.getExpiresAt();

        assertTrue(this.service.isTrusted(USER_ID, token));

        assertTrue(this.repository.findAll().getFirst().getExpiresAt().isAfter(before),
                "a remembered device should not expire while it is in regular use");
    }

    @Test
    void usingAnUntickedDeviceDoesNotExtendIt() {
        String token = this.service.issue(USER_ID, AGENT, false);
        LocalDateTime before = this.repository.findAll().getFirst().getExpiresAt();

        assertTrue(this.service.isTrusted(USER_ID, token));

        // The unticked case is a hard cap, so trust cannot creep indefinitely.
        assertEquals(before, this.repository.findAll().getFirst().getExpiresAt());
    }

    @Test
    void realignUpgradesASessionDeviceWhenRememberMeIsTicked() {
        String token = this.service.issue(USER_ID, AGENT, false);

        String replacement = this.service.realign(USER_ID, token, AGENT, true);

        assertNotNull(replacement, "ticking Remember me must hand back a longer-lived token");
        assertFalse(this.service.isTrusted(USER_ID, token), "the old short-lived token is retired");
        assertTrue(this.service.isTrusted(USER_ID, replacement));

        TrustedDevice upgraded = this.repository.findByTokenHash(sha256Hex(replacement)).orElseThrow();
        assertTrue(upgraded.isPersistent());
        assertTrue(upgraded.getExpiresAt().isAfter(LocalDateTime.now().plusDays(REMEMBERED_DAYS - 1)));
    }

    @Test
    void realignDowngradesARememberedDeviceWhenTheBoxIsUnticked() {
        String token = this.service.issue(USER_ID, AGENT, true);

        String replacement = this.service.realign(USER_ID, token, AGENT, false);

        assertNotNull(replacement);
        assertFalse(this.service.isTrusted(USER_ID, token));

        TrustedDevice downgraded = this.repository.findByTokenHash(sha256Hex(replacement)).orElseThrow();
        assertFalse(downgraded.isPersistent());
        assertTrue(downgraded.getExpiresAt().isBefore(LocalDateTime.now().plusHours(SESSION_HOURS + 1)));
    }

    @Test
    void realignChangesNothingWhenTheAnswerIsTheSame() {
        String token = this.service.issue(USER_ID, AGENT, true);

        assertNull(this.service.realign(USER_ID, token, AGENT, true),
            "an unchanged answer must not churn the token the browser is holding");
        assertTrue(this.service.isTrusted(USER_ID, token));
    }

    @Test
    void realignIgnoresADeviceThatIsNotTrusted() {
        assertNull(this.service.realign(USER_ID, "not-a-real-token", AGENT, true));

        String theirs = this.service.issue(OTHER_USER_ID, AGENT, false);
        assertNull(this.service.realign(USER_ID, theirs, AGENT, true),
            "one account must not be able to re-scope another account's device");
    }

    @Test
    void recordsWhenADeviceWasLastUsed() {
        String token = this.service.issue(USER_ID, AGENT, true);
        this.repository.save(new TrustedDevice.Builder()
                .copy(this.repository.findAll().getFirst())
                .setLastUsedAt(LocalDateTime.now().minusDays(3))
                .build());

        assertTrue(this.service.isTrusted(USER_ID, token));

        assertTrue(this.repository.findAll().getFirst().getLastUsedAt()
                        .isAfter(LocalDateTime.now().minusMinutes(1)));
    }

    @Test
    void truncatesAnOverlongUserAgent() {
        this.service.issue(USER_ID, "x".repeat(500), true);

        // MySQL would reject the insert rather than trim, so a spoofed or simply
        // long header must never fail an otherwise valid sign-in.
        assertEquals(TrustedDeviceFactory.MAX_LABEL_LENGTH,
                this.repository.findAll().getFirst().getLabel().length());
    }

    @Test
    void toleratesAMissingUserAgent() {
        String token = this.service.issue(USER_ID, null, true);

        assertTrue(this.service.isTrusted(USER_ID, token));
    }

    /** Mirrors the digest DeviceTrustService uses, so a row can be planted directly. */
    private static String sha256Hex(String value) {
        try {
            return java.util.HexFormat.of().formatHex(java.security.MessageDigest
                    .getInstance("SHA-256")
                    .digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        }
        catch (java.security.NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

}
