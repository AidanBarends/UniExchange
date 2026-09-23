/*
 TrustedDeviceFactory.java

 Factory for TrustedDevice. All construction goes through here so that every
 TrustedDevice is validated with Helper before it exists - the entity itself
 exposes only a Builder and a protected JPA constructor.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 05 September 2026
*/

package za.ac.cput.factory.identity;

import java.time.LocalDateTime;

import za.ac.cput.domain.identity.TrustedDevice;
import za.ac.cput.util.Helper;

public class TrustedDeviceFactory {

    /** Column width of trusted_device.label. Longer User-Agent strings are cut. */
    public static final int MAX_LABEL_LENGTH = 255;

    // Prevent instantiation - factory class
    private TrustedDeviceFactory() {}

    public static TrustedDevice createTrustedDevice(long userId, String tokenHash, String label,
                                                    boolean persistent, LocalDateTime expiresAt) {
        if (!Helper.isValidId(userId)) {
            throw new IllegalArgumentException("TrustedDevice: userId must be a positive id");
        }

        if (Helper.isNullOrEmpty(tokenHash)) {
            throw new IllegalArgumentException("TrustedDevice: tokenHash is required");
        }

        if (!Helper.isValidObject(expiresAt)) {
            throw new IllegalArgumentException("TrustedDevice: expiresAt is required");
        }

        LocalDateTime now = LocalDateTime.now();

        return new TrustedDevice.Builder()
                .setUserId(userId)
                .setTokenHash(tokenHash)
                .setLabel(truncateLabel(label))
                .setPersistent(persistent)
                .setExpiresAt(expiresAt)
                .setLastUsedAt(now)
                .setCreatedAt(now)
                .build();
    }

    /** Records a successful use, optionally sliding the expiry window forward. */
    public static TrustedDevice touch(TrustedDevice existing, LocalDateTime expiresAt) {
        if (!Helper.isValidObject(existing)) {
            throw new IllegalArgumentException("TrustedDevice: existing record is required for an update");
        }

        if (!Helper.isValidObject(expiresAt)) {
            throw new IllegalArgumentException("TrustedDevice: expiresAt is required");
        }

        return new TrustedDevice.Builder()
                .copy(existing)
                .setExpiresAt(expiresAt)
                .setLastUsedAt(LocalDateTime.now())
                .build();
    }

    /** Retires a device so it can never skip an OTP again. */
    public static TrustedDevice revoke(TrustedDevice existing) {
        if (!Helper.isValidObject(existing)) {
            throw new IllegalArgumentException("TrustedDevice: existing record is required for an update");
        }

        return new TrustedDevice.Builder()
                .copy(existing)
                .setRevokedAt(LocalDateTime.now())
                .build();
    }

    /*
     A User-Agent has no length limit, and MySQL would reject anything past the
     column width rather than trim it. Cut it here so a long or spoofed header
     can never fail an otherwise valid sign-in.
    */
    private static String truncateLabel(String label) {
        if (Helper.isNullOrEmpty(label)) {
            return null;
        }
        String trimmed = label.trim();
        return trimmed.length() <= MAX_LABEL_LENGTH ? trimmed : trimmed.substring(0, MAX_LABEL_LENGTH);
    }

}
