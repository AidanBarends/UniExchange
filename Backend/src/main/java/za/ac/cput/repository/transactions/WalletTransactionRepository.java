/*
 WalletTransactionRepository.java

 Spring Data JPA repository for the WalletTransaction entity.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.repository.transactions;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import za.ac.cput.domain.enums.WalletTransactionType;
import za.ac.cput.domain.transactions.WalletTransaction;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {

    List<WalletTransaction> findByWalletIdOrderByCreatedAtDesc(long walletId);

    List<WalletTransaction> findByWalletIdAndType(long walletId, WalletTransactionType type);

}
