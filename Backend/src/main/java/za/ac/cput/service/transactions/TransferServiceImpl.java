/*
 TransferServiceImpl.java

 Student-to-student wallet transfers.

 One transaction covers both sides, so a transfer either moves in full or not at
 all - if the sender cannot cover it, a credit that already ran is rolled back
 with the failed debit.

 Wallets are touched in ascending USER id, the lock order WalletRepository
 documents. Two students sending to each other at the same moment would
 otherwise each hold one wallet lock and wait on the other's forever.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 24 September 2026
*/

package za.ac.cput.service.transactions;

import java.math.BigDecimal;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import za.ac.cput.domain.enums.AccountStatus;
import za.ac.cput.domain.identity.User;
import za.ac.cput.domain.transactions.Wallet;
import za.ac.cput.dto.transactions.WalletDtos.TransferResult;
import za.ac.cput.exception.ConflictException;
import za.ac.cput.repository.identity.UserRepository;
import za.ac.cput.util.Helper;

@Service
public class TransferServiceImpl implements ITransferService {

    static final String REFERENCE_TYPE = "TRANSFER";

    private final IWalletService walletService;
    private final UserRepository userRepository;

    public TransferServiceImpl(IWalletService walletService, UserRepository userRepository) {
        this.walletService = walletService;
        this.userRepository = userRepository;
    }

    @Transactional
    @Override
    public TransferResult send(long senderId, String recipientEmail, BigDecimal amount) {
        if (!Helper.isPositiveMoney(amount)) {
            throw new IllegalArgumentException(
                    "Enter an amount greater than zero with at most 2 decimal places");
        }
        if (Helper.isNullOrEmpty(recipientEmail)) {
            throw new IllegalArgumentException("Enter the recipient's student email");
        }

        // Signup only admits lowercase student emails, so lowercase input matches on any collation.
        User recipient = this.userRepository.findByEmail(recipientEmail.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ConflictException("RECIPIENT_NOT_FOUND",
                        "No student with that email"));

        if (recipient.getUserId() == senderId) {
            throw new ConflictException("CANNOT_SEND_TO_SELF", "You cannot send money to yourself");
        }
        if (recipient.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new ConflictException("RECIPIENT_UNAVAILABLE",
                    "That student cannot receive money right now");
        }

        User sender = this.userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Transfer: no such sender"));

        long recipientId = recipient.getUserId();
        String sentDescription = "Sent to " + fullName(recipient);
        String receivedDescription = "Received from " + fullName(sender);

        Wallet senderWallet;
        if (senderId < recipientId) {
            senderWallet = debitSender(senderId, amount, recipientId, sentDescription);
            creditRecipient(recipientId, amount, senderId, receivedDescription);
        } else {
            creditRecipient(recipientId, amount, senderId, receivedDescription);
            senderWallet = debitSender(senderId, amount, recipientId, sentDescription);
        }

        return new TransferResult(amount, fullName(recipient), senderWallet.getBalance());
    }

    private Wallet debitSender(long senderId, BigDecimal amount, long recipientId, String description) {
        return this.walletService.debit(senderId, amount, REFERENCE_TYPE, recipientId, description);
    }

    private void creditRecipient(long recipientId, BigDecimal amount, long senderId, String description) {
        this.walletService.credit(recipientId, amount, REFERENCE_TYPE, senderId, description);
    }

    private static String fullName(User user) {
        return user.getFirstName() + " " + user.getLastName();
    }

}
