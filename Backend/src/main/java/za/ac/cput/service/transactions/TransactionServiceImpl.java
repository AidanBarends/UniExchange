/*
 TransactionServiceImpl.java

 Business logic for Transaction. Implements the generic CRUD contract
 IService<Transaction, Long> plus the Transaction-specific operations.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.service.transactions;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import za.ac.cput.domain.enums.TransactionStatus;
import za.ac.cput.domain.transactions.Transaction;
import za.ac.cput.repository.transactions.TransactionRepository;

@Service
public class TransactionServiceImpl implements ITransactionService {

    private final TransactionRepository repository;

    public TransactionServiceImpl(TransactionRepository repository) {
        this.repository = repository;
    }

    @Override
    public Transaction create(Transaction transaction) {
        return this.repository.save(transaction);
    }

    @Override
    public Transaction read(Long id) {
        return id == null ? null : this.repository.findById(id).orElse(null);
    }

    @Override
    public Transaction update(Transaction transaction) {
        return this.repository.save(transaction);
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
    public List<Transaction> getAll() {
        return this.repository.findAll();
    }

    @Override
    public List<Transaction> findByBuyerId(long buyerId) {
        return this.repository.findByBuyerId(buyerId);
    }

    @Override
    public List<Transaction> findBySellerId(long sellerId) {
        return this.repository.findBySellerId(sellerId);
    }

    @Override
    public Transaction complete(Long transactionId) {
        Transaction found = read(transactionId);
        if (found == null) {
            return null;
        }
        return this.repository.save(new Transaction.Builder()
                .copy(found)
                .setStatus(TransactionStatus.COMPLETED)
                .setCompletedAt(LocalDateTime.now())
                .build());
    }

}
