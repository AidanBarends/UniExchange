/*
 WalletTransactionServiceImpl.java

 Business logic for WalletTransaction. Implements the generic CRUD contract
 IService<WalletTransaction, Long> plus the WalletTransaction-specific operations.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.service.transactions;

import java.util.List;

import org.springframework.stereotype.Service;

import za.ac.cput.domain.transactions.WalletTransaction;
import za.ac.cput.repository.transactions.WalletTransactionRepository;

@Service
public class WalletTransactionServiceImpl implements IWalletTransactionService {

    private final WalletTransactionRepository repository;

    public WalletTransactionServiceImpl(WalletTransactionRepository repository) {
        this.repository = repository;
    }

    @Override
    public WalletTransaction create(WalletTransaction walletTransaction) {
        return this.repository.save(walletTransaction);
    }

    @Override
    public WalletTransaction read(Long id) {
        return id == null ? null : this.repository.findById(id).orElse(null);
    }

    @Override
    public WalletTransaction update(WalletTransaction walletTransaction) {
        return this.repository.save(walletTransaction);
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
    public List<WalletTransaction> getAll() {
        return this.repository.findAll();
    }

    @Override
    public List<WalletTransaction> findByWalletId(long walletId) {
        return this.repository.findByWalletIdOrderByCreatedAtDesc(walletId);
    }

}
