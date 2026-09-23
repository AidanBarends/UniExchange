/*
 TransactionRepository.java

 Spring Data JPA repository for the Transaction entity.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.repository.transactions;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import za.ac.cput.domain.enums.TransactionStatus;
import za.ac.cput.domain.transactions.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByBuyerId(long buyerId);

    List<Transaction> findBySellerId(long sellerId);

    List<Transaction> findByListingId(long listingId);

    List<Transaction> findByStatus(TransactionStatus status);

}
