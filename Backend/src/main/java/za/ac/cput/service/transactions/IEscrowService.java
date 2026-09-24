/*
 IEscrowService.java

 Buying a listing with wallet money, held in escrow until the buyer confirms.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 24 September 2026
*/

package za.ac.cput.service.transactions;

import java.math.BigDecimal;
import java.util.List;

import za.ac.cput.domain.transactions.Transaction;

public interface IEscrowService {

    /**
     * Buys a listing: claims it, debits the buyer, and opens a PENDING transaction.
     * The seller is NOT paid yet.
     *
     * @param expectedAmount what the buyer was shown, re-checked against the live
     *                       listing price so a seller cannot edit it mid-purchase
     */
    Transaction purchase(long buyerId, long listingId, BigDecimal expectedAmount);

    /** Buyer confirms receipt: the seller is paid and the transaction completes. */
    Transaction confirmReceipt(long buyerId, long transactionId);

    /**
     * Cancels an open escrow and refunds the buyer. Either party may do this while
     * the transaction is still PENDING; the listing goes back on sale.
     */
    Transaction cancel(long actingUserId, long transactionId);

    /** Everything this user bought or sold, newest first. */
    List<Transaction> historyFor(long userId);

    /** Total the user has paid but not yet released. */
    BigDecimal heldFor(long userId);

}
