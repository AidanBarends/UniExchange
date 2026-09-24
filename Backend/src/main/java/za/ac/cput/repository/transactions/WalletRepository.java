/*
 WalletRepository.java

 Spring Data JPA repository for the Wallet entity.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.repository.transactions;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import za.ac.cput.domain.transactions.Wallet;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    /*
     Plain, UNLOCKED read. Safe for display only.

     Never call this on a path that goes on to change the balance: it puts the
     Wallet into the persistence context, and a later locking read in the same
     transaction then returns that same cached instance with its STALE balance
     (see findByUserIdForUpdate). Read-then-write through this method is a lost
     update even when a lock is held.
    */
    Optional<Wallet> findByUserId(long userId);

    /*
     The ONLY way to read a wallet that is about to change. Issues
     SELECT ... FOR UPDATE, so concurrent movers on the same wallet serialise
     rather than both computing from the same starting balance.

     Two rules go with it, and both are easy to get wrong:

       1. This must be the FIRST touch of the row in the transaction. Hibernate
          will not refresh an entity already in the persistence context, so if
          anything loaded this wallet earlier the lock is taken at the database
          but the object handed back still carries the old balance. WalletServiceImpl
          calls entityManager.refresh(..., PESSIMISTIC_WRITE) right after this as
          insurance against that.

       2. When a single transaction must lock two wallets, lock them in ascending
          USER id - not wallet id. Ordering by walletId would force an unlocked
          read just to discover it, which is exactly the L1-cache poisoning rule 1
          forbids. wallet.user_id is UNIQUE NOT NULL, so it is a valid total order.

     Lock order across the whole escrow flow is: listing, then transaction, then
     wallets by ascending userId. See EscrowServiceImpl.
    */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from Wallet w where w.userId = :userId")
    Optional<Wallet> findByUserIdForUpdate(@Param("userId") long userId);

    boolean existsByUserId(long userId);

}
