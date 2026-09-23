/*
 ReviewFactory.java

 Factory for Review. All construction goes through here so that every
 Review is validated with Helper before it exists - the entity itself
 exposes only a Builder and a protected JPA constructor.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.factory.trust;

import java.time.LocalDateTime;

import za.ac.cput.domain.trust.Review;
import za.ac.cput.util.Helper;

public class ReviewFactory {

    // Prevent instantiation - factory class
    private ReviewFactory() {}

    public static Review createReview(long transactionId, long reviewerId, long revieweeId, int rating,
                                      String comment) {
        if (!Helper.isValidId(transactionId)) {
            throw new IllegalArgumentException("Review: transactionId must be a positive id");
        }

        if (!Helper.isValidId(reviewerId)) {
            throw new IllegalArgumentException("Review: reviewerId must be a positive id");
        }

        if (!Helper.isValidId(revieweeId)) {
            throw new IllegalArgumentException("Review: revieweeId must be a positive id");
        }

        if (!Helper.isValidRating(rating)) {
            throw new IllegalArgumentException("Review: rating must be between 1 and 5");
        }

        LocalDateTime now = LocalDateTime.now();

        return new Review.Builder()
                .setTransactionId(transactionId)
                .setReviewerId(reviewerId)
                .setRevieweeId(revieweeId)
                .setRating(rating)
                .setComment(comment)
                .setCreatedAt(now)
                .build();
    }

    public static Review updateReview(Review existing, long transactionId, long reviewerId, long revieweeId,
                                      int rating, String comment) {
        if (!Helper.isValidObject(existing)) {
            throw new IllegalArgumentException("Review: existing record is required for an update");
        }

        if (!Helper.isValidId(transactionId)) {
            throw new IllegalArgumentException("Review: transactionId must be a positive id");
        }

        if (!Helper.isValidId(reviewerId)) {
            throw new IllegalArgumentException("Review: reviewerId must be a positive id");
        }

        if (!Helper.isValidId(revieweeId)) {
            throw new IllegalArgumentException("Review: revieweeId must be a positive id");
        }

        if (!Helper.isValidRating(rating)) {
            throw new IllegalArgumentException("Review: rating must be between 1 and 5");
        }

        return new Review.Builder()
                .copy(existing)
                .setTransactionId(transactionId)
                .setReviewerId(reviewerId)
                .setRevieweeId(revieweeId)
                .setRating(rating)
                .setComment(comment)
                .build();
    }

}
