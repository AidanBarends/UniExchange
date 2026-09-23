/*
 ListingFactory.java

 Factory for Listing. All construction goes through here so that every
 Listing is validated with Helper before it exists - the entity itself
 exposes only a Builder and a protected JPA constructor.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.factory.marketplace;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import za.ac.cput.domain.enums.ListingStatus;
import za.ac.cput.domain.marketplace.Listing;
import za.ac.cput.util.Helper;

public class ListingFactory {

    // Prevent instantiation - factory class
    private ListingFactory() {}

    public static Listing createListing(long sellerId, long categoryId, long campusId, String title,
                                        String description, BigDecimal price, ListingStatus status) {
        if (!Helper.isValidId(sellerId)) {
            throw new IllegalArgumentException("Listing: sellerId must be a positive id");
        }

        if (!Helper.isValidId(categoryId)) {
            throw new IllegalArgumentException("Listing: categoryId must be a positive id");
        }

        if (!Helper.isValidId(campusId)) {
            throw new IllegalArgumentException("Listing: campusId must be a positive id");
        }

        if (Helper.isNullOrEmpty(title)) {
            throw new IllegalArgumentException("Listing: title is required");
        }

        if (!Helper.isValidBigDecimal(price)) {
            throw new IllegalArgumentException("Listing: price must be a non-negative amount");
        }

        if (!Helper.isValidObject(status)) {
            throw new IllegalArgumentException("Listing: status is required");
        }

        LocalDateTime now = LocalDateTime.now();

        return new Listing.Builder()
                .setSellerId(sellerId)
                .setCategoryId(categoryId)
                .setCampusId(campusId)
                .setTitle(title)
                .setDescription(description)
                .setPrice(price)
                .setStatus(status)
                .setCreatedAt(now)
                .setUpdatedAt(now)
                .build();
    }

    public static Listing updateListing(Listing existing, long sellerId, long categoryId, long campusId,
                                        String title, String description, BigDecimal price,
                                        ListingStatus status) {
        if (!Helper.isValidObject(existing)) {
            throw new IllegalArgumentException("Listing: existing record is required for an update");
        }

        if (!Helper.isValidId(sellerId)) {
            throw new IllegalArgumentException("Listing: sellerId must be a positive id");
        }

        if (!Helper.isValidId(categoryId)) {
            throw new IllegalArgumentException("Listing: categoryId must be a positive id");
        }

        if (!Helper.isValidId(campusId)) {
            throw new IllegalArgumentException("Listing: campusId must be a positive id");
        }

        if (Helper.isNullOrEmpty(title)) {
            throw new IllegalArgumentException("Listing: title is required");
        }

        if (!Helper.isValidBigDecimal(price)) {
            throw new IllegalArgumentException("Listing: price must be a non-negative amount");
        }

        if (!Helper.isValidObject(status)) {
            throw new IllegalArgumentException("Listing: status is required");
        }

        return new Listing.Builder()
                .copy(existing)
                .setSellerId(sellerId)
                .setCategoryId(categoryId)
                .setCampusId(campusId)
                .setTitle(title)
                .setDescription(description)
                .setPrice(price)
                .setStatus(status)
                .setUpdatedAt(LocalDateTime.now())
                .build();
    }

}
