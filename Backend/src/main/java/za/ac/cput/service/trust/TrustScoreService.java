/*
 TrustScoreService.java

 Decides who has earned the Trusted Seller badge, and takes it away again if they
 stop deserving it.

 THE RULE

 Five completed sales to five DIFFERENT buyers, each rated at least 4, while the
 seller's overall average stays at or above the floor. All three numbers are
 properties, not constants, so the bar can be tuned without a code change.

 The "different buyers" and "overall average" parts are what make the badge mean
 anything: without them, five purchases by one friend, or five good reviews
 sitting alongside twenty terrible ones, would both earn it.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 24 September 2026
*/

package za.ac.cput.service.trust;

import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import za.ac.cput.domain.trust.TrustedSellerBadge;
import za.ac.cput.factory.trust.TrustedSellerBadgeFactory;
import za.ac.cput.repository.trust.ReviewRepository;
import za.ac.cput.repository.trust.TrustedSellerBadgeRepository;
import za.ac.cput.service.communication.NotificationPublisher;

@Service
public class TrustScoreService {

    private static final Logger log = LoggerFactory.getLogger(TrustScoreService.class);

    private final ReviewRepository reviewRepository;
    private final TrustedSellerBadgeRepository badgeRepository;
    private final NotificationPublisher notifications;
    private final int minGoodSales;
    private final int minRating;
    private final double minAverage;

    public TrustScoreService(ReviewRepository reviewRepository,
                             TrustedSellerBadgeRepository badgeRepository,
                             NotificationPublisher notifications,
                             @Value("${app.trust.badge.min-good-sales:5}") int minGoodSales,
                             @Value("${app.trust.badge.min-rating:4}") int minRating,
                             @Value("${app.trust.badge.min-average:4.0}") double minAverage) {
        this.reviewRepository = reviewRepository;
        this.badgeRepository = badgeRepository;
        this.notifications = notifications;
        this.minGoodSales = minGoodSales;
        this.minRating = minRating;
        this.minAverage = minAverage;
    }

    /**
     * Re-evaluates one seller and awards or revokes accordingly.
     *
     * REQUIRES_NEW is deliberate. This runs after a review is saved, and a badge
     * problem must never roll back the student's review - the review is their data,
     * the badge is only derived from it. A separate transaction also means the
     * unique-constraint violation that a concurrent award causes is contained here
     * rather than poisoning the caller's transaction.
     *
     * Callers must invoke this AFTER the review has been committed, or the count
     * query will not see it.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void reevaluate(long sellerId) {
        long qualifyingBuyers = this.reviewRepository.countQualifyingBuyers(sellerId, this.minRating);
        double average = this.reviewRepository.averageRatingFor(sellerId);

        boolean earned = qualifyingBuyers >= this.minGoodSales && average >= this.minAverage;
        Optional<TrustedSellerBadge> existing = this.badgeRepository.findByUserId(sellerId);

        if (earned) {
            award(sellerId, existing);
        } else {
            existing.filter(badge -> badge.getRevokedAt() == null)
                    .ifPresent(badge -> revoke(sellerId, badge));
        }
    }

    /** True only for a badge that exists AND has not been revoked. */
    public boolean hasBadge(long userId) {
        return this.badgeRepository.findByUserId(userId)
                .filter(badge -> badge.getRevokedAt() == null)
                .isPresent();
    }

    private void award(long sellerId, Optional<TrustedSellerBadge> existing) {
        if (existing.isPresent()) {
            TrustedSellerBadge badge = existing.get();
            if (badge.getRevokedAt() == null) {
                return;   // already has it
            }
            /*
             Re-award rather than insert. user_id is UNIQUE, so a revoked badge
             occupies the only row this seller can ever have - inserting would just
             hit the constraint. Clearing revokedAt is the reinstatement.
            */
            this.badgeRepository.save(new TrustedSellerBadge.Builder()
                    .copy(badge)
                    .setRevokedAt(null)
                    .setEarnedAt(LocalDateTime.now())
                    .build());
            log.info("Trusted Seller badge reinstated for user {}", sellerId);
            this.notifications.badgeEarned(sellerId);
            return;
        }

        try {
            /*
             saveAndFlush, not save: save() defers the INSERT to commit, which is
             AFTER this method returns - so the constraint violation would escape
             the catch below and surface somewhere far less helpful.
            */
            this.badgeRepository.saveAndFlush(
                    TrustedSellerBadgeFactory.createTrustedSellerBadge(sellerId));
            log.info("Trusted Seller badge awarded to user {}", sellerId);
            this.notifications.badgeEarned(sellerId);
        } catch (org.springframework.dao.DataIntegrityViolationException concurrentAward) {
            // Two reviews landed at once and both crossed the threshold. The unique
            // constraint on user_id means exactly one insert wins, which is the
            // correct outcome - the loser has nothing left to do.
            log.debug("Trusted Seller badge for {} was awarded concurrently", sellerId);
        }
    }

    private void revoke(long sellerId, TrustedSellerBadge badge) {
        this.badgeRepository.save(new TrustedSellerBadge.Builder()
                .copy(badge)
                .setRevokedAt(LocalDateTime.now())
                .build());
        log.info("Trusted Seller badge revoked for user {}", sellerId);
    }

}
