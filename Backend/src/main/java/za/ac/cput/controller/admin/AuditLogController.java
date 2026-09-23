/*
 AuditLogController.java

 REST endpoints for AuditLog.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.controller.admin;

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

import za.ac.cput.domain.admin.AuditLog;
import za.ac.cput.dto.admin.AuditLogRequest;
import za.ac.cput.factory.admin.AuditLogFactory;
import za.ac.cput.service.admin.IAuditLogService;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private final IAuditLogService service;

    public AuditLogController(IAuditLogService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<AuditLog> create(@RequestBody AuditLogRequest request) {
        AuditLog created = this.service.create(AuditLogFactory.createAuditLog(
                request.adminId(), request.action(), request.targetType(), request.targetId(),
                request.details()));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuditLog> read(@PathVariable Long id) {
        AuditLog found = this.service.read(id);
        return found == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(found);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AuditLog> update(@PathVariable Long id, @RequestBody AuditLogRequest request) {
        AuditLog existing = this.service.read(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(this.service.update(AuditLogFactory.updateAuditLog(
                existing, request.adminId(), request.action(), request.targetType(), request.targetId(),
                request.details())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return this.service.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping
    public List<AuditLog> getAll() {
        return this.service.getAll();
    }

    @GetMapping("/admin/{adminId}")
    public List<AuditLog> byAdmin(@PathVariable long adminId) {
        return this.service.findByAdminId(adminId);
    }

}
