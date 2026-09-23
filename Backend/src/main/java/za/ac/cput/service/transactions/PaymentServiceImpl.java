/*
 PaymentServiceImpl.java

 Business logic for Payment. Implements the generic CRUD contract
 IService<Payment, Long> plus the Payment-specific operations.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.service.transactions;

import java.util.List;

import org.springframework.stereotype.Service;

import za.ac.cput.domain.transactions.Payment;
import za.ac.cput.repository.transactions.PaymentRepository;

@Service
public class PaymentServiceImpl implements IPaymentService {

    private final PaymentRepository repository;

    public PaymentServiceImpl(PaymentRepository repository) {
        this.repository = repository;
    }

    @Override
    public Payment create(Payment payment) {
        return this.repository.save(payment);
    }

    @Override
    public Payment read(Long id) {
        return id == null ? null : this.repository.findById(id).orElse(null);
    }

    @Override
    public Payment update(Payment payment) {
        return this.repository.save(payment);
    }

    @Override
    public boolean delete(Long id) {
        if (id == null || !this.repository.existsById(id)) {
            return false;
        }
        this.repository.deleteById(id);
        return true;
    }

    @Override
    public List<Payment> getAll() {
        return this.repository.findAll();
    }

    @Override
    public List<Payment> findByTransactionId(long transactionId) {
        return this.repository.findByTransactionId(transactionId);
    }

    @Override
    public Payment findByExternalReference(String externalReference) {
        return this.repository.findByExternalReference(externalReference).orElse(null);
    }

}
