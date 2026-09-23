/*
 VerificationFactory.java

 Factory for Verification. All construction goes through here so that every
 Verification is validated with Helper before it exists - the entity itself
 exposes only a Builder and a protected JPA constructor.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.factory.identity;

import java.time.LocalDateTime;

import za.ac.cput.domain.enums.VerificationType;
import za.ac.cput.domain.identity.Verification;
import za.ac.cput.util.Helper;

public class VerificationFactory {

    // Prevent instantiation - factory class
    private VerificationFactory() {}

    public static Verification createVerification(long userId, VerificationType verificationType,
                                                  String token, LocalDateTime expiresAt) {
        if (!Helper.isValidId(userId)) {
            throw new IllegalArgumentException("Verification: userId must be a positive id");
        }

        if (!Helper.isValidObject(verificationType)) {
            throw new IllegalArgumentException("Verification: verificationType is required");
        }

        if (Helper.isNullOrEmpty(token)) {
            throw new IllegalArgumentException("Verification: token is required");
        }

        if (!Helper.isValidObject(expiresAt)) {
            throw new IllegalArgumentException("Verification: expiresAt is required");
        }

        LocalDateTime now = LocalDateTime.now();

        return new Verification.Builder()
                .setUserId(userId)
                .setVerificationType(verificationType)
                .setToken(token)
                .setExpiresAt(expiresAt)
                .setCreatedAt(now)
                .build();
    }

    public static Verification updateVerification(Verification existing, long userId,
                                                  VerificationType verificationType, String token,
                                                  LocalDateTime expiresAt) {
        if (!Helper.isValidObject(existing)) {
            throw new IllegalArgumentException("Verification: existing record is required for an update");
        }

        if (!Helper.isValidId(userId)) {
            throw new IllegalArgumentException("Verification: userId must be a positive id");
        }

        if (!Helper.isValidObject(verificationType)) {
            throw new IllegalArgumentException("Verification: verificationType is required");
        }

        if (Helper.isNullOrEmpty(token)) {
            throw new IllegalArgumentException("Verification: token is required");
        }

        if (!Helper.isValidObject(expiresAt)) {
            throw new IllegalArgumentException("Verification: expiresAt is required");
        }

        return new Verification.Builder()
                .copy(existing)
                .setUserId(userId)
                .setVerificationType(verificationType)
                .setToken(token)
                .setExpiresAt(expiresAt)
                .build();
    }

}
