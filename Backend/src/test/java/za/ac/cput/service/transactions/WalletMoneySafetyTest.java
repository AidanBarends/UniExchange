/*
 WalletMoneySafetyTest.java

 The money-losing cases. Each of these corresponds to a real defect that either
 existed in this codebase or would have been introduced by the escrow work.

 @SpringBootTest, and deliberately WITHOUT @Transactional: a @DataJpaTest wraps
 everything in one transaction on one connection, so two "concurrent" debits would
 serialise for free and the double-spend test would pass no matter how broken the
 locking was. Real contention needs real threads and real transactions.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 24 September 2026
*/

package za.ac.cput.service.transactions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

import za.ac.cput.domain.transactions.Wallet;
import za.ac.cput.domain.transactions.WalletTransaction;
import za.ac.cput.exception.InsufficientFundsException;
import za.ac.cput.repository.transactions.WalletRepository;
import za.ac.cput.repository.transactions.WalletTransactionRepository;

@SpringBootTest
class WalletMoneySafetyTest {

    private static final AtomicInteger NEXT_USER_ID = new AtomicInteger(900_000);

    @Autowired
    private IWalletService walletService;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletTransactionRepository ledgerRepository;

    private long freshUser() {
        return NEXT_USER_ID.incrementAndGet();
    }

    /*
     The bug this guards against was live: debit() negates its argument before the
     balance check, so a NEGATIVE amount became a positive delta and INCREASED the
     balance - while writing a ledger row labelled DEBIT. Combined with the old
     POST /api/wallets/{id}/debit?amount=-999999 being open to any signed-in
     student, that was self-service free money.
    */
    @Test
    void refusesANegativeDebit() {
        long userId = freshUser();
        this.walletService.credit(userId, new BigDecimal("100.00"), "TEST", null, "seed");

        assertThrows(IllegalArgumentException.class, () ->
                this.walletService.debit(userId, new BigDecimal("-500.00"), "TEST", null, "attack"));

        assertEquals(0, new BigDecimal("100.00")
                .compareTo(this.walletService.findByUserId(userId).getBalance()),
                "a rejected debit must not change the balance");
    }

    @Test
    void refusesZeroAndOverPreciseAmounts() {
        long userId = freshUser();

        assertThrows(IllegalArgumentException.class, () ->
                this.walletService.credit(userId, BigDecimal.ZERO, "TEST", null, "zero"));

        // Three decimals would make balanceAfter in the ledger disagree with the
        // DECIMAL(10,2) balance column, and the wallet would stop reconciling.
        assertThrows(IllegalArgumentException.class, () ->
                this.walletService.credit(userId, new BigDecimal("10.005"), "TEST", null, "scale"));
    }

    @Test
    void refusesToOverdraw() {
        long userId = freshUser();
        this.walletService.credit(userId, new BigDecimal("50.00"), "TEST", null, "seed");

        assertThrows(InsufficientFundsException.class, () ->
                this.walletService.debit(userId, new BigDecimal("50.01"), "TEST", null, "too much"));
    }

    /*
     The double-spend test.

     Ten threads each try to take R80 from a R100 balance. Exactly one may succeed:
     without SELECT ... FOR UPDATE they all read 100, all compute 20, and all write
     it - the student spends R800 they never had.
    */
    @Test
    void concurrentDebitsCannotOverdraw() throws Exception {
        long userId = freshUser();
        this.walletService.credit(userId, new BigDecimal("100.00"), "TEST", null, "seed");

        int threads = 10;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            List<Callable<Boolean>> attempts = java.util.Collections.nCopies(threads, () -> {
                try {
                    this.walletService.debit(userId, new BigDecimal("80.00"), "TEST", null, "race");
                    return true;
                } catch (InsufficientFundsException expected) {
                    return false;
                }
            });

            long succeeded = 0;
            for (Future<Boolean> result : pool.invokeAll(attempts)) {
                if (result.get()) succeeded++;
            }

            assertEquals(1, succeeded, "exactly one R80 debit may succeed against a R100 balance");
        } finally {
            pool.shutdownNow();
        }

        Wallet wallet = this.walletService.findByUserId(userId);
        assertEquals(0, new BigDecimal("20.00").compareTo(wallet.getBalance()));
        assertTrue(wallet.getBalance().signum() >= 0, "a balance must never go negative");
    }

    /**
     * The ledger is the audit trail behind the balance, so every row's balanceAfter
     * must match what the wallet actually held at that moment. If these drift, the
     * wallet cannot be reconciled and a bug becomes undetectable.
     */
    @Test
    void ledgerBalanceAfterTracksTheWallet() {
        long userId = freshUser();
        this.walletService.credit(userId, new BigDecimal("200.00"), "TEST", null, "one");
        this.walletService.debit(userId, new BigDecimal("75.50"), "TEST", null, "two");
        this.walletService.credit(userId, new BigDecimal("10.25"), "TEST", null, "three");

        Wallet wallet = this.walletService.findByUserId(userId);
        List<WalletTransaction> ledger =
                this.ledgerRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getWalletId());

        assertEquals(0, new BigDecimal("134.75").compareTo(wallet.getBalance()));
        assertEquals(0, wallet.getBalance().compareTo(ledger.get(0).getBalanceAfter()),
                "the newest ledger row must agree with the current balance");
    }

    @Test
    void creatingAWalletIsIdempotent() {
        long userId = freshUser();
        Wallet first = this.walletService.getOrCreateForUser(userId);
        Wallet second = this.walletService.getOrCreateForUser(userId);

        assertEquals(first.getWalletId(), second.getWalletId());
        assertEquals(1, this.walletRepository.findAll().stream()
                .filter(w -> w.getUserId() == userId).count());
    }

}
