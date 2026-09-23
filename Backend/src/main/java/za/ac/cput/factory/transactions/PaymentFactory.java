/*
 PaymentFactory.java

 Factory for Payment. All construction goes through here so that every
 Payment is validated with Helper before it exists - the entity itself
 exposes only a Builder and a protected JPA constructor.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.factory.transactions;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import za.ac.cput.domain.enums.PaymentMethod;
import za.ac.cput.domain.enums.PaymentStatus;
import za.ac.cput.domain.transactions.Payment;
import za.ac.cput.util.Helper;

public class PaymentFactory {

    // Prevent instantiation - factory class
    private PaymentFactory() {}

    public static Payment createPayment(long transactionId, BigDecimal amount, PaymentMethod method,
                                        PaymentStatus status, String externalReference) {
        if (!Helper.isValidId(transactionId)) {
            throw new IllegalArgumentException("Payment: transactionId must be a positive id");
        }

        if (!Helper.isValidBigDecimal(amount)) {
            throw new IllegalArgumentException("Payment: amount must be a non-negative amount");
        }

        if (!Helper.isValidObject(method)) {
            throw new IllegalArgumentException("Payment: method is required");
        }

        if (!Helper.isValidObject(status)) {
            throw new IllegalArgumentException("Payment: status is required");
        }

        LocalDateTime now = LocalDateTime.now();

        return new Payment.Builder()
                .setTransactionId(transactionId)
                .setAmount(amount)
                .setMethod(method)
                .setStatus(status)
                .setExternalReference(externalReference)
                .setCreatedAt(now)
                .build();
    }

    public static Payment updatePayment(Payment existing, long transactionId, BigDecimal amount,
                                        PaymentMethod method, PaymentStatus status, String externalReference) {
        if (!Helper.isValidObject(existing)) {
            throw new IllegalArgumentException("Payment: existing record is required for an update");
        }

        if (!Helper.isValidId(transactionId)) {
            throw new IllegalArgumentException("Payment: transactionId must be a positive id");
        }

        if (!Helper.isValidBigDecimal(amount)) {
            throw new IllegalArgumentException("Payment: amount must be a non-negative amount");
        }

        if (!Helper.isValidObject(method)) {
            throw new IllegalArgumentException("Payment: method is required");
        }

        if (!Helper.isValidObject(status)) {
            throw new IllegalArgumentException("Payment: status is required");
        }

        return new Payment.Builder()
                .copy(existing)
                .setTransactionId(transactionId)
                .setAmount(amount)
                .setMethod(method)
                .setStatus(status)
                .setExternalReference(externalReference)
                .build();
    }

}
