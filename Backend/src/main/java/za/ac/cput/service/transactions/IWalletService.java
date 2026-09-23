/*
 IWalletService.java

 Service contract for Wallet.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.service.transactions;

import java.math.BigDecimal;

import za.ac.cput.domain.transactions.Wallet;
import za.ac.cput.service.IService;

public interface IWalletService extends IService<Wallet, Long> {

    Wallet findByUserId(long userId);

    Wallet credit(Long walletId, BigDecimal amount, String description);

    Wallet debit(Long walletId, BigDecimal amount, String description);

}
