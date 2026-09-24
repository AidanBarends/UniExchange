/*
 EscrowServiceTest.java

 The escrow lifecycle and the two races that would cost real money: paying a
 seller twice, and selling one item to two buyers.

 @SpringBootTest without @Transactional for the same reason as
 WalletMoneySafetyTest - a shared transaction would hide exactly the concurrency
 this is meant to catch.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 24 September 2026
*/

package za.ac.cput.service.transactions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import za.ac.cput.domain.enums.ListingStatus;
import za.ac.cput.domain.enums.TransactionStatus;
import za.ac.cput.domain.marketplace.Listing;
import za.ac.cput.domain.transactions.Transaction;
import za.ac.cput.exception.ConflictException;
import za.ac.cput.exception.InsufficientFundsException;
import za.ac.cput.factory.marketplace.ListingFactory;
import za.ac.cput.repository.marketplace.ListingRepository;
import za.ac.cput.repository.transactions.TransactionRepository;

@SpringBootTest
class EscrowServiceTest {

    private static final AtomicInteger NEXT_USER_ID = new AtomicInteger(800_000);

    @Autowired
    private IEscrowService escrowService;

    @Autowired
    private IWalletService walletService;

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private long freshUser() {
        return NEXT_USER_ID.incrementAndGet();
    }

    private Listing listingFor(long sellerId, String price) {
        return this.listingRepository.save(ListingFactory.createListing(
                sellerId, 1L, 1L, "Test item", "A thing for sale",
                new BigDecimal(price), ListingStatus.ACTIVE));
    }

    private long fundedBuyer(String amount) {
        long buyerId = freshUser();
        this.walletService.credit(buyerId, new BigDecimal(amount), "TEST", null, "seed");
        return buyerId;
    }

    @Test
    void purchaseHoldsTheMoneyWithoutPayingTheSeller() {
        long sellerId = freshUser();
        long buyerId = fundedBuyer("500.00");
        Listing listing = listingFor(sellerId, "150.00");

        Transaction transaction = this.escrowService.purchase(
                buyerId, listing.getListingId(), new BigDecimal("150.00"));

        assertEquals(TransactionStatus.PENDING, transaction.getStatus());
        // Buyer has paid...
        assertEquals(0, new BigDecimal("350.00")
                .compareTo(this.walletService.findByUserId(buyerId).getBalance()));
        // ...but the seller has NOT been paid. That is the whole point of escrow.
        assertEquals(0, BigDecimal.ZERO
                .compareTo(this.walletService.getOrCreateForUser(sellerId).getBalance()));
        assertEquals(0, new BigDecimal("150.00").compareTo(this.escrowService.heldFor(buyerId)));
    }

    @Test
    void confirmingReceiptPaysTheSeller() {
        long sellerId = freshUser();
        long buyerId = fundedBuyer("500.00");
        Listing listing = listingFor(sellerId, "150.00");

        Transaction transaction = this.escrowService.purchase(
                buyerId, listing.getListingId(), new BigDecimal("150.00"));
        Transaction completed = this.escrowService.confirmReceipt(buyerId, transaction.getTransactionId());

        assertEquals(TransactionStatus.COMPLETED, completed.getStatus());
        assertEquals(0, new BigDecimal("150.00")
                .compareTo(this.walletService.findByUserId(sellerId).getBalance()));
        assertEquals(0, BigDecimal.ZERO.compareTo(this.escrowService.heldFor(buyerId)));
    }

    @Test
    void cancellingRefundsTheBuyerAndRelistsTheItem() {
        long sellerId = freshUser();
        long buyerId = fundedBuyer("500.00");
        Listing listing = listingFor(sellerId, "150.00");

        Transaction transaction = this.escrowService.purchase(
                buyerId, listing.getListingId(), new BigDecimal("150.00"));
        this.escrowService.cancel(buyerId, transaction.getTransactionId());

        assertEquals(0, new BigDecimal("500.00")
                .compareTo(this.walletService.findByUserId(buyerId).getBalance()),
                "a cancelled purchase must return every cent");
        assertEquals(0, BigDecimal.ZERO
                .compareTo(this.walletService.getOrCreateForUser(sellerId).getBalance()));
        assertEquals(ListingStatus.ACTIVE,
                this.listingRepository.findById(listing.getListingId()).orElseThrow().getStatus(),
                "a cancelled purchase puts the item back on sale");
    }

    @Test
    void cannotBuyWithoutEnoughMoney() {
        long sellerId = freshUser();
        long buyerId = fundedBuyer("10.00");
        Listing listing = listingFor(sellerId, "150.00");

        assertThrows(InsufficientFundsException.class, () ->
                this.escrowService.purchase(buyerId, listing.getListingId(), new BigDecimal("150.00")));

        // The rollback must also un-claim the listing, or a failed purchase would
        // silently take the item off the market.
        assertEquals(ListingStatus.ACTIVE,
                this.listingRepository.findById(listing.getListingId()).orElseThrow().getStatus());
    }

