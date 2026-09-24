/*
 ListingImageFactory.java

 Factory for ListingImage. All construction goes through here so that every
 ListingImage is validated with Helper before it exists - the entity itself
 exposes only a Builder and a protected JPA constructor.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.factory.marketplace;

import java.net.URI;

import za.ac.cput.domain.marketplace.ListingImage;
import za.ac.cput.util.Helper;

public class ListingImageFactory {

    // Prevent instantiation - factory class
    private ListingImageFactory() {}

    public static ListingImage createListingImage(long listingId, String imageUrl, int position,
                                                  boolean isPrimary) {
        return create(listingId, imageUrl, position, isPrimary, false);
    }

    public static ListingImage createUploadedListingImage(long listingId, String imageUrl, int position,
                                                          boolean isPrimary) {
        return create(listingId, imageUrl, position, isPrimary, true);
    }

    private static ListingImage create(long listingId, String imageUrl, int position,
                                       boolean isPrimary, boolean uploadedFile) {
        if (!Helper.isValidId(listingId)) {
            throw new IllegalArgumentException("ListingImage: listingId must be a positive id");
        }

        if ((!uploadedFile && !Helper.isValidUrl(imageUrl))
                || (uploadedFile && !isStoredImageUrl(imageUrl))) {
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

    private static boolean isStoredImageUrl(String imageUrl) {
        if (Helper.isNullOrEmpty(imageUrl)) {
            return false;
        }
        try {
            URI uri = URI.create(imageUrl.trim());
            return ("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                    && uri.getHost() != null
                    && uri.getPath() != null
                    && uri.getPath().startsWith("/api/listing-images/files/");
        } catch (IllegalArgumentException exception) {
            return false;
        }
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
