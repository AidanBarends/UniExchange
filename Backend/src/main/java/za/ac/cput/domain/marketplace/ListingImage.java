/*
 ListingImage.java

 ListingImage POJO class

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.domain.marketplace;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "listing_image")
public class ListingImage {
    //  Variables/Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long imageId;

    @Column(nullable = false, name = "listing_id")
    private long listingId;

    @Column(nullable = false, length = 500, name = "image_url")
    private String imageUrl;

    @Column(nullable = false)
    private int position;

    @Column(nullable = false, name = "is_primary")
    private boolean isPrimary;

    //  Constructors
    protected ListingImage() {
        // Required by JPA
    }

    private ListingImage(Builder builder) {
        this.imageId = builder.imageId;
        this.listingId = builder.listingId;
        this.imageUrl = builder.imageUrl;
        this.position = builder.position;
        this.isPrimary = builder.isPrimary;
    }

    //  Getters
    public long getImageId() {
        return imageId;
    }

    public long getListingId() {
        return listingId;
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
        return "ListingImage{" +
                "imageId=" + imageId +
                ", listingId=" + listingId +
                ", imageUrl='" + imageUrl + '\'' +
                ", position=" + position +
                ", isPrimary=" + isPrimary +
                '}';
    }

    //  Builder Class
    public static class Builder {

        //  Variables/Attributes
        private long imageId;
        private long listingId;
        private String imageUrl;
        private int position;
        private boolean isPrimary;

        //  Setters
        public Builder setImageId(long imageId) {
            this.imageId = imageId;
            return this;
        }

        public Builder setListingId(long listingId) {
            this.listingId = listingId;
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

        public Builder copy(ListingImage listingImage) {
            this.imageId = listingImage.imageId;
            this.listingId = listingImage.listingId;
            this.imageUrl = listingImage.imageUrl;
            this.position = listingImage.position;
            this.isPrimary = listingImage.isPrimary;
            return this;
        }

        //  build method
        public ListingImage build() {
            return new ListingImage(this);
        }
    }
}