/*
 IPaymentService.java

 Service contract for Payment.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.service.transactions;

import java.util.List;

import za.ac.cput.domain.transactions.Payment;
import za.ac.cput.service.IService;

public interface IPaymentService extends IService<Payment, Long> {

    List<Payment> findByTransactionId(long transactionId);

    Payment findByExternalReference(String externalReference);

}
