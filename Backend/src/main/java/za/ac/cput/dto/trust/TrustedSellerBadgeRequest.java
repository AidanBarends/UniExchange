/*
 TrustedSellerBadgeRequest.java

 Inbound payload for creating/updating a TrustedSellerBadge. Entities have no public
 setters, so requests arrive as a record and are handed to TrustedSellerBadgeFactory.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.dto.trust;

public record TrustedSellerBadgeRequest(
        long userId) {
}
