/*
 VendorApplicationController.java

 REST endpoints for VendorApplication.

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import za.ac.cput.domain.enums.VendorApplicationStatus;
import za.ac.cput.domain.trust.VendorApplication;
import za.ac.cput.dto.trust.VendorApplicationRequest;
import za.ac.cput.factory.trust.VendorApplicationFactory;
import za.ac.cput.service.trust.IVendorApplicationService;

@RestController
@RequestMapping("/api/vendor-applications")
public class VendorApplicationController {

    private final IVendorApplicationService service;

    public VendorApplicationController(IVendorApplicationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<VendorApplication> create(@RequestBody VendorApplicationRequest request) {
        VendorApplication created = this.service.create(VendorApplicationFactory.createVendorApplication(
                request.applicantId(), request.businessName(), request.businessDescription(),
                request.status()));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VendorApplication> read(@PathVariable Long id) {
        VendorApplication found = this.service.read(id);
        return found == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(found);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VendorApplication> update(@PathVariable Long id,
                                                    @RequestBody VendorApplicationRequest request) {
        VendorApplication existing = this.service.read(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(this.service.update(VendorApplicationFactory.updateVendorApplication(
                existing, request.applicantId(), request.businessName(), request.businessDescription(),
                request.status())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return this.service.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping
    public List<VendorApplication> getAll() {
        return this.service.getAll();
    }

    @GetMapping("/status/{status}")
    public List<VendorApplication> byStatus(@PathVariable VendorApplicationStatus status) {
        return this.service.findByStatus(status);
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<VendorApplication> approve(@PathVariable Long id, @RequestParam long reviewedBy,
                                                     @RequestParam(required = false) String reviewNote) {
        VendorApplication updated = this.service.decide(id, VendorApplicationStatus.APPROVED, reviewedBy, reviewNote);
        return updated == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<VendorApplication> reject(@PathVariable Long id, @RequestParam long reviewedBy,
                                                    @RequestParam(required = false) String reviewNote) {
        VendorApplication updated = this.service.decide(id, VendorApplicationStatus.REJECTED, reviewedBy, reviewNote);
        return updated == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(updated);
    }

}
