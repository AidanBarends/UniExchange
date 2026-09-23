/*
 PaymentController.java

 REST endpoints for Payment.

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

import za.ac.cput.domain.transactions.Payment;
import za.ac.cput.dto.transactions.PaymentRequest;
import za.ac.cput.factory.transactions.PaymentFactory;
import za.ac.cput.service.transactions.IPaymentService;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final IPaymentService service;

    public PaymentController(IPaymentService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Payment> create(@RequestBody PaymentRequest request) {
        Payment created = this.service.create(PaymentFactory.createPayment(
                request.transactionId(), request.amount(), request.method(), request.status(),
                request.externalReference()));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Payment> read(@PathVariable Long id) {
        Payment found = this.service.read(id);
        return found == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(found);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Payment> update(@PathVariable Long id, @RequestBody PaymentRequest request) {
        Payment existing = this.service.read(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(this.service.update(PaymentFactory.updatePayment(
                existing, request.transactionId(), request.amount(), request.method(), request.status(),
                request.externalReference())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return this.service.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping
    public List<Payment> getAll() {
        return this.service.getAll();
    }

    @GetMapping("/transaction/{transactionId}")
    public List<Payment> byTransaction(@PathVariable long transactionId) {
        return this.service.findByTransactionId(transactionId);
    }

}
