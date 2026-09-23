/*
 ListingImageRequest.java

 Inbound payload for creating/updating a ListingImage. Entities have no public
 setters, so requests arrive as a record and are handed to ListingImageFactory.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.dto.marketplace;

public record ListingImageRequest(
        long listingId,
        String imageUrl,
        int position,
        boolean isPrimary) {
}
