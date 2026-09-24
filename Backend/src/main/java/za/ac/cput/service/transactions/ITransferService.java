/*
 ITransferService.java

 Student-to-student wallet transfers.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 24 September 2026
*/

package za.ac.cput.service.transactions;

import java.math.BigDecimal;

import za.ac.cput.dto.transactions.WalletDtos.TransferResult;

public interface ITransferService {

    /**
     * Moves money from the sender's wallet to the recipient's, atomically.
     *
     * @param senderId       always the signed-in user, never taken from the request
     * @param recipientEmail the recipient's student email
     */
    TransferResult send(long senderId, String recipientEmail, BigDecimal amount);

}
