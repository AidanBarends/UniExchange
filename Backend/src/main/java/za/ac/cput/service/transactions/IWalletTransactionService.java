/*
 IWalletTransactionService.java

 Service contract for WalletTransaction.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.service.transactions;

import java.util.List;

import za.ac.cput.domain.transactions.WalletTransaction;
import za.ac.cput.service.IService;

public interface IWalletTransactionService extends IService<WalletTransaction, Long> {

    List<WalletTransaction> findByWalletId(long walletId);

}
