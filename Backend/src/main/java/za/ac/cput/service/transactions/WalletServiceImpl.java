/*
 WalletServiceImpl.java

 Business logic for Wallet. Implements the generic CRUD contract
 IService<Wallet, Long> plus the Wallet-specific operations.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.service.transactions;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import za.ac.cput.domain.enums.WalletTransactionType;
import za.ac.cput.domain.transactions.Wallet;
import za.ac.cput.factory.transactions.WalletTransactionFactory;
import za.ac.cput.repository.transactions.WalletRepository;
import za.ac.cput.repository.transactions.WalletTransactionRepository;
import za.ac.cput.util.Helper;

@Service
public class WalletServiceImpl implements IWalletService {

    private final WalletRepository repository;
    private final WalletTransactionRepository walletTransactionRepository;

    public WalletServiceImpl(WalletRepository repository,
                             WalletTransactionRepository walletTransactionRepository) {
        this.repository = repository;
        this.walletTransactionRepository = walletTransactionRepository;
    }

    @Override
    public Wallet create(Wallet wallet) {
        return this.repository.save(wallet);
    }

    @Override
    public Wallet read(Long id) {
        return id == null ? null : this.repository.findById(id).orElse(null);
    }

    @Override
    public Wallet update(Wallet wallet) {
        return this.repository.save(wallet);
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
    public List<Wallet> getAll() {
        return this.repository.findAll();
    }

    @Override
    public Wallet findByUserId(long userId) {
        return this.repository.findByUserId(userId).orElse(null);
    }

    @Transactional
    @Override
    public Wallet credit(Long walletId, BigDecimal amount, String description) {
        return applyMovement(walletId, amount, WalletTransactionType.CREDIT, description);
    }

    @Transactional
    @Override
    public Wallet debit(Long walletId, BigDecimal amount, String description) {
        return applyMovement(walletId, amount.negate(), WalletTransactionType.DEBIT, description);
    }

    private Wallet applyMovement(Long walletId, BigDecimal delta,
                                 WalletTransactionType type, String description) {
        Wallet wallet = read(walletId);
        if (wallet == null) {
            return null;
        }
        BigDecimal newBalance = wallet.getBalance().add(delta);
        if (!Helper.isValidBigDecimal(newBalance)) {
            throw new IllegalArgumentException("Wallet: insufficient funds");
        }

        Wallet saved = this.repository.save(new Wallet.Builder()
                .copy(wallet)
                .setBalance(newBalance)
                .setUpdatedAt(LocalDateTime.now())
                .build());

        this.walletTransactionRepository.save(WalletTransactionFactory.createWalletTransaction(
                saved.getWalletId(), type, delta.abs(), newBalance,
                "WALLET", saved.getWalletId(), description));

        return saved;
    }

}
