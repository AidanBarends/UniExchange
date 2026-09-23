/*
 ReviewServiceImpl.java

 Business logic for Review. Implements the generic CRUD contract
 IService<Review, Long> plus the Review-specific operations.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.service.trust;

import java.util.List;

import org.springframework.stereotype.Service;

import za.ac.cput.domain.trust.Review;
import za.ac.cput.repository.trust.ReviewRepository;

@Service
public class ReviewServiceImpl implements IReviewService {

    private final ReviewRepository repository;

    public ReviewServiceImpl(ReviewRepository repository) {
        this.repository = repository;
    }

    @Override
    public Review create(Review review) {
        return this.repository.save(review);
    }

    @Override
    public Review read(Long id) {
        return id == null ? null : this.repository.findById(id).orElse(null);
    }

    @Override
    public Review update(Review review) {
        return this.repository.save(review);
    }

    @Override
    public boolean delete(Long id) {
        if (id == null || !this.repository.existsById(id)) {
            return false;
        }
        this.repository.deleteById(id);
        return true;
    }

    @Override
    public List<Review> getAll() {
        return this.repository.findAll();
    }

    @Override
    public List<Review> findByRevieweeId(long revieweeId) {
        return this.repository.findByRevieweeId(revieweeId);
    }

    @Override
    public double averageRatingForUser(long revieweeId) {
        return this.repository.findByRevieweeId(revieweeId).stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }

}
