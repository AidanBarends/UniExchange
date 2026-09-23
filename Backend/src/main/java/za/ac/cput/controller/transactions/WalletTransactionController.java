/*
 WalletTransactionController.java

 REST endpoints for WalletTransaction.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.controller.transactions;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import za.ac.cput.domain.transactions.WalletTransaction;
import za.ac.cput.dto.transactions.WalletTransactionRequest;
import za.ac.cput.factory.transactions.WalletTransactionFactory;
import za.ac.cput.service.transactions.IWalletTransactionService;

@RestController
@RequestMapping("/api/wallet-transactions")
public class WalletTransactionController {

    private final IWalletTransactionService service;

    public WalletTransactionController(IWalletTransactionService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<WalletTransaction> create(@RequestBody WalletTransactionRequest request) {
        WalletTransaction created = this.service.create(WalletTransactionFactory.createWalletTransaction(
                request.walletId(), request.type(), request.amount(), request.balanceAfter(),
                request.referenceType(), request.referenceId(), request.description()));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WalletTransaction> read(@PathVariable Long id) {
        WalletTransaction found = this.service.read(id);
        return found == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(found);
    }

    @PutMapping("/{id}")
    public ResponseEntity<WalletTransaction> update(@PathVariable Long id,
                                                    @RequestBody WalletTransactionRequest request) {
        WalletTransaction existing = this.service.read(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(this.service.update(WalletTransactionFactory.updateWalletTransaction(
                existing, request.walletId(), request.type(), request.amount(), request.balanceAfter(),
                request.referenceType(), request.referenceId(), request.description())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return this.service.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping
    public List<WalletTransaction> getAll() {
        return this.service.getAll();
    }

    @GetMapping("/wallet/{walletId}")
    public List<WalletTransaction> byWallet(@PathVariable long walletId) {
        return this.service.findByWalletId(walletId);
    }

}
