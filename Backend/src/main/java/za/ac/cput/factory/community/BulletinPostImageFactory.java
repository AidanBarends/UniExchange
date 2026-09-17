/*
 BulletinPostImageFactory.java

 Factory for BulletinPostImage. All construction goes through here so that
 every BulletinPostImage is validated with Helper before it exists - the
 entity itself exposes only a Builder and a protected JPA constructor.

 Author: Aidan Barends 230255639
 Date: 17 September 2026
*/

package za.ac.cput.factory.community;

import za.ac.cput.domain.community.BulletinPostImage;
import za.ac.cput.util.Helper;

public class BulletinPostImageFactory {

    // Prevent instantiation - factory class
    private BulletinPostImageFactory() {}

    public static BulletinPostImage createBulletinPostImage(long bulletinPostId, String imageUrl, int position,
                                                             boolean isPrimary) {
        if (!Helper.isValidId(bulletinPostId)) {
            throw new IllegalArgumentException("BulletinPostImage: bulletinPostId must be a positive id");
        }

        if (!Helper.isValidUrl(imageUrl)) {
            throw new IllegalArgumentException("BulletinPostImage: imageUrl must be a valid URL");
        }

        if (position < 0) {
            throw new IllegalArgumentException("BulletinPostImage: position cannot be negative");
        }

        return new BulletinPostImage.Builder()
                .setBulletinPostId(bulletinPostId)
                .setImageUrl(imageUrl)
                .setPosition(position)
                .setPrimary(isPrimary)
                .build();
    }

    public static BulletinPostImage updateBulletinPostImage(BulletinPostImage existing, long bulletinPostId,
                                                             String imageUrl, int position, boolean isPrimary) {
        if (!Helper.isValidObject(existing)) {
            throw new IllegalArgumentException("BulletinPostImage: existing record is required for an update");
        }

        if (!Helper.isValidId(bulletinPostId)) {
            throw new IllegalArgumentException("BulletinPostImage: bulletinPostId must be a positive id");
        }

        if (!Helper.isValidUrl(imageUrl)) {
            throw new IllegalArgumentException("BulletinPostImage: imageUrl must be a valid URL");
        }

        if (position < 0) {
            throw new IllegalArgumentException("BulletinPostImage: position cannot be negative");
        }

        return new BulletinPostImage.Builder()
                .copy(existing)
                .setBulletinPostId(bulletinPostId)
                .setImageUrl(imageUrl)
                .setPosition(position)
                .setPrimary(isPrimary)
                .build();
    }

}
