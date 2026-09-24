/*
 TransactionFactory.java

 Factory for Transaction. All construction goes through here so that every
 Transaction is validated with Helper before it exists - the entity itself
 exposes only a Builder and a protected JPA constructor.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.factory.transactions;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import za.ac.cput.domain.enums.PaymentMethod;
import za.ac.cput.domain.enums.TransactionStatus;
import za.ac.cput.domain.transactions.Transaction;
import za.ac.cput.util.Helper;

public class TransactionFactory {

    // Prevent instantiation - factory class
    private TransactionFactory() {}

    public static Transaction createTransaction(long buyerId, long sellerId, long listingId,
                                                BigDecimal amount, PaymentMethod paymentMethod,
                                                TransactionStatus status) {
        if (!Helper.isValidId(buyerId)) {
            throw new IllegalArgumentException("Transaction: buyerId must be a positive id");
        }

        if (!Helper.isValidId(sellerId)) {
            throw new IllegalArgumentException("Transaction: sellerId must be a positive id");
        }

        // The README documents buyer != seller as a database CHECK, but CHECK
        // constraints are only ever emitted at CREATE TABLE, so it does not exist on
        // any database built before it was written down. Enforcing it here means the
        // rule actually holds. Self-dealing would otherwise be a way to manufacture
        // "completed sales" towards a Trusted Seller badge.
        if (buyerId == sellerId) {
            throw new IllegalArgumentException("Transaction: the buyer and seller cannot be the same person");
        }

        if (!Helper.isValidId(listingId)) {
            throw new IllegalArgumentException("Transaction: listingId must be a positive id");
        }

        // isPositiveMoney, not isValidBigDecimal: a zero-amount transaction is not a
        // sale, and a scale above 2 would leave the wallet ledger disagreeing with
        // the DECIMAL(10,2) balance column.
        if (!Helper.isPositiveMoney(amount)) {
            throw new IllegalArgumentException("Transaction: amount must be greater than zero");
        }

        if (!Helper.isValidObject(paymentMethod)) {
            throw new IllegalArgumentException("Transaction: paymentMethod is required");
        }

        if (!Helper.isValidObject(status)) {
            throw new IllegalArgumentException("Transaction: status is required");
        }

        LocalDateTime now = LocalDateTime.now();

        return new Transaction.Builder()
                .setBuyerId(buyerId)
                .setSellerId(sellerId)
                .setListingId(listingId)
                .setAmount(amount)
                .setPaymentMethod(paymentMethod)
                .setStatus(status)
                .setCreatedAt(now)
                .build();
    }

    public static Transaction updateTransaction(Transaction existing, long buyerId, long sellerId,
                                                long listingId, BigDecimal amount,
                                                PaymentMethod paymentMethod, TransactionStatus status) {
        if (!Helper.isValidObject(existing)) {
            throw new IllegalArgumentException("Transaction: existing record is required for an update");
        }

        if (!Helper.isValidId(buyerId)) {
            throw new IllegalArgumentException("Transaction: buyerId must be a positive id");
        }

        if (!Helper.isValidId(sellerId)) {
            throw new IllegalArgumentException("Transaction: sellerId must be a positive id");
        }

        // The README documents buyer != seller as a database CHECK, but CHECK
        // constraints are only ever emitted at CREATE TABLE, so it does not exist on
        // any database built before it was written down. Enforcing it here means the
        // rule actually holds. Self-dealing would otherwise be a way to manufacture
        // "completed sales" towards a Trusted Seller badge.
        if (buyerId == sellerId) {
            throw new IllegalArgumentException("Transaction: the buyer and seller cannot be the same person");
        }

        if (!Helper.isValidId(listingId)) {
            throw new IllegalArgumentException("Transaction: listingId must be a positive id");
        }

        // isPositiveMoney, not isValidBigDecimal: a zero-amount transaction is not a
        // sale, and a scale above 2 would leave the wallet ledger disagreeing with
        // the DECIMAL(10,2) balance column.
        if (!Helper.isPositiveMoney(amount)) {
            throw new IllegalArgumentException("Transaction: amount must be greater than zero");
        }

        if (!Helper.isValidObject(paymentMethod)) {
            throw new IllegalArgumentException("Transaction: paymentMethod is required");
        }

        if (!Helper.isValidObject(status)) {
            throw new IllegalArgumentException("Transaction: status is required");
        }

        return new Transaction.Builder()
                .copy(existing)
                .setBuyerId(buyerId)
                .setSellerId(sellerId)
                .setListingId(listingId)
                .setAmount(amount)
                .setPaymentMethod(paymentMethod)
                .setStatus(status)
                .build();
    }

}
