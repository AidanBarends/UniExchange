/*
 IWalletService.java

 Service contract for Wallet.

 Note that credit/debit are keyed on USER id, not wallet id. That is deliberate
 and load-bearing: the locking read is findByUserIdForUpdate, and locking two
 wallets in a deterministic order requires an ordering key that is known before
 any row is read. Taking a walletId here would force callers to look the wallet
 up first, and that unlocked read is exactly what makes the subsequent lock
 return a stale balance. See WalletRepository for the full rule.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.service.transactions;

import java.math.BigDecimal;

import za.ac.cput.domain.transactions.Wallet;
import za.ac.cput.service.IService;

public interface IWalletService extends IService<Wallet, Long> {

    /** Unlocked read, for display only. Null when the user has no wallet yet. */
    Wallet findByUserId(long userId);

    /**
     * The wallet for this user, creating an empty ZAR one if it does not exist.
     * Every money path uses this rather than findByUserId, because a null wallet
     * on a release would otherwise silently swallow the buyer's funds.
     */
    Wallet getOrCreateForUser(long userId);

    /**
     * Adds money. referenceType/referenceId tie the ledger row back to what caused
     * it (e.g. "TRANSACTION" + transactionId, or "TOPUP" + topUpId) so the ledger
     * can be reconciled against transactions and top-ups.
     *
     * @throws IllegalArgumentException if the amount is not positive with <= 2 decimals
     */
    Wallet credit(long userId, BigDecimal amount, String referenceType, Long referenceId, String description);

    /**
     * Removes money.
     *
     * @throws IllegalArgumentException                     if the amount is not positive with <= 2 decimals
     * @throws za.ac.cput.exception.InsufficientFundsException if the balance would go negative
     */
    Wallet debit(long userId, BigDecimal amount, String referenceType, Long referenceId, String description);

}
