/*
 BulletinPostImageRequest.java

 Inbound payload for creating/updating a BulletinPostImage. Entities have no
 public setters, so requests arrive as a record and are handed to
 BulletinPostImageFactory.

 Author: Aidan Barends 230255639
 Date: 17 September 2026
*/

package za.ac.cput.dto.community;

public record BulletinPostImageRequest(
        long bulletinPostId,
        String imageUrl,
        int position,
        boolean isPrimary) {
}
