/*
 IReviewService.java

 Service contract for Review.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.service.trust;

import java.util.List;

import za.ac.cput.domain.trust.Review;
import za.ac.cput.service.IService;

public interface IReviewService extends IService<Review, Long> {

    List<Review> findByRevieweeId(long revieweeId);

    double averageRatingForUser(long revieweeId);

}
