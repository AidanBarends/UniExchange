/*
 WalletTopUpRepository.java

 Spring Data JPA repository for the WalletTopUp entity.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 24 September 2026
*/

package za.ac.cput.repository.transactions;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import za.ac.cput.domain.enums.PaymentStatus;
import za.ac.cput.domain.transactions.WalletTopUp;

@Repository
public interface WalletTopUpRepository extends JpaRepository<WalletTopUp, Long> {

    Optional<WalletTopUp> findByMerchantPaymentId(String merchantPaymentId);

    List<WalletTopUp> findByUserIdOrderByCreatedAtDesc(long userId);

    /*
     Marks a top-up paid, once and only once.

     PayFast delivers the same notification more than once by design, and may
     deliver two concurrently. A read-then-write would let both see PENDING and
     both credit the wallet - the student pays once and receives the money twice.

     The row count is the authority: exactly one caller gets 1, everyone else gets
     0 and must do nothing at all.
    */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update WalletTopUp t set t.status = :to, t.pfPaymentId = :pfPaymentId, t.completedAt = :at " +
           "where t.merchantPaymentId = :merchantPaymentId and t.status = :from")
    int compareAndSetStatus(@Param("merchantPaymentId") String merchantPaymentId,
                            @Param("from") PaymentStatus from,
                            @Param("to") PaymentStatus to,
                            @Param("pfPaymentId") String pfPaymentId,
                            @Param("at") LocalDateTime at);

    /** Backs the reconciliation check: every cent in the system came from a completed top-up. */
    @Query("select coalesce(sum(t.amount), 0) from WalletTopUp t " +
           "where t.status = za.ac.cput.domain.enums.PaymentStatus.COMPLETED")
    BigDecimal sumCompleted();

}
