/*
 VerificationController.java

 REST endpoints for Verification.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.controller.identity;

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

import za.ac.cput.domain.identity.Verification;
import za.ac.cput.dto.identity.VerificationRequest;
import za.ac.cput.factory.identity.VerificationFactory;
import za.ac.cput.service.identity.IVerificationService;

@RestController
@RequestMapping("/api/verifications")
public class VerificationController {

    private final IVerificationService service;

    public VerificationController(IVerificationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Verification> create(@RequestBody VerificationRequest request) {
        Verification created = this.service.create(VerificationFactory.createVerification(
                request.userId(), request.verificationType(), request.token(), request.expiresAt()));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Verification> read(@PathVariable Long id) {
        Verification found = this.service.read(id);
        return found == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(found);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Verification> update(@PathVariable Long id,
                                               @RequestBody VerificationRequest request) {
        Verification existing = this.service.read(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(this.service.update(VerificationFactory.updateVerification(
                existing, request.userId(), request.verificationType(), request.token(), request.expiresAt())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return this.service.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping
    public List<Verification> getAll() {
        return this.service.getAll();
    }

    @GetMapping("/token/{token}")
    public ResponseEntity<Verification> byToken(@PathVariable String token) {
        Verification found = this.service.findByToken(token);
        return found == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(found);
    }

}
