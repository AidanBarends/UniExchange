/*
 BulletinPostRequest.java

 Inbound payload for creating/updating a BulletinPost. Entities have no public
 setters, so requests arrive as a record and are handed to BulletinPostFactory.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.dto.community;

import za.ac.cput.domain.enums.BulletinPostStatus;

public record BulletinPostRequest(
        long authorId,
        String title,
        String content,
        BulletinPostStatus status,
        boolean isFacultyAnnouncement) {
}
