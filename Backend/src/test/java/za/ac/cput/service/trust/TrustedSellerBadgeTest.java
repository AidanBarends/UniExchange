/*
 TrustedSellerBadgeTest.java

 The badge rule, and the four ways of gaming it that the query has to close.

 A badge that can be farmed is worse than no badge, because students will trust
 it. Each test here corresponds to one shortcut someone would actually try.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 24 September 2026
*/

package za.ac.cput.service.trust;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import za.ac.cput.domain.enums.ListingStatus;
import za.ac.cput.domain.marketplace.Listing;
import za.ac.cput.domain.transactions.Transaction;
import za.ac.cput.exception.ConflictException;
import za.ac.cput.factory.marketplace.ListingFactory;
import za.ac.cput.repository.marketplace.ListingRepository;
import za.ac.cput.service.transactions.IEscrowService;
import za.ac.cput.service.transactions.IWalletService;

@SpringBootTest
class TrustedSellerBadgeTest {

    private static final AtomicInteger NEXT_USER_ID = new AtomicInteger(700_000);

    @Autowired
    private IEscrowService escrowService;

    @Autowired
    private IWalletService walletService;

    @Autowired
    private ReviewSubmissionService reviewService;

    @Autowired
    private TrustScoreService trustScoreService;

    @Autowired
    private ListingRepository listingRepository;

    private long freshUser() {
        return NEXT_USER_ID.incrementAndGet();
    }

    /** Runs a full sale through to COMPLETED and returns it, ready to be reviewed. */
    private Transaction completedSale(long sellerId, long buyerId) {
        this.walletService.credit(buyerId, new BigDecimal("100.00"), "TEST", null, "seed");

        Listing listing = this.listingRepository.save(ListingFactory.createListing(
                sellerId, 1L, 1L, "Item", "For sale",
                new BigDecimal("10.00"), ListingStatus.ACTIVE));

        Transaction transaction = this.escrowService.purchase(
                buyerId, listing.getListingId(), new BigDecimal("10.00"));
        return this.escrowService.confirmReceipt(buyerId, transaction.getTransactionId());
    }

    private void sellTo(long sellerId, long buyerId, int rating) {
        Transaction sale = completedSale(sellerId, buyerId);
        this.reviewService.submit(buyerId, sale.getTransactionId(), rating, "Fine");
    }

    @Test
    void awardedAfterFiveGoodSalesToFiveDifferentBuyers() {
        long sellerId = freshUser();

        for (int i = 0; i < 4; i++) {
            sellTo(sellerId, freshUser(), 5);
        }
        assertFalse(this.trustScoreService.hasBadge(sellerId), "four good sales is not yet enough");

        sellTo(sellerId, freshUser(), 5);
        assertTrue(this.trustScoreService.hasBadge(sellerId), "the fifth good sale earns the badge");
    }

    /*
     The obvious shortcut: get one friend to buy five times. Five sales, five
     five-star reviews, one actual customer. count(distinct buyerId) is what stops
     it.
    */
    @Test
    void fiveSalesToTheSameBuyerDoNotEarnIt() {
        long sellerId = freshUser();
        long friendId = freshUser();

        for (int i = 0; i < 6; i++) {
            sellTo(sellerId, friendId, 5);
        }

        assertFalse(this.trustScoreService.hasBadge(sellerId),
                "one repeat customer is not five happy customers");
    }

    /*
     The second shortcut: five glowing reviews sitting next to a pile of terrible
     ones. The average floor is what stops it.
    */
    @Test
    void goodSalesDoNotOutweighABadAverage() {
        long sellerId = freshUser();

        for (int i = 0; i < 5; i++) {
            sellTo(sellerId, freshUser(), 5);
        }
        assertTrue(this.trustScoreService.hasBadge(sellerId));

        // Now the complaints arrive.
        for (int i = 0; i < 15; i++) {
            sellTo(sellerId, freshUser(), 1);
        }

        assertFalse(this.trustScoreService.hasBadge(sellerId),
                "the badge must be revoked once the overall average falls through the floor");
    }

    @Test
    void lowRatedSalesDoNotCount() {
        long sellerId = freshUser();

        for (int i = 0; i < 6; i++) {
            sellTo(sellerId, freshUser(), 3);   // completed, but not "good"
        }

        assertFalse(this.trustScoreService.hasBadge(sellerId));
    }

    /*
     The third shortcut: review the same sale five times. The unique constraint on
     (transaction_id, reviewer_id) is what stops it, and ReviewSubmissionService
     turns the violation into a clear 409 rather than a 500.
    */
    @Test
    void oneTransactionCannotBeReviewedTwice() {
        long sellerId = freshUser();
        long buyerId = freshUser();
        Transaction sale = completedSale(sellerId, buyerId);

        this.reviewService.submit(buyerId, sale.getTransactionId(), 5, "Great");

        assertThrows(ConflictException.class, () ->
                this.reviewService.submit(buyerId, sale.getTransactionId(), 5, "Great again"));
    }

    /*
     The fourth shortcut: review a sale you had nothing to do with, or review
     yourself. Both are rejected because the reviewee is derived from the
     transaction rather than supplied by the caller.
    */
    @Test
    void onlyTheOtherPartyMayReview() {
        long sellerId = freshUser();
        long buyerId = freshUser();
        long strangerId = freshUser();
        Transaction sale = completedSale(sellerId, buyerId);

        assertThrows(ConflictException.class, () ->
                this.reviewService.submit(strangerId, sale.getTransactionId(), 5, "Nice"));

        // The seller reviewing their own sale rates the BUYER, never themselves -
        // so it can never contribute to the seller's own badge.
        this.reviewService.submit(sellerId, sale.getTransactionId(), 5, "Good buyer");
        assertFalse(this.trustScoreService.hasBadge(sellerId));
    }

    @Test
    void cannotReviewAPurchaseThatIsStillHeld() {
        long sellerId = freshUser();
        long buyerId = freshUser();
        this.walletService.credit(buyerId, new BigDecimal("100.00"), "TEST", null, "seed");

        Listing listing = this.listingRepository.save(ListingFactory.createListing(
                sellerId, 1L, 1L, "Item", "For sale",
                new BigDecimal("10.00"), ListingStatus.ACTIVE));
        Transaction pending = this.escrowService.purchase(
                buyerId, listing.getListingId(), new BigDecimal("10.00"));

        assertThrows(ConflictException.class, () ->
                this.reviewService.submit(buyerId, pending.getTransactionId(), 5, "Too early"));
    }

}
