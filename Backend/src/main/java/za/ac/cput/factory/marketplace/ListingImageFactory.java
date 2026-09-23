/*
 ListingImageFactory.java

 Factory for ListingImage. All construction goes through here so that every
 ListingImage is validated with Helper before it exists - the entity itself
 exposes only a Builder and a protected JPA constructor.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.factory.marketplace;

import za.ac.cput.domain.marketplace.ListingImage;
import za.ac.cput.util.Helper;

public class ListingImageFactory {

    // Prevent instantiation - factory class
    private ListingImageFactory() {}

    public static ListingImage createListingImage(long listingId, String imageUrl, int position,
                                                  boolean isPrimary) {
        if (!Helper.isValidId(listingId)) {
            throw new IllegalArgumentException("ListingImage: listingId must be a positive id");
        }

        if (!Helper.isValidUrl(imageUrl)) {
            throw new IllegalArgumentException("ListingImage: imageUrl must be a valid URL");
        }

        if (position < 0) {
            throw new IllegalArgumentException("ListingImage: position cannot be negative");
        }

        return new ListingImage.Builder()
                .setListingId(listingId)
                .setImageUrl(imageUrl)
                .setPosition(position)
                .setPrimary(isPrimary)
                .build();
    }

    public static ListingImage updateListingImage(ListingImage existing, long listingId, String imageUrl,
                                                  int position, boolean isPrimary) {
        if (!Helper.isValidObject(existing)) {
            throw new IllegalArgumentException("ListingImage: existing record is required for an update");
        }

        if (!Helper.isValidId(listingId)) {
            throw new IllegalArgumentException("ListingImage: listingId must be a positive id");
        }

        if (!Helper.isValidUrl(imageUrl)) {
            throw new IllegalArgumentException("ListingImage: imageUrl must be a valid URL");
        }

        if (position < 0) {
            throw new IllegalArgumentException("ListingImage: position cannot be negative");
        }

        return new ListingImage.Builder()
                .copy(existing)
                .setListingId(listingId)
                .setImageUrl(imageUrl)
                .setPosition(position)
                .setPrimary(isPrimary)
                .build();
    }

}
