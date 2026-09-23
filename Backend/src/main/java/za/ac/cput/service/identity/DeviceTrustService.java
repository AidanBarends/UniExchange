/*
 DeviceTrustService.java

 Decides whether a browser has already proved itself, so a sign-in from it can
 skip the emailed OTP.

 Design notes:
  - The token is never stored. Only SHA-256 of it goes into
    TrustedDevice.tokenHash, and the browser keeps the plaintext.
  - SHA-256 rather than BCrypt, unlike OtpService. BCrypt salts every row, so a
    stored BCrypt hash cannot be looked up - checking a token would mean loading
    every device and matching one by one. The reason BCrypt exists is to slow
    down guessing a low-entropy secret; this token is 256 bits from SecureRandom,
    so there is nothing to guess and a plain digest with a unique index is both
    the correct and the faster construction.
  - A device token is NEVER a way in on its own. The password is checked first,
    every time; this only ever skips the second factor.
  - The token is bound to a user. Presenting another account's token proves
    nothing about this one, so the userId is compared explicitly.
  - Only /verify-otp mints these, so a device can only become trusted after a
    real code was entered.

 Follows the OtpService pattern (a focused @Service talking to its repository
 directly) rather than the IService CRUD pattern: there is no CRUD controller
 for trusted devices, and there should not be one.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 05 September 2026
*/

package za.ac.cput.service.identity;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import za.ac.cput.domain.identity.TrustedDevice;
import za.ac.cput.factory.identity.TrustedDeviceFactory;
import za.ac.cput.repository.identity.TrustedDeviceRepository;
import za.ac.cput.util.Helper;

@Service
public class DeviceTrustService {

    /** 256 bits, so the token cannot be guessed and needs no work factor. */
    private static final int TOKEN_BYTES = 32;

    private final TrustedDeviceRepository repository;
    private final SecureRandom random = new SecureRandom();

    private final long rememberedDays;
    private final long sessionHours;

    public DeviceTrustService(TrustedDeviceRepository repository,
                              @Value("${app.trusted-device.remembered-days:30}") long rememberedDays,
                              @Value("${app.trusted-device.session-hours:12}") long sessionHours) {
        this.repository = repository;
        this.rememberedDays = rememberedDays;
        this.sessionHours = sessionHours;
    }

    /**
     * Trusts a device and returns the token to hand back to the browser.
     *
     * @param persistent true when "Remember me" was ticked - a longer lifetime
     *                   that slides forward on each use. False gives a short
     *                   hard-capped one, matching a browser session.
     * @return the plaintext token - the ONLY time it exists in readable form.
     */
    @Transactional
    public String issue(long userId, String label, boolean persistent) {
        byte[] bytes = new byte[TOKEN_BYTES];
        this.random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        this.repository.save(TrustedDeviceFactory.createTrustedDevice(
                userId,
                sha256Hex(token),
                label,
                persistent,
                expiryFor(persistent)));

        return token;
    }

    /**
     * Whether this user may skip the OTP on the device holding this token.
     * Records the use, and slides the window forward for remembered devices.
     */
    @Transactional
    public boolean isTrusted(long userId, String token) {
        TrustedDevice device = findLive(userId, token);
        if (device == null) {
            return false;
        }

        // Remembered devices get a sliding window so a regular user is never
        // logged out mid-term; session ones keep their original hard cap.
        this.repository.save(TrustedDeviceFactory.touch(
                device,
                device.isPersistent() ? expiryFor(true) : device.getExpiresAt()));

        return true;
    }

    /**
     * Brings an already-trusted device into line with the "Remember me" answer
     * just given, since a student can tick or untick it on any sign-in.
     *
     * Without this the two halves drift apart: ticking the box on a device that
     * was trusted for this browser session only would move the session to
     * localStorage while its device token stayed in sessionStorage, so the trust
     * would quietly die at the next browser close despite the box being ticked.
     *
     * @return a replacement token to hand back to the browser, or null when the
     *         answer has not changed and the existing token still stands.
     */
    @Transactional
    public String realign(long userId, String token, String label, boolean persistent) {
        TrustedDevice device = findLive(userId, token);
        if (device == null || device.isPersistent() == persistent) {
            return null;
        }

        // Retire rather than edit, so the old token stops working the moment the
        // new one is handed out - the lifetime is part of what it certifies.
        this.repository.save(TrustedDeviceFactory.revoke(device));
        return issue(userId, label, persistent);
    }

    /** The usable device for this token and user, or null. */
    private TrustedDevice findLive(long userId, String token) {
        if (Helper.isNullOrEmpty(token) || !Helper.isValidId(userId)) {
            return null;
        }

        TrustedDevice device = this.repository.findByTokenHash(sha256Hex(token)).orElse(null);
        if (device == null) {
            return null;
        }

        // A token belonging to a different account proves nothing about this one.
        if (device.getUserId() != userId) {
            return null;
        }

        if (device.getRevokedAt() != null) {
            return null;
        }

        if (device.getExpiresAt() == null || device.getExpiresAt().isBefore(LocalDateTime.now())) {
            return null;
        }

        return device;
    }

    /**
     * Retires every device for a user, so the next sign-in anywhere needs a code
     * again. The hook a password change or a "sign out everywhere" button uses.
     */
    @Transactional
    public void revokeAll(long userId) {
        this.repository.findByUserId(userId).stream()
                .filter(device -> device.getRevokedAt() == null)
                .map(TrustedDeviceFactory::revoke)
                .forEach(this.repository::save);
    }

    public long getRememberedDays() {
        return this.rememberedDays;
    }

    public long getSessionHours() {
        return this.sessionHours;
    }

    private LocalDateTime expiryFor(boolean persistent) {
        LocalDateTime now = LocalDateTime.now();
        return persistent ? now.plusDays(this.rememberedDays) : now.plusHours(this.sessionHours);
    }

    /** Lowercase hex SHA-256, matching the 64-character token_hash column. */
    private static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        }
        catch (NoSuchAlgorithmException ex) {
            // SHA-256 is required of every JVM, so this cannot happen.
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }

}
