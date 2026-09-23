/*
 TransactionRequest.java

 Inbound payload for creating/updating a Transaction. Entities have no public
 setters, so requests arrive as a record and are handed to TransactionFactory.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.dto.transactions;

import java.math.BigDecimal;

import za.ac.cput.domain.enums.PaymentMethod;
import za.ac.cput.domain.enums.TransactionStatus;

public record TransactionRequest(
        long buyerId,
        long sellerId,
        long listingId,
        BigDecimal amount,
        PaymentMethod paymentMethod,
        TransactionStatus status) {
}
