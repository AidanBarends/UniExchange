/*
 PaymentRequest.java

 Inbound payload for creating/updating a Payment. Entities have no public
 setters, so requests arrive as a record and are handed to PaymentFactory.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.dto.transactions;

import java.math.BigDecimal;

import za.ac.cput.domain.enums.PaymentMethod;
import za.ac.cput.domain.enums.PaymentStatus;

public record PaymentRequest(
        long transactionId,
        BigDecimal amount,
        PaymentMethod method,
        PaymentStatus status,
        String externalReference) {
}
