/*
 WalletTransactionRequest.java

 Inbound payload for creating/updating a WalletTransaction. Entities have no public
 setters, so requests arrive as a record and are handed to WalletTransactionFactory.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.dto.transactions;

import java.math.BigDecimal;

import za.ac.cput.domain.enums.WalletTransactionType;

public record WalletTransactionRequest(
        long walletId,
        WalletTransactionType type,
        BigDecimal amount,
        BigDecimal balanceAfter,
        String referenceType,
        Long referenceId,
        String description) {
}
