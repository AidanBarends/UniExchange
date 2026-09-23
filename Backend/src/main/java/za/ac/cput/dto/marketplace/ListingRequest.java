/*
 ListingRequest.java

 Inbound payload for creating/updating a Listing. Entities have no public
 setters, so requests arrive as a record and are handed to ListingFactory.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.dto.marketplace;

import java.math.BigDecimal;

import za.ac.cput.domain.enums.ListingStatus;

public record ListingRequest(
        long sellerId,
        long categoryId,
        long campusId,
        String title,
        String description,
        BigDecimal price,
        ListingStatus status) {
}
