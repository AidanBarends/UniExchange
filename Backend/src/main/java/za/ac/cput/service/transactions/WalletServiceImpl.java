/*
 WalletServiceImpl.java

 Business logic for Wallet. Implements the generic CRUD contract
 IService<Wallet, Long> plus the Wallet-specific operations.

 This is the only class in the application that may change a balance. Every
 change goes through applyMovement, which does four things in a fixed order:
 lock the row, re-read it, check the result, and write both the new balance and
 a matching ledger row. Anything that skips one of those steps is a money bug.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.service.transactions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import za.ac.cput.domain.enums.WalletTransactionType;
import za.ac.cput.domain.transactions.Wallet;
import za.ac.cput.exception.InsufficientFundsException;
import za.ac.cput.factory.transactions.WalletFactory;
import za.ac.cput.factory.transactions.WalletTransactionFactory;
import za.ac.cput.repository.transactions.WalletRepository;
import za.ac.cput.repository.transactions.WalletTransactionRepository;
import za.ac.cput.util.Helper;

@Service
public class WalletServiceImpl implements IWalletService {

    /** Every wallet is ZAR. The column exists for future-proofing, not for a choice. */
    private static final String DEFAULT_CURRENCY = "ZAR";

    private final WalletRepository repository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final EntityManager entityManager;

    public WalletServiceImpl(WalletRepository repository,
                             WalletTransactionRepository walletTransactionRepository,
                             EntityManager entityManager) {
        this.repository = repository;
        this.walletTransactionRepository = walletTransactionRepository;
        this.entityManager = entityManager;
    }

    @Override
    public Wallet create(Wallet wallet) {
        return this.repository.save(wallet);
    }

    @Override
    public Wallet read(Long id) {
        return id == null ? null : this.repository.findById(id).orElse(null);
    }

    @Override
    public Wallet update(Wallet wallet) {
        return this.repository.save(wallet);
    }

    @Override
    public boolean delete(Long id) {
        if (id == null || !this.repository.existsById(id)) {
            return false;
        }
        this.repository.deleteById(id);
        return true;
    }

    @Override
    public List<Wallet> getAll() {
        return this.repository.findAll();
    }

    @Override
    public Wallet findByUserId(long userId) {
        return this.repository.findByUserId(userId).orElse(null);
    }

    /*
     Wallets are created at registration, so this is a safety net rather than the
     normal path - but it has to exist, because a null wallet on the release side
     of an escrow would otherwise mean the buyer is debited and the seller is
     never credited.

     The insert races: two concurrent calls for the same new user both see no row
     and both insert. wallet.user_id is UNIQUE, so one of them loses with a
     DataIntegrityViolationException; catching it and re-reading is correct and
     cheaper than locking. Deliberately NOT done with a locking read on a missing
     row - that takes a gap lock in InnoDB and deadlocks against adjacent user ids.
    */
    @Transactional
    @Override
    public Wallet getOrCreateForUser(long userId) {
        if (!Helper.isValidId(userId)) {
            throw new IllegalArgumentException("Wallet: userId must be a positive id");
        }
        return this.repository.findByUserId(userId).orElseGet(() -> {
            try {
                return this.repository.saveAndFlush(
                        WalletFactory.createWallet(userId, BigDecimal.ZERO, DEFAULT_CURRENCY));
            } catch (DataIntegrityViolationException lostTheRace) {
                return this.repository.findByUserId(userId).orElseThrow(() -> lostTheRace);
            }
        });
    }

    @Transactional
    @Override
    public Wallet credit(long userId, BigDecimal amount, String referenceType,
                         Long referenceId, String description) {
        requirePositive(amount);
        return applyMovement(userId, amount, WalletTransactionType.CREDIT,
                referenceType, referenceId, description);
    }

    @Transactional
    @Override
    public Wallet debit(long userId, BigDecimal amount, String referenceType,
                        Long referenceId, String description) {
        requirePositive(amount);
        return applyMovement(userId, amount.negate(), WalletTransactionType.DEBIT,
                referenceType, referenceId, description);
    }

    /*
     Guards the bug this method used to have: debit() negates its argument, so a
     NEGATIVE amount became a positive delta and quietly increased the balance
     while writing a ledger row labelled DEBIT. isValidBigDecimal did not catch
     it because it only rejects values below zero - and after negation the value
     was above zero. Direction belongs to the method, never to the sign.
    */
    private static void requirePositive(BigDecimal amount) {
        if (!Helper.isPositiveMoney(amount)) {
            throw new IllegalArgumentException(
                    "Wallet: amount must be greater than zero with at most 2 decimal places");
        }
    }

    private Wallet applyMovement(long userId, BigDecimal delta, WalletTransactionType type,
                                 String referenceType, Long referenceId, String description) {

        Wallet wallet = getOrCreateForUser(userId);

        /*
         Take the row lock. On its own this is NOT enough: if anything earlier in
         this transaction already loaded the wallet, Hibernate returns that cached
         instance and the FOR UPDATE select never overwrites its stale balance -
         so we would hold a lock and still lose an update. refresh() is the only
         call that both locks and re-reads, which is why it is here rather than a
         second findByUserIdForUpdate.
        */
        this.entityManager.refresh(wallet, LockModeType.PESSIMISTIC_WRITE);

        // setScale keeps balanceAfter and the DECIMAL(10,2) column in agreement;
        // without it a 3-decimal input leaves the ledger disagreeing with the balance.
        BigDecimal newBalance = wallet.getBalance().add(delta).setScale(2, RoundingMode.HALF_UP);
        if (newBalance.signum() < 0) {
            throw new InsufficientFundsException(wallet.getBalance(), delta.abs());
        }

        Wallet saved = this.repository.save(new Wallet.Builder()
                .copy(wallet)
                .setBalance(newBalance)
                .setUpdatedAt(LocalDateTime.now())
                .build());

        this.walletTransactionRepository.save(WalletTransactionFactory.createWalletTransaction(
                saved.getWalletId(), type, delta.abs(), newBalance,
                referenceType, referenceId, description));

        return saved;
    }

}
