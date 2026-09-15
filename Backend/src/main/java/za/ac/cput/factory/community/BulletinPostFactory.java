/*
 BulletinPostFactory.java

 Factory for BulletinPost. All construction goes through here so that every
 BulletinPost is validated with Helper before it exists - the entity itself
 exposes only a Builder and a protected JPA constructor.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026

 Edited: Aidan Barends 230255639
 Date: 15 September 2026
 Added category parameter to createBulletinPost/updateBulletinPost, defaulting to GENERAL.
*/

package za.ac.cput.factory.community;

import java.time.LocalDateTime;

import za.ac.cput.domain.community.BulletinPost;
import za.ac.cput.domain.enums.BulletinPostCategory;
import za.ac.cput.domain.enums.BulletinPostStatus;
import za.ac.cput.util.Helper;

public class BulletinPostFactory {

    // Prevent instantiation - factory class
    private BulletinPostFactory() {}

    public static BulletinPost createBulletinPost(long authorId, String title, String content,
                                                  BulletinPostStatus status, boolean isFacultyAnnouncement,
                                                  BulletinPostCategory category) {
        if (!Helper.isValidId(authorId)) {
            throw new IllegalArgumentException("BulletinPost: authorId must be a positive id");
        }

        if (Helper.isNullOrEmpty(title)) {
            throw new IllegalArgumentException("BulletinPost: title is required");
        }

        if (Helper.isNullOrEmpty(content)) {
            throw new IllegalArgumentException("BulletinPost: content is required");
        }

        if (!Helper.isValidObject(status)) {
            throw new IllegalArgumentException("BulletinPost: status is required");
        }

        LocalDateTime now = LocalDateTime.now();

        return new BulletinPost.Builder()
                .setAuthorId(authorId)
                .setTitle(title)
                .setContent(content)
                .setStatus(status)
                .setFacultyAnnouncement(isFacultyAnnouncement)
                .setCategory(category != null ? category : BulletinPostCategory.GENERAL)
                .setCreatedAt(now)
                .setUpdatedAt(now)
                .build();
    }

    public static BulletinPost updateBulletinPost(BulletinPost existing, long authorId, String title,
                                                  String content, BulletinPostStatus status,
                                                  boolean isFacultyAnnouncement, BulletinPostCategory category) {
        if (!Helper.isValidObject(existing)) {
            throw new IllegalArgumentException("BulletinPost: existing record is required for an update");
        }

        if (!Helper.isValidId(authorId)) {
            throw new IllegalArgumentException("BulletinPost: authorId must be a positive id");
        }

        if (Helper.isNullOrEmpty(title)) {
            throw new IllegalArgumentException("BulletinPost: title is required");
        }

        if (Helper.isNullOrEmpty(content)) {
            throw new IllegalArgumentException("BulletinPost: content is required");
        }

        if (!Helper.isValidObject(status)) {
            throw new IllegalArgumentException("BulletinPost: status is required");
        }

        return new BulletinPost.Builder()
                .copy(existing)
                .setAuthorId(authorId)
                .setTitle(title)
                .setContent(content)
                .setStatus(status)
                .setFacultyAnnouncement(isFacultyAnnouncement)
                .setCategory(category != null ? category : existing.getCategory())
                .setUpdatedAt(LocalDateTime.now())
                .build();
    }

}
