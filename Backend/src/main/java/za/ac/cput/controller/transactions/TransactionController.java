/*
 TransactionController.java

 REST endpoints for Transaction.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.controller.transactions;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import za.ac.cput.domain.transactions.Transaction;
import za.ac.cput.dto.transactions.TransactionRequest;
import za.ac.cput.factory.transactions.TransactionFactory;
import za.ac.cput.service.transactions.ITransactionService;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final ITransactionService service;

    public TransactionController(ITransactionService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Transaction> create(@RequestBody TransactionRequest request) {
        Transaction created = this.service.create(TransactionFactory.createTransaction(
                request.buyerId(), request.sellerId(), request.listingId(), request.amount(),
                request.paymentMethod(), request.status()));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> read(@PathVariable Long id) {
        Transaction found = this.service.read(id);
        return found == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(found);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Transaction> update(@PathVariable Long id, @RequestBody TransactionRequest request) {
        Transaction existing = this.service.read(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(this.service.update(TransactionFactory.updateTransaction(
                existing, request.buyerId(), request.sellerId(), request.listingId(), request.amount(),
                request.paymentMethod(), request.status())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return this.service.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping
    public List<Transaction> getAll() {
        return this.service.getAll();
    }

    @GetMapping("/buyer/{buyerId}")
    public List<Transaction> byBuyer(@PathVariable long buyerId) {
        return this.service.findByBuyerId(buyerId);
    }

    @GetMapping("/seller/{sellerId}")
    public List<Transaction> bySeller(@PathVariable long sellerId) {
        return this.service.findBySellerId(sellerId);
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<Transaction> complete(@PathVariable Long id) {
        Transaction updated = this.service.complete(id);
        return updated == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(updated);
    }

}
