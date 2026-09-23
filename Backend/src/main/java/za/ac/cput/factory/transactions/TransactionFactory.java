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

        if (!Helper.isValidId(listingId)) {
            throw new IllegalArgumentException("Transaction: listingId must be a positive id");
        }

        if (!Helper.isValidBigDecimal(amount)) {
            throw new IllegalArgumentException("Transaction: amount must be a non-negative amount");
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

        if (!Helper.isValidId(listingId)) {
            throw new IllegalArgumentException("Transaction: listingId must be a positive id");
        }

        if (!Helper.isValidBigDecimal(amount)) {
            throw new IllegalArgumentException("Transaction: amount must be a non-negative amount");
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
