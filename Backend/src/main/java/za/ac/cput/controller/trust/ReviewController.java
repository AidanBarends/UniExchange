/*
 ReviewController.java

 REST endpoints for Review.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.controller.trust;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import za.ac.cput.domain.trust.Review;
import za.ac.cput.dto.trust.ReviewRequest;
import za.ac.cput.factory.trust.ReviewFactory;
import za.ac.cput.service.trust.IReviewService;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final IReviewService service;

    public ReviewController(IReviewService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Review> create(@RequestBody ReviewRequest request) {
        Review created = this.service.create(ReviewFactory.createReview(
                request.transactionId(), request.reviewerId(), request.revieweeId(), request.rating(),
                request.comment()));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Review> read(@PathVariable Long id) {
        Review found = this.service.read(id);
        return found == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(found);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Review> update(@PathVariable Long id, @RequestBody ReviewRequest request) {
        Review existing = this.service.read(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(this.service.update(ReviewFactory.updateReview(
                existing, request.transactionId(), request.reviewerId(), request.revieweeId(),
                request.rating(), request.comment())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return this.service.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping
    public List<Review> getAll() {
        return this.service.getAll();
    }

    @GetMapping("/reviewee/{revieweeId}")
    public List<Review> byReviewee(@PathVariable long revieweeId) {
        return this.service.findByRevieweeId(revieweeId);
    }

    @GetMapping("/reviewee/{revieweeId}/average")
    public double averageForReviewee(@PathVariable long revieweeId) {
        return this.service.averageRatingForUser(revieweeId);
    }

}
