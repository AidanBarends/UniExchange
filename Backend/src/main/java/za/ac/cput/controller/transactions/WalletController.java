/*
 WalletController.java

 REST endpoints for Wallet.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.controller.transactions;

import java.math.BigDecimal;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import za.ac.cput.domain.transactions.Wallet;
import za.ac.cput.dto.transactions.WalletRequest;
import za.ac.cput.factory.transactions.WalletFactory;
import za.ac.cput.service.transactions.IWalletService;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    private final IWalletService service;

    public WalletController(IWalletService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Wallet> create(@RequestBody WalletRequest request) {
        Wallet created = this.service.create(WalletFactory.createWallet(
                request.userId(), request.balance(), request.currency()));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Wallet> read(@PathVariable Long id) {
        Wallet found = this.service.read(id);
        return found == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(found);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Wallet> update(@PathVariable Long id, @RequestBody WalletRequest request) {
        Wallet existing = this.service.read(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(this.service.update(WalletFactory.updateWallet(
                existing, request.userId(), request.balance(), request.currency())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return this.service.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping
    public List<Wallet> getAll() {
        return this.service.getAll();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Wallet> byUser(@PathVariable long userId) {
        Wallet found = this.service.findByUserId(userId);
        return found == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(found);
    }

    @PostMapping("/{id}/credit")
    public ResponseEntity<Wallet> credit(@PathVariable Long id, @RequestParam BigDecimal amount,
                                         @RequestParam(required = false) String description) {
        Wallet updated = this.service.credit(id, amount, description);
        return updated == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/debit")
    public ResponseEntity<Wallet> debit(@PathVariable Long id, @RequestParam BigDecimal amount,
                                        @RequestParam(required = false) String description) {
        Wallet updated = this.service.debit(id, amount, description);
        return updated == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(updated);
    }

}
