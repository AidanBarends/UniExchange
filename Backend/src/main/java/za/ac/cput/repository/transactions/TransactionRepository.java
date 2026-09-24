/*
 TransactionRepository.java

 Spring Data JPA repository for the Transaction entity.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.repository.transactions;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import za.ac.cput.domain.enums.TransactionStatus;
import za.ac.cput.domain.transactions.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByBuyerId(long buyerId);

    List<Transaction> findBySellerId(long sellerId);

    List<Transaction> findByListingId(long listingId);

    List<Transaction> findByStatus(TransactionStatus status);

    /*
     Moves a transaction between states, atomically.

     Without this, two concurrent "I received it" clicks both read PENDING and
     both credit the seller - paying them twice out of nothing. The wallet lock
     does not prevent that: both credits are legitimate individually, it is the
     second state change that is not.

     It also makes the ordering safe. The caller runs this FIRST and only touches
     wallets if it returns 1, so "cancel then confirm" cannot pay a seller after
     the buyer has already been refunded.
    */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Transaction t set t.status = :to, t.completedAt = :at " +
           "where t.transactionId = :id and t.status = :from")
    int compareAndSetStatus(@Param("id") long transactionId,
                            @Param("from") TransactionStatus from,
                            @Param("to") TransactionStatus to,
                            @Param("at") LocalDateTime at);

    /**
     * Money this buyer has in escrow: paid for, not yet released to the seller.
     *
     * This is what makes the "no new columns" escrow model work - held funds are
     * derived from transaction state rather than stored on the wallet.
     */
    @Query("select coalesce(sum(t.amount), 0) from Transaction t " +
           "where t.buyerId = :buyerId and t.status = za.ac.cput.domain.enums.TransactionStatus.PENDING")
    BigDecimal sumHeldForBuyer(@Param("buyerId") long buyerId);

    /** Blocks deleting a listing while a buyer's money is still held against it. */
    long countByListingIdAndStatus(long listingId, TransactionStatus status);

    /** Drives the auto-release job. Ids only, so no entity is preloaded into its transaction. */
    @Query("select t.transactionId from Transaction t " +
           "where t.status = za.ac.cput.domain.enums.TransactionStatus.PENDING and t.createdAt < :cutoff")
    List<Long> findPendingIdsCreatedBefore(@Param("cutoff") LocalDateTime cutoff);

    List<Transaction> findByBuyerIdOrSellerIdOrderByCreatedAtDesc(long buyerId, long sellerId);

}
