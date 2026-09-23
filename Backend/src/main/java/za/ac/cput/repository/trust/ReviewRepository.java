/*
 ReviewRepository.java

 Spring Data JPA repository for the Review entity.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.repository.trust;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import za.ac.cput.domain.trust.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByRevieweeId(long revieweeId);

    List<Review> findByReviewerId(long reviewerId);

    List<Review> findByTransactionId(long transactionId);

}
