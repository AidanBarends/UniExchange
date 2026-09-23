/*
 WalletTransactionFactory.java

 Factory for WalletTransaction. All construction goes through here so that every
 WalletTransaction is validated with Helper before it exists - the entity itself
 exposes only a Builder and a protected JPA constructor.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.factory.transactions;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import za.ac.cput.domain.enums.WalletTransactionType;
import za.ac.cput.domain.transactions.WalletTransaction;
import za.ac.cput.util.Helper;

public class WalletTransactionFactory {

    // Prevent instantiation - factory class
    private WalletTransactionFactory() {}

    public static WalletTransaction createWalletTransaction(long walletId, WalletTransactionType type,
                                                            BigDecimal amount, BigDecimal balanceAfter,
                                                            String referenceType, Long referenceId,
                                                            String description) {
        if (!Helper.isValidId(walletId)) {
            throw new IllegalArgumentException("WalletTransaction: walletId must be a positive id");
        }

        if (!Helper.isValidObject(type)) {
            throw new IllegalArgumentException("WalletTransaction: type is required");
        }

        if (!Helper.isValidBigDecimal(amount)) {
            throw new IllegalArgumentException("WalletTransaction: amount must be a non-negative amount");
        }

        if (!Helper.isValidBigDecimal(balanceAfter)) {
            throw new IllegalArgumentException("WalletTransaction: balanceAfter must be a non-negative amount");
        }

        if (referenceId != null && !Helper.isValidId(referenceId)) {
            throw new IllegalArgumentException("WalletTransaction: referenceId must be a positive id when supplied");
        }

        LocalDateTime now = LocalDateTime.now();

        return new WalletTransaction.Builder()
                .setWalletId(walletId)
                .setType(type)
                .setAmount(amount)
                .setBalanceAfter(balanceAfter)
                .setReferenceType(referenceType)
                .setReferenceId(referenceId)
                .setDescription(description)
                .setCreatedAt(now)
                .build();
    }

    public static WalletTransaction updateWalletTransaction(WalletTransaction existing, long walletId,
                                                            WalletTransactionType type, BigDecimal amount,
                                                            BigDecimal balanceAfter, String referenceType,
                                                            Long referenceId, String description) {
        if (!Helper.isValidObject(existing)) {
            throw new IllegalArgumentException("WalletTransaction: existing record is required for an update");
        }

        if (!Helper.isValidId(walletId)) {
            throw new IllegalArgumentException("WalletTransaction: walletId must be a positive id");
        }

        if (!Helper.isValidObject(type)) {
            throw new IllegalArgumentException("WalletTransaction: type is required");
        }

        if (!Helper.isValidBigDecimal(amount)) {
            throw new IllegalArgumentException("WalletTransaction: amount must be a non-negative amount");
        }

        if (!Helper.isValidBigDecimal(balanceAfter)) {
            throw new IllegalArgumentException("WalletTransaction: balanceAfter must be a non-negative amount");
        }

        if (referenceId != null && !Helper.isValidId(referenceId)) {
            throw new IllegalArgumentException("WalletTransaction: referenceId must be a positive id when supplied");
        }

        return new WalletTransaction.Builder()
                .copy(existing)
                .setWalletId(walletId)
                .setType(type)
                .setAmount(amount)
                .setBalanceAfter(balanceAfter)
                .setReferenceType(referenceType)
                .setReferenceId(referenceId)
                .setDescription(description)
                .build();
    }

}
