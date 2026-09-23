/*
 OtpServiceTest.java

 Covers the parts of the verification gate that are easy to get wrong: expiry,
 the brute-force cap, single-use, and the fact that a code is never stored in
 readable form.

 @DataJpaTest so it exercises the real repository against H2, with the two
 collaborators OtpService needs supplied directly.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.service.identity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import za.ac.cput.domain.enums.VerificationType;
import za.ac.cput.domain.identity.Verification;
import za.ac.cput.factory.identity.VerificationFactory;
import za.ac.cput.repository.identity.VerificationRepository;

@DataJpaTest
class OtpServiceTest {

    private static final long USER_ID = 42L;

    @Autowired
    private VerificationRepository repository;

    private PasswordEncoder passwordEncoder;
    private OtpService otpService;

    @BeforeEach
    void setUp() {
        // Strength 4 keeps BCrypt fast enough for a test that hashes repeatedly.
        this.passwordEncoder = new BCryptPasswordEncoder(4);
        this.otpService = new OtpService(this.repository, this.passwordEncoder, 6, 10, 5, 60);
    }

    @Test
    void issuesASixDigitCode() {
        String code = this.otpService.issue(USER_ID);

        assertNotNull(code);
        assertTrue(code.matches("\\d{6}"), "expected 6 digits, got " + code);
    }

    @Test
    void neverStoresTheCodeInPlaintext() {
        String code = this.otpService.issue(USER_ID);
        Verification stored = this.repository.findAll().getFirst();

        assertNotEquals(code, stored.getToken());
        assertTrue(this.passwordEncoder.matches(code, stored.getToken()),
                "stored value should be a hash of the code");
    }

    @Test
    void acceptsTheCorrectCode() {
        String code = this.otpService.issue(USER_ID);

        assertEquals(OtpService.Result.OK, this.otpService.check(USER_ID, code));
    }

    @Test
    void acceptsACodeWithSurroundingWhitespace() {
        String code = this.otpService.issue(USER_ID);

        assertEquals(OtpService.Result.OK, this.otpService.check(USER_ID, "  " + code + " "));
    }

    @Test
    void rejectsTheWrongCodeAndCountsTheAttempt() {
        String code = this.otpService.issue(USER_ID);
        String wrong = code.equals("000000") ? "111111" : "000000";

        assertEquals(OtpService.Result.MISMATCH, this.otpService.check(USER_ID, wrong));
        assertEquals(1, this.repository.findAll().getFirst().getAttempts());
    }

    @Test
    void locksOutAfterTooManyAttempts() {
        String code = this.otpService.issue(USER_ID);
        String wrong = code.equals("000000") ? "111111" : "000000";

        for (int i = 0; i < 5; i++) {
            assertEquals(OtpService.Result.MISMATCH, this.otpService.check(USER_ID, wrong));
        }

        // The cap holds even when the caller finally supplies the right code,
        // which is the whole point - otherwise 6 digits is brute-forceable.
        assertEquals(OtpService.Result.TOO_MANY_ATTEMPTS, this.otpService.check(USER_ID, wrong));
        assertEquals(OtpService.Result.TOO_MANY_ATTEMPTS, this.otpService.check(USER_ID, code));
    }

    @Test
    void rejectsAnExpiredCode() {
        this.repository.save(VerificationFactory.createVerification(
                USER_ID,
                VerificationType.EMAIL,
                this.passwordEncoder.encode("123456"),
                LocalDateTime.now().minusMinutes(1)));

        assertEquals(OtpService.Result.EXPIRED, this.otpService.check(USER_ID, "123456"));
    }

    @Test
    void reportsWhenThereIsNoPendingCode() {
        assertEquals(OtpService.Result.NO_PENDING_CODE, this.otpService.check(USER_ID, "123456"));
    }

    @Test
    void aCodeCannotBeUsedTwice() {
        String code = this.otpService.issue(USER_ID);

        assertEquals(OtpService.Result.OK, this.otpService.check(USER_ID, code));
        assertEquals(OtpService.Result.NO_PENDING_CODE, this.otpService.check(USER_ID, code));
    }

    @Test
    void reissuingInvalidatesThePreviousCode() {
        String first = this.otpService.issue(USER_ID);
        String second = this.otpService.issue(USER_ID);

        assertEquals(1, this.repository.findAll().size(), "the old code should be gone");
        assertEquals(OtpService.Result.MISMATCH, this.otpService.check(USER_ID, first));
        // The attempt above counted, but the new code still works.
        assertEquals(OtpService.Result.OK, this.otpService.check(USER_ID, second));
    }

    @Test
    void codesAreScopedToTheirOwnUser() {
        String code = this.otpService.issue(USER_ID);

        assertEquals(OtpService.Result.NO_PENDING_CODE, this.otpService.check(USER_ID + 1, code));
    }

    @Test
    void reportsTheResendCooldown() {
        assertEquals(0L, this.otpService.resendCooldownRemaining(USER_ID),
                "no code issued yet, so a resend is allowed");

        this.otpService.issue(USER_ID);
        long remaining = this.otpService.resendCooldownRemaining(USER_ID);

        assertTrue(remaining > 0 && remaining <= 60, "expected a cooldown, got " + remaining);
    }

}
