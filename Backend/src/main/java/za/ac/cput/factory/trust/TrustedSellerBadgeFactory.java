/*
 TrustedSellerBadgeFactory.java

 Factory for TrustedSellerBadge. All construction goes through here so that every
 TrustedSellerBadge is validated with Helper before it exists - the entity itself
 exposes only a Builder and a protected JPA constructor.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.factory.trust;

import java.time.LocalDateTime;

import za.ac.cput.domain.trust.TrustedSellerBadge;
import za.ac.cput.util.Helper;

public class TrustedSellerBadgeFactory {

    // Prevent instantiation - factory class
    private TrustedSellerBadgeFactory() {}

    public static TrustedSellerBadge createTrustedSellerBadge(long userId) {
        if (!Helper.isValidId(userId)) {
            throw new IllegalArgumentException("TrustedSellerBadge: userId must be a positive id");
        }

        LocalDateTime now = LocalDateTime.now();

        return new TrustedSellerBadge.Builder()
                .setUserId(userId)
                .setEarnedAt(now)
                .build();
    }

    public static TrustedSellerBadge updateTrustedSellerBadge(TrustedSellerBadge existing, long userId) {
        if (!Helper.isValidObject(existing)) {
            throw new IllegalArgumentException("TrustedSellerBadge: existing record is required for an update");
        }

        if (!Helper.isValidId(userId)) {
            throw new IllegalArgumentException("TrustedSellerBadge: userId must be a positive id");
        }

        return new TrustedSellerBadge.Builder()
                .copy(existing)
                .setUserId(userId)
                .build();
    }

}
