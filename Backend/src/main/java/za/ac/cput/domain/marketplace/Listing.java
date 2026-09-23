/*
 Listing.java

 Listing POJO class

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.domain.marketplace;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import za.ac.cput.domain.enums.ListingStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "listing")
public class Listing {
    //  Variables/Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long listingId;

    @Column(nullable = false, name = "seller_id")
    private long sellerId;

    @Column(nullable = false, name = "category_id")
    private long categoryId;

    @Column(nullable = false, name = "campus_id")
    private long campusId;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ListingStatus status;

    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;

    @Column(nullable = false, name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    //  Constructors
    protected Listing() {
        // Required by JPA
    }

    private Listing(Builder builder) {
        this.listingId = builder.listingId;
        this.sellerId = builder.sellerId;
        this.categoryId = builder.categoryId;
        this.campusId = builder.campusId;
        this.title = builder.title;
        this.description = builder.description;
        this.price = builder.price;
        this.status = builder.status;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
        this.deletedAt = builder.deletedAt;
    }

    //  Getters
    public long getListingId() {
        return listingId;
    }

    public long getSellerId() {
        return sellerId;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public long getCampusId() {
        return campusId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public ListingStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    //  toString
    @Override
    public String toString() {
        return "Listing{" +
                "listingId=" + listingId +
                ", sellerId=" + sellerId +
                ", categoryId=" + categoryId +
                ", campusId=" + campusId +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", deletedAt=" + deletedAt +
                '}';
    }

    //  Builder Class
    public static class Builder {

        //  Variables/Attributes
        private long listingId;
        private long sellerId;
        private long categoryId;
        private long campusId;
        private String title;
        private String description;
        private BigDecimal price;
        private ListingStatus status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime deletedAt;

        //  Setters
        public Builder setListingId(long listingId) {
            this.listingId = listingId;
            return this;
        }

        public Builder setSellerId(long sellerId) {
            this.sellerId = sellerId;
            return this;
        }

        public Builder setCategoryId(long categoryId) {
            this.categoryId = categoryId;
            return this;
        }

        public Builder setCampusId(long campusId) {
            this.campusId = campusId;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setPrice(BigDecimal price) {
            this.price = price;
            return this;
        }

        public Builder setStatus(ListingStatus status) {
            this.status = status;
            return this;
        }

        public Builder setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder setUpdatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Builder setDeletedAt(LocalDateTime deletedAt) {
            this.deletedAt = deletedAt;
            return this;
        }

        public Builder copy(Listing listing) {
            this.listingId = listing.listingId;
            this.sellerId = listing.sellerId;
            this.categoryId = listing.categoryId;
            this.campusId = listing.campusId;
            this.title = listing.title;
            this.description = listing.description;
            this.price = listing.price;
            this.status = listing.status;
            this.createdAt = listing.createdAt;
            this.updatedAt = listing.updatedAt;
            this.deletedAt = listing.deletedAt;
            return this;
        }

        //  build method
        public Listing build() {
            return new Listing(this);
        }
    }
}