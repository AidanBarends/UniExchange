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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import za.ac.cput.domain.transactions.Transaction;
import za.ac.cput.domain.trust.Review;
import za.ac.cput.dto.trust.ReviewRequest;
import za.ac.cput.factory.trust.ReviewFactory;
import za.ac.cput.security.UniExchangeUserDetailsService.AuthenticatedUser;
import za.ac.cput.service.trust.IReviewService;
import za.ac.cput.service.trust.ReviewSubmissionService;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final IReviewService service;
    private final ReviewSubmissionService submissionService;

    public ReviewController(IReviewService service, ReviewSubmissionService submissionService) {
        this.service = service;
        this.submissionService = submissionService;
    }

    /**
     * Leaves a review for the other party to a completed transaction.
     *
     * The request body no longer carries reviewerId or revieweeId. The reviewer is
     * whoever holds the token, and the reviewee is derived from the transaction -
     * so there is nothing here to forge. The previous version took all three ids
     * from the client, which let anyone write any review about anyone, including
     * five-star reviews of themselves.
     */
    @PostMapping
    public ResponseEntity<Review> create(@RequestBody ReviewRequest request,
                                         @AuthenticationPrincipal AuthenticatedUser principal) {
        Review created = this.submissionService.submit(
                principal.getUser().getUserId(),
                request.transactionId(),
                request.rating(),
                request.comment());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /** Completed sales and purchases the signed-in student has not reviewed yet. */
    @GetMapping("/pending")
    public List<Transaction> pending(@AuthenticationPrincipal AuthenticatedUser principal) {
        return this.submissionService.awaitingReviewBy(principal.getUser().getUserId());
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
