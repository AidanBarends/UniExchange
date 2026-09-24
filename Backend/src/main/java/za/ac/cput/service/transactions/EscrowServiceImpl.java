/*
 EscrowServiceImpl.java

 Wallet-funded purchases with the money held until the buyer confirms receipt.

 HOW ESCROW IS REPRESENTED

 There is no "held balance" column anywhere. Money in escrow is simply a
 Transaction row in PENDING: the buyer has been debited, the seller has not yet
 been credited, so the funds exist in neither wallet. Held total for a buyer is
 sum(amount) over their PENDING transactions.

 That is deliberate. Adding a held_balance column to wallet would mean a NOT NULL
 column on a populated table, which ddl-auto=update cannot do, and it would give
 two sources of truth that could drift apart. The trade-off is that
 sum(wallet.balance) alone no longer accounts for every cent - the reconciliation
 identity becomes:

     sum(wallet.balance) + sum(PENDING transaction amounts) == sum(completed top-ups)

 which EscrowReconciliationTest asserts.

 LOCK ORDER - keep to this, in every method here

     1. listing   (compare-and-set, no row lock held)
     2. transaction (compare-and-set)
     3. wallets, by ascending userId

 Deadlocks come from two transactions taking the same locks in different orders.
 Wallets are ordered by USER id rather than wallet id because the user ids are
 known up front, while discovering a wallet id would need an unlocked read that
 defeats the locking read entirely (see WalletRepository).

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 24 September 2026
*/

package za.ac.cput.service.transactions;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import za.ac.cput.domain.enums.ListingStatus;
import za.ac.cput.domain.enums.PaymentMethod;
import za.ac.cput.domain.enums.TransactionStatus;
import za.ac.cput.domain.marketplace.Listing;
import za.ac.cput.domain.transactions.Transaction;
import za.ac.cput.exception.ConflictException;
import za.ac.cput.factory.transactions.TransactionFactory;
import za.ac.cput.repository.marketplace.ListingRepository;
import za.ac.cput.repository.transactions.TransactionRepository;
import za.ac.cput.service.communication.NotificationPublisher;
import za.ac.cput.util.Helper;

@Service
public class EscrowServiceImpl implements IEscrowService {

    /** Ledger tag so a purchase or release is traceable back to its transaction. */
    private static final String REFERENCE_TYPE = "TRANSACTION";

    private final TransactionRepository transactionRepository;
    private final ListingRepository listingRepository;
    private final IWalletService walletService;
    private final NotificationPublisher notifications;

    public EscrowServiceImpl(TransactionRepository transactionRepository,
                             ListingRepository listingRepository,
                             IWalletService walletService,
                             NotificationPublisher notifications) {
        this.transactionRepository = transactionRepository;
        this.listingRepository = listingRepository;
        this.walletService = walletService;
        this.notifications = notifications;
    }

    @Transactional
    @Override
    public Transaction purchase(long buyerId, long listingId, BigDecimal expectedAmount) {
        Listing listing = this.listingRepository.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("Purchase: that listing does not exist"));

        if (listing.getSellerId() == buyerId) {
            throw new IllegalArgumentException("Purchase: you cannot buy your own listing");
        }

        BigDecimal price = listing.getPrice();
        if (!Helper.isPositiveMoney(price)) {
            throw new IllegalArgumentException("Purchase: this listing has no valid price");
        }

        /*
         The price the buyer agreed to must still be the price. Without this a
         seller could edit the listing between the buyer opening it and confirming,
         and the buyer would be charged whatever the new number is.
        */
        if (expectedAmount != null && price.compareTo(expectedAmount) != 0) {
            throw new ConflictException("PRICE_CHANGED",
                    "The price changed while you were buying. It is now R" + price + ".");
        }

        // 1. Claim the listing. Row count is the authority - see ListingRepository.
        if (this.listingRepository.compareAndSetStatus(
                listingId, ListingStatus.ACTIVE, ListingStatus.SOLD, LocalDateTime.now()) != 1) {
            throw new ConflictException("LISTING_UNAVAILABLE",
                    "Someone else has already bought this item.");
        }