    @Test
    void rejectsAPriceThatChangedUnderTheBuyer() {
        long sellerId = freshUser();
        long buyerId = fundedBuyer("500.00");
        Listing listing = listingFor(sellerId, "150.00");

        // The buyer agreed to R100, but the listing says R150.
        assertThrows(ConflictException.class, () ->
                this.escrowService.purchase(buyerId, listing.getListingId(), new BigDecimal("100.00")));
    }

    @Test
    void cannotBuyYourOwnListing() {
        long sellerId = fundedBuyer("500.00");
        Listing listing = listingFor(sellerId, "150.00");

        assertThrows(IllegalArgumentException.class, () ->
                this.escrowService.purchase(sellerId, listing.getListingId(), new BigDecimal("150.00")));
    }

    /*
     Double-release: two "I received it" clicks arriving together. Without the
     compare-and-set on status, both read PENDING and both credit the seller -
     who is paid twice for one sale, out of money that does not exist.
    */
    @Test
    void confirmingTwiceOnlyPaysOnce() throws Exception {
        long sellerId = freshUser();
        long buyerId = fundedBuyer("500.00");
        Listing listing = listingFor(sellerId, "150.00");
        Transaction transaction = this.escrowService.purchase(
                buyerId, listing.getListingId(), new BigDecimal("150.00"));
        long transactionId = transaction.getTransactionId();

        ExecutorService pool = Executors.newFixedThreadPool(4);
        try {
            List<Callable<Boolean>> attempts = java.util.Collections.nCopies(4, () -> {
                try {
                    this.escrowService.confirmReceipt(buyerId, transactionId);
                    return true;
                } catch (ConflictException expected) {
                    return false;
                }
            });

            long succeeded = 0;
            for (Future<Boolean> result : pool.invokeAll(attempts)) {
                if (result.get()) succeeded++;
            }
            assertEquals(1, succeeded, "only one confirmation may release the money");
        } finally {
            pool.shutdownNow();
        }

        assertEquals(0, new BigDecimal("150.00")
                .compareTo(this.walletService.findByUserId(sellerId).getBalance()),
                "the seller must be paid exactly once");
    }

    /*
     Double-sell: two buyers, one item. Note that locking wallets does not help
     here at all - the two buyers debit two different wallet rows, so the only
     contested resource is the listing.
    */
    @Test
    void twoBuyersCannotBuyTheSameItem() throws Exception {
        long sellerId = freshUser();
        long buyerA = fundedBuyer("500.00");
        long buyerB = fundedBuyer("500.00");
        Listing listing = listingFor(sellerId, "150.00");
        long listingId = listing.getListingId();

        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            List<Callable<Boolean>> attempts = List.of(
                    buyAttempt(buyerA, listingId),
                    buyAttempt(buyerB, listingId));

            long succeeded = 0;
            for (Future<Boolean> result : pool.invokeAll(attempts)) {
                if (result.get()) succeeded++;
            }
            assertEquals(1, succeeded, "one item, one buyer");
        } finally {
            pool.shutdownNow();
        }

        assertEquals(1, this.transactionRepository.findByListingId(listingId).size());
    }

    private Callable<Boolean> buyAttempt(long buyerId, long listingId) {
        return () -> {
            try {
                this.escrowService.purchase(buyerId, listingId, new BigDecimal("150.00"));
                return true;
            } catch (ConflictException | InsufficientFundsException expected) {
                return false;
            }
        };
    }

    /**
     * The reconciliation identity for the implicit-escrow model: money that has
     * left a buyer's wallet but not yet reached a seller must be exactly the sum of
     * PENDING transactions. If this drifts, a cent has been created or destroyed.
     */
    @Test
    void escrowedMoneyIsAccountedForExactly() {
        long sellerId = freshUser();
        long buyerId = fundedBuyer("500.00");
        Listing first = listingFor(sellerId, "120.00");
        Listing second = listingFor(sellerId, "80.00");

        this.escrowService.purchase(buyerId, first.getListingId(), new BigDecimal("120.00"));
        this.escrowService.purchase(buyerId, second.getListingId(), new BigDecimal("80.00"));

        BigDecimal available = this.walletService.findByUserId(buyerId).getBalance();
        BigDecimal held = this.escrowService.heldFor(buyerId);

        assertEquals(0, new BigDecimal("300.00").compareTo(available));
        assertEquals(0, new BigDecimal("200.00").compareTo(held));
        assertEquals(0, new BigDecimal("500.00").compareTo(available.add(held)),
                "available + held must still equal everything the buyer put in");
    }

}
