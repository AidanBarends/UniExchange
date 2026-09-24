/*
 TransferServiceTest.java

 Student-to-student transfers: money moves in full or not at all, and two
 students sending to each other at once cannot deadlock or lose money.

 @SpringBootTest without @Transactional for the same reason as EscrowServiceTest -
 a shared transaction would hide the concurrency this is meant to catch.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 24 September 2026
*/

package za.ac.cput.service.transactions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import za.ac.cput.domain.enums.AccountStatus;
import za.ac.cput.domain.enums.WalletTransactionType;
import za.ac.cput.domain.identity.User;
import za.ac.cput.domain.transactions.WalletTransaction;
import za.ac.cput.dto.transactions.WalletDtos.TransferResult;
import za.ac.cput.exception.ConflictException;
import za.ac.cput.exception.InsufficientFundsException;
import za.ac.cput.factory.identity.UserFactory;
import za.ac.cput.repository.identity.UserRepository;
import za.ac.cput.repository.transactions.WalletTransactionRepository;

@SpringBootTest
class TransferServiceTest {

    private static final AtomicInteger NEXT = new AtomicInteger();

    @Autowired
    private ITransferService transferService;

    @Autowired
    private IWalletService walletService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletTransactionRepository ledgerRepository;

    private User student(String firstName, AccountStatus status) {
        String email = "transfer" + NEXT.incrementAndGet() + "t" + System.nanoTime() + "@mycput.ac.za";
        return this.userRepository.save(UserFactory.createUser(
                email, firstName, null, "Tester", null, "hash", null, status, null));
    }

    private User fundedStudent(String firstName, String amount) {
        User user = student(firstName, AccountStatus.ACTIVE);
        this.walletService.credit(user.getUserId(), new BigDecimal(amount), "TEST", null, "seed");
        return user;
    }

    private BigDecimal balanceOf(User user) {
        return this.walletService.getOrCreateForUser(user.getUserId()).getBalance();
    }

    private List<WalletTransaction> transferRowsOf(User user) {
        long walletId = this.walletService.getOrCreateForUser(user.getUserId()).getWalletId();
        return this.ledgerRepository.findByWalletIdOrderByCreatedAtDesc(walletId).stream()
                .filter(row -> TransferServiceImpl.REFERENCE_TYPE.equals(row.getReferenceType()))
                .toList();
    }

    @Test
    void movesMoneyAndRecordsBothSides() {
        User alice = fundedStudent("Alice", "200.00");
        User bob = student("Bob", AccountStatus.ACTIVE);

        TransferResult result = this.transferService.send(
                alice.getUserId(), bob.getEmail(), new BigDecimal("75.50"));

        assertEquals(0, new BigDecimal("124.50").compareTo(result.balanceAfter()));
        assertEquals("Bob Tester", result.recipientName());
        assertEquals(0, new BigDecimal("124.50").compareTo(balanceOf(alice)));
        assertEquals(0, new BigDecimal("75.50").compareTo(balanceOf(bob)));

        List<WalletTransaction> sent = transferRowsOf(alice);
        assertEquals(1, sent.size());
        assertEquals(WalletTransactionType.DEBIT, sent.get(0).getType());
        assertEquals("Sent to Bob Tester", sent.get(0).getDescription());

        List<WalletTransaction> received = transferRowsOf(bob);
        assertEquals(1, received.size());
        assertEquals(WalletTransactionType.CREDIT, received.get(0).getType());
        assertEquals("Received from Alice Tester", received.get(0).getDescription());
    }

    @Test
    void emailLookupIgnoresCaseAndWhitespace() {
        User alice = fundedStudent("Alice", "10.00");
        User bob = student("Bob", AccountStatus.ACTIVE);

        this.transferService.send(alice.getUserId(), "  " + bob.getEmail().toUpperCase() + " ",
                new BigDecimal("10.00"));

        assertEquals(0, new BigDecimal("10.00").compareTo(balanceOf(bob)));
    }

    @Test
    void insufficientFundsMovesNothingOnEitherSide() {
        // Exercise both call orders: the credit may run BEFORE the failing debit.
        User lowId = fundedStudent("Low", "20.00");
        User highId = fundedStudent("High", "20.00");

        assertThrows(InsufficientFundsException.class, () -> this.transferService.send(
                lowId.getUserId(), highId.getEmail(), new BigDecimal("50.00")));
        assertThrows(InsufficientFundsException.class, () -> this.transferService.send(
                highId.getUserId(), lowId.getEmail(), new BigDecimal("50.00")));

        assertEquals(0, new BigDecimal("20.00").compareTo(balanceOf(lowId)));
        assertEquals(0, new BigDecimal("20.00").compareTo(balanceOf(highId)));
        assertEquals(0, transferRowsOf(lowId).size());
        assertEquals(0, transferRowsOf(highId).size());
    }

    @Test
    void rejectsBadRecipients() {
        User alice = fundedStudent("Alice", "100.00");
        User suspended = student("Sus", AccountStatus.SUSPENDED);

        ConflictException self = assertThrows(ConflictException.class, () -> this.transferService.send(
                alice.getUserId(), alice.getEmail(), new BigDecimal("1.00")));
        assertEquals("CANNOT_SEND_TO_SELF", self.getCode());

        ConflictException unknown = assertThrows(ConflictException.class, () -> this.transferService.send(
                alice.getUserId(), "000000000@mycput.ac.za", new BigDecimal("1.00")));
        assertEquals("RECIPIENT_NOT_FOUND", unknown.getCode());

        ConflictException unavailable = assertThrows(ConflictException.class, () -> this.transferService.send(
                alice.getUserId(), suspended.getEmail(), new BigDecimal("1.00")));
        assertEquals("RECIPIENT_UNAVAILABLE", unavailable.getCode());

        assertEquals(0, new BigDecimal("100.00").compareTo(balanceOf(alice)));
    }

    @Test
    void rejectsBadAmounts() {
        User alice = fundedStudent("Alice", "100.00");
        User bob = student("Bob", AccountStatus.ACTIVE);

        for (String bad : List.of("0", "-5.00", "1.005")) {
            assertThrows(IllegalArgumentException.class, () -> this.transferService.send(
                    alice.getUserId(), bob.getEmail(), new BigDecimal(bad)));
        }
        assertThrows(IllegalArgumentException.class, () -> this.transferService.send(
                alice.getUserId(), bob.getEmail(), null));

        assertEquals(0, new BigDecimal("100.00").compareTo(balanceOf(alice)));
    }

    @Test
    void opposingConcurrentTransfersNeitherDeadlockNorLoseMoney() throws Exception {
        User alice = fundedStudent("Alice", "100.00");
        User bob = fundedStudent("Bob", "100.00");
        int rounds = 20;

        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < rounds; i++) {
                futures.add(pool.submit(() -> this.transferService.send(
                        alice.getUserId(), bob.getEmail(), new BigDecimal("1.00"))));
                futures.add(pool.submit(() -> this.transferService.send(
                        bob.getUserId(), alice.getEmail(), new BigDecimal("1.00"))));
            }
            for (Future<?> future : futures) {
                future.get();
            }
        } finally {
            pool.shutdown();
        }

        assertEquals(0, new BigDecimal("100.00").compareTo(balanceOf(alice)));
        assertEquals(0, new BigDecimal("100.00").compareTo(balanceOf(bob)));
    }

}
