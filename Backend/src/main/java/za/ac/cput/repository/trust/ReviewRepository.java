/*
 ReviewRepository.java

 Spring Data JPA repository for the Review entity.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.repository.trust;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import za.ac.cput.domain.trust.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByRevieweeId(long revieweeId);

    List<Review> findByReviewerId(long reviewerId);

    List<Review> findByTransactionId(long transactionId);

    boolean existsByTransactionIdAndReviewerId(long transactionId, long reviewerId);

    List<Review> findByRevieweeIdOrderByCreatedAtDesc(long revieweeId);

    /*
     How many DIFFERENT students have completed a purchase from this seller and
     rated it well.

     Every clause here closes a way of gaming the badge, and removing any one of
     them reopens it:

       count(distinct t.buyerId)  - five purchases by one friend is not five
                                    happy customers
       r.reviewerId = t.buyerId   - only the person who actually bought it may
                                    vouch for the sale
       r.revieweeId = :sellerId   - a review OF THE BUYER must not count towards
                                    the seller's badge
       reviewerId <> revieweeId   - no reviewing yourself
       status = COMPLETED         - the money must actually have been released

     The unique constraint on (transaction_id, reviewer_id) does the rest, by
     stopping one buyer stacking several good reviews on a single sale.
    */
    @Query("""
           select count(distinct t.buyerId)
             from Transaction t, Review r
            where r.transactionId = t.transactionId
              and t.sellerId      = :sellerId
              and r.revieweeId    = :sellerId
              and r.reviewerId    = t.buyerId
              and r.reviewerId   <> r.revieweeId
              and t.status        = za.ac.cput.domain.enums.TransactionStatus.COMPLETED
              and r.rating       >= :minRating
           """)
    long countQualifyingBuyers(@Param("sellerId") long sellerId, @Param("minRating") int minRating);

    /**
     * Overall average across every review of this person.
     *
     * Deliberately NOT filtered to good reviews: the badge needs a floor so that
     * five happy buyers cannot outweigh twenty unhappy ones.
     */
    @Query("select coalesce(avg(r.rating), 0) from Review r where r.revieweeId = :userId")
    double averageRatingFor(@Param("userId") long userId);

    long countByRevieweeId(long revieweeId);

}
