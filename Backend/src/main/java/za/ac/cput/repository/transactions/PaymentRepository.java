/*
 PaymentRepository.java

 Spring Data JPA repository for the Payment entity.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.repository.transactions;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import za.ac.cput.domain.enums.PaymentStatus;
import za.ac.cput.domain.transactions.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByTransactionId(long transactionId);

    List<Payment> findByStatus(PaymentStatus status);

    Optional<Payment> findByExternalReference(String externalReference);

}
