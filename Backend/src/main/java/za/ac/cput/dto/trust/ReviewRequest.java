/*
 ReviewRequest.java

 Inbound payload for creating/updating a Review. Entities have no public
 setters, so requests arrive as a record and are handed to ReviewFactory.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.dto.trust;

public record ReviewRequest(
        long transactionId,
        long reviewerId,
        long revieweeId,
        int rating,
        String comment) {
}
