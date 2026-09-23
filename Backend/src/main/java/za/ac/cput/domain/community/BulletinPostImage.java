/*
 BulletinPostImage.java

 BulletinPostImage POJO class

 Author: Aidan Barends 230255639
 Date: 17 September 2026
*/

package za.ac.cput.domain.community;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "bulletin_post_image")
public class BulletinPostImage {
    //  Variables/Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long imageId;

    @Column(nullable = false, name = "bulletin_post_id")
    private long bulletinPostId;

    @Column(nullable = false, length = 500, name = "image_url")
    private String imageUrl;

    @Column(nullable = false)
    private int position;

    @Column(nullable = false, name = "is_primary")
    private boolean isPrimary;

    //  Constructors
    protected BulletinPostImage() {
        // Required by JPA
    }

    private BulletinPostImage(Builder builder) {
        this.imageId = builder.imageId;
        this.bulletinPostId = builder.bulletinPostId;
        this.imageUrl = builder.imageUrl;
        this.position = builder.position;
        this.isPrimary = builder.isPrimary;
    }

    //  Getters
    public long getImageId() {
        return imageId;
    }

    public long getBulletinPostId() {
        return bulletinPostId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getPosition() {
        return position;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    //  toString
    @Override
    public String toString() {
        return "BulletinPostImage{" +
                "imageId=" + imageId +
                ", bulletinPostId=" + bulletinPostId +
                ", imageUrl='" + imageUrl + '\'' +
                ", position=" + position +
                ", isPrimary=" + isPrimary +
                '}';
    }

    //  Builder Class
    public static class Builder {

        //  Variables/Attributes
        private long imageId;
        private long bulletinPostId;
        private String imageUrl;
        private int position;
        private boolean isPrimary;

        //  Setters
        public Builder setImageId(long imageId) {
            this.imageId = imageId;
            return this;
        }

        public Builder setBulletinPostId(long bulletinPostId) {
            this.bulletinPostId = bulletinPostId;
            return this;
        }

        public Builder setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public Builder setPosition(int position) {
            this.position = position;
            return this;
        }

        public Builder setPrimary(boolean primary) {
            isPrimary = primary;
            return this;
        }

        public Builder copy(BulletinPostImage bulletinPostImage) {
            this.imageId = bulletinPostImage.imageId;
            this.bulletinPostId = bulletinPostImage.bulletinPostId;
            this.imageUrl = bulletinPostImage.imageUrl;
            this.position = bulletinPostImage.position;
            this.isPrimary = bulletinPostImage.isPrimary;
            return this;
        }

        //  build method
        public BulletinPostImage build() {
            return new BulletinPostImage(this);
        }
    }
}