        // 2. Open the escrow. Saved before the debit so the ledger row can point at it.
        Transaction transaction = this.transactionRepository.save(TransactionFactory.createTransaction(
                buyerId, listing.getSellerId(), listingId, price,
                PaymentMethod.WALLET, TransactionStatus.PENDING));

        /*
         3. Take the buyer's money. If this throws - insufficient funds - the whole
            method rolls back, which releases the listing again. That rollback is
            the only thing un-claiming it, so nothing between here and the claim
            may commit independently.
        */
        this.walletService.debit(buyerId, price, REFERENCE_TYPE, transaction.getTransactionId(),
                "Purchase: " + listing.getTitle());

        this.notifications.purchaseMade(listing.getSellerId(), listing.getTitle(), price,
                transaction.getTransactionId());

        return transaction;
    }

    @Transactional
    @Override
    public Transaction confirmReceipt(long buyerId, long transactionId) {
        Transaction transaction = require(transactionId);

        // Only the buyer decides they received it. A seller confirming their own
        // sale would make escrow pointless.
        if (transaction.getBuyerId() != buyerId) {
            throw new ConflictException("NOT_YOUR_PURCHASE",
                    "Only the buyer can confirm they received an item.");
        }

        return release(transaction, "Sale released");
    }

    @Transactional
    @Override
    public Transaction cancel(long actingUserId, long transactionId) {
        Transaction transaction = require(transactionId);

        if (transaction.getBuyerId() != actingUserId && transaction.getSellerId() != actingUserId) {
            throw new ConflictException("NOT_YOUR_TRANSACTION",
                    "You are not part of this transaction.");
        }

        // CAS first: if this loses, someone already confirmed or cancelled, and no
        // money must move.
        if (this.transactionRepository.compareAndSetStatus(transactionId, TransactionStatus.PENDING,
                TransactionStatus.CANCELLED, LocalDateTime.now()) != 1) {
            throw new ConflictException("ALREADY_RESOLVED",
                    "This purchase has already been completed or cancelled.");
        }

        this.walletService.credit(transaction.getBuyerId(), transaction.getAmount(),
                REFERENCE_TYPE, transactionId, "Refund for cancelled purchase");

        // Both sides hear about it: the buyer that their money is back, the seller
        // that the item is on sale again.
        this.notifications.purchaseCancelled(transaction.getBuyerId(), transaction.getAmount(), transactionId);
        this.notifications.purchaseCancelled(transaction.getSellerId(), transaction.getAmount(), transactionId);

        // Back on sale. Only from SOLD, so a listing the seller has since removed
        // or deleted is left alone.
        this.listingRepository.compareAndSetStatus(transaction.getListingId(),
                ListingStatus.SOLD, ListingStatus.ACTIVE, LocalDateTime.now());

        return require(transactionId);
    }

    /**
     * Pays the seller. Shared by the buyer's confirmation and the auto-release job,
     * so both go through the same compare-and-set and can race each other safely.
     */
    @Transactional
    public Transaction release(Transaction transaction, String description) {
        long transactionId = transaction.getTransactionId();

        if (this.transactionRepository.compareAndSetStatus(transactionId, TransactionStatus.PENDING,
                TransactionStatus.COMPLETED, LocalDateTime.now()) != 1) {
            throw new ConflictException("ALREADY_RESOLVED",
                    "This purchase has already been completed or cancelled.");
        }

        this.walletService.credit(transaction.getSellerId(), transaction.getAmount(),
                REFERENCE_TYPE, transactionId, description);

        this.notifications.fundsReleased(transaction.getSellerId(), transaction.getAmount(), transactionId);

        return require(transactionId);
    }

    @Override
    public List<Transaction> historyFor(long userId) {
        return this.transactionRepository.findByBuyerIdOrSellerIdOrderByCreatedAtDesc(userId, userId);
    }

    @Override
    public BigDecimal heldFor(long userId) {
        BigDecimal held = this.transactionRepository.sumHeldForBuyer(userId);
        return held == null ? BigDecimal.ZERO : held;
    }

    private Transaction require(long transactionId) {
        return this.transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Purchase: no such transaction"));
    }

}
