/*
 Review.java

 Review POJO class

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.domain.trust;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "review")
public class Review {
    //  Variables/Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long reviewId;

    @Column(nullable = false, name = "transaction_id")
    private long transactionId;

    @Column(nullable = false, name = "reviewer_id")
    private long reviewerId;

    @Column(nullable = false, name = "reviewee_id")
    private long revieweeId;

    @Column(nullable = false)
    private int rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;

    //  Constructors
    protected Review() {
        // Required by JPA
    }

    private Review(Builder builder) {
        this.reviewId = builder.reviewId;
        this.transactionId = builder.transactionId;
        this.reviewerId = builder.reviewerId;
        this.revieweeId = builder.revieweeId;
        this.rating = builder.rating;
        this.comment = builder.comment;
        this.createdAt = builder.createdAt;
    }

    //  Getters
    public long getReviewId() {
        return reviewId;
    }

    public long getTransactionId() {
        return transactionId;
    }

    public long getReviewerId() {
        return reviewerId;
    }

    public long getRevieweeId() {
        return revieweeId;
    }

    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    //  toString
    @Override
    public String toString() {
        return "Review{" +
                "reviewId=" + reviewId +
                ", transactionId=" + transactionId +
                ", reviewerId=" + reviewerId +
                ", revieweeId=" + revieweeId +
                ", rating=" + rating +
                ", comment='" + comment + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }

    //  Builder Class
    public static class Builder {

        //  Variables/Attributes
        private long reviewId;
        private long transactionId;
        private long reviewerId;
        private long revieweeId;
        private int rating;
        private String comment;
        private LocalDateTime createdAt;

        //  Setters
        public Builder setReviewId(long reviewId) {
            this.reviewId = reviewId;
            return this;
        }

        public Builder setTransactionId(long transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        public Builder setReviewerId(long reviewerId) {
            this.reviewerId = reviewerId;
            return this;
        }

        public Builder setRevieweeId(long revieweeId) {
            this.revieweeId = revieweeId;
            return this;
        }

        public Builder setRating(int rating) {
            this.rating = rating;
            return this;
        }

        public Builder setComment(String comment) {
            this.comment = comment;
            return this;
        }

        public Builder setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder copy(Review review) {
            this.reviewId = review.reviewId;
            this.transactionId = review.transactionId;
            this.reviewerId = review.reviewerId;
            this.revieweeId = review.revieweeId;
            this.rating = review.rating;
            this.comment = review.comment;
            this.createdAt = review.createdAt;
            return this;
        }

        //  build method
        public Review build() {
            return new Review(this);
        }
    }
}