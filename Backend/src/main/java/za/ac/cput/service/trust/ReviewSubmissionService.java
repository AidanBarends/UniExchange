/*
 ReviewSubmissionService.java

 Writing a review, with the rules that make a rating worth reading.

 A review may only be written by someone who actually completed a transaction
 with the person they are rating. Without that, ratings are just opinions from
 strangers and the Trusted Seller badge derived from them means nothing.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 24 September 2026
*/

package za.ac.cput.service.trust;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;

import za.ac.cput.domain.enums.TransactionStatus;
import za.ac.cput.domain.transactions.Transaction;
import za.ac.cput.domain.trust.Review;
import za.ac.cput.exception.ConflictException;
import za.ac.cput.factory.trust.ReviewFactory;
import za.ac.cput.repository.transactions.TransactionRepository;
import za.ac.cput.repository.trust.ReviewRepository;
import za.ac.cput.service.communication.NotificationPublisher;

@Service
public class ReviewSubmissionService {

    private static final Logger log = LoggerFactory.getLogger(ReviewSubmissionService.class);

    private final ReviewRepository reviewRepository;
    private final TransactionRepository transactionRepository;
    private final TrustScoreService trustScoreService;
    private final NotificationPublisher notifications;

    public ReviewSubmissionService(ReviewRepository reviewRepository,
                                   TransactionRepository transactionRepository,
                                   TrustScoreService trustScoreService,
                                   NotificationPublisher notifications) {
        this.reviewRepository = reviewRepository;
        this.transactionRepository = transactionRepository;
        this.trustScoreService = trustScoreService;
        this.notifications = notifications;
    }

    /**
     * Records a review of the other party to a completed transaction.
     *
     * The reviewee is derived from the transaction rather than taken from the
     * request - the caller says WHICH sale they are reviewing, not WHO they are
     * rating, so there is nothing to forge.
     */
    public Review submit(long reviewerId, long transactionId, int rating, String comment) {
        Transaction transaction = this.transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Review: no such transaction"));

        // Only a finished sale can be reviewed. While money is still held nobody
        // knows yet whether the deal went well.
        if (transaction.getStatus() != TransactionStatus.COMPLETED) {
            throw new ConflictException("TRANSACTION_NOT_COMPLETED",
                    "You can only review a purchase once it is complete.");
        }

        boolean isBuyer = transaction.getBuyerId() == reviewerId;
        boolean isSeller = transaction.getSellerId() == reviewerId;
        if (!isBuyer && !isSeller) {
            throw new ConflictException("NOT_YOUR_TRANSACTION",
                    "You can only review someone you actually traded with.");
        }

        // Buyers review sellers and sellers review buyers; the counterparty is
        // whichever side the reviewer is not.
        long revieweeId = isBuyer ? transaction.getSellerId() : transaction.getBuyerId();

        if (this.reviewRepository.existsByTransactionIdAndReviewerId(transactionId, reviewerId)) {
            throw new ConflictException("ALREADY_REVIEWED",
                    "You have already reviewed this transaction.");
        }

        Review saved;
        try {
            /*
             Note there is deliberately NO @Transactional on this method.

             Spring Data's save methods are transactional in their own right, so this
             call commits on its own - which is exactly what the badge step below
             needs. Wrapping the whole method in a transaction instead would mean the
             review is still uncommitted when reevaluate() runs, and its
             REQUIRES_NEW transaction would not see it: the seller would stay one
             sale short of the badge until their next review arrived.

             (An earlier version of this had a @Transactional protected save() called
             as this.save(...). That does nothing at all - self-invocation never goes
             through the proxy - which is a good illustration of why this is spelled
             out rather than left implicit.)
            */
            saved = this.reviewRepository.saveAndFlush(ReviewFactory.createReview(
                    transactionId, reviewerId, revieweeId, rating, comment));
        } catch (DataIntegrityViolationException duplicate) {
            // The unique constraint caught a double submit that raced the exists()
            // check above.
            throw new ConflictException("ALREADY_REVIEWED",
                    "You have already reviewed this transaction.");
        }

        this.notifications.reviewReceived(revieweeId, rating, transactionId);

        /*
         Badge evaluation runs in its own transaction and must never be able to undo
         the review: the review is the student's data, the badge is only derived
         from it.

         UnexpectedRollbackException is caught alongside the constraint violation
         because a REQUIRES_NEW transaction that fails internally surfaces as that at
         its commit boundary rather than as the original exception.
        */
        try {
            this.trustScoreService.reevaluate(revieweeId);
        } catch (DataIntegrityViolationException | UnexpectedRollbackException concurrent) {
            log.debug("Badge re-evaluation for {} raced another review", revieweeId);
        }

        return saved;
    }

    /** Completed transactions this user was part of and has not yet reviewed. */
    public List<Transaction> awaitingReviewBy(long userId) {
        return this.transactionRepository
                .findByBuyerIdOrSellerIdOrderByCreatedAtDesc(userId, userId).stream()
                .filter(transaction -> transaction.getStatus() == TransactionStatus.COMPLETED)
                .filter(transaction -> !this.reviewRepository
                        .existsByTransactionIdAndReviewerId(transaction.getTransactionId(), userId))
                .toList();
    }

}
