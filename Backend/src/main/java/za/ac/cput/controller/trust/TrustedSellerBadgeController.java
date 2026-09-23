/*
 TrustedSellerBadgeController.java

 REST endpoints for TrustedSellerBadge.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.controller.trust;

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

import za.ac.cput.domain.trust.TrustedSellerBadge;
import za.ac.cput.dto.trust.TrustedSellerBadgeRequest;
import za.ac.cput.factory.trust.TrustedSellerBadgeFactory;
import za.ac.cput.service.trust.ITrustedSellerBadgeService;

@RestController
@RequestMapping("/api/trusted-seller-badges")
public class TrustedSellerBadgeController {

    private final ITrustedSellerBadgeService service;

    public TrustedSellerBadgeController(ITrustedSellerBadgeService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<TrustedSellerBadge> create(@RequestBody TrustedSellerBadgeRequest request) {
        TrustedSellerBadge created = this.service.create(TrustedSellerBadgeFactory.createTrustedSellerBadge(
                request.userId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrustedSellerBadge> read(@PathVariable Long id) {
        TrustedSellerBadge found = this.service.read(id);
        return found == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(found);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TrustedSellerBadge> update(@PathVariable Long id,
                                                     @RequestBody TrustedSellerBadgeRequest request) {
        TrustedSellerBadge existing = this.service.read(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(this.service.update(TrustedSellerBadgeFactory.updateTrustedSellerBadge(
                existing, request.userId())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return this.service.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping
    public List<TrustedSellerBadge> getAll() {
        return this.service.getAll();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<TrustedSellerBadge> byUser(@PathVariable long userId) {
        TrustedSellerBadge found = this.service.findByUserId(userId);
        return found == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(found);
    }

    @PatchMapping("/{id}/revoke")
    public ResponseEntity<TrustedSellerBadge> revoke(@PathVariable Long id) {
        TrustedSellerBadge updated = this.service.revoke(id);
        return updated == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(updated);
    }

}
