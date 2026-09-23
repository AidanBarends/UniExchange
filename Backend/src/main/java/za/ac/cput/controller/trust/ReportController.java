/*
 ReportController.java

 REST endpoints for Report.

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

import za.ac.cput.domain.enums.ReportStatus;
import za.ac.cput.domain.trust.Report;
import za.ac.cput.dto.trust.ReportRequest;
import za.ac.cput.factory.trust.ReportFactory;
import za.ac.cput.service.trust.IReportService;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final IReportService service;

    public ReportController(IReportService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Report> create(@RequestBody ReportRequest request) {
        Report created = this.service.create(ReportFactory.createReport(
                request.reporterId(), request.targetType(), request.targetId(), request.reason(),
                request.status()));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Report> read(@PathVariable Long id) {
        Report found = this.service.read(id);
        return found == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(found);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Report> update(@PathVariable Long id, @RequestBody ReportRequest request) {
        Report existing = this.service.read(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(this.service.update(ReportFactory.updateReport(
                existing, request.reporterId(), request.targetType(), request.targetId(), request.reason(),
                request.status())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return this.service.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping
    public List<Report> getAll() {
        return this.service.getAll();
    }

    @GetMapping("/status/{status}")
    public List<Report> byStatus(@PathVariable ReportStatus status) {
        return this.service.findByStatus(status);
    }

    @PatchMapping("/{id}/resolve")
    public ResponseEntity<Report> resolve(@PathVariable Long id, @RequestParam long handledBy,
                                          @RequestParam(required = false) String resolutionNote) {
        Report updated = this.service.resolve(id, handledBy, resolutionNote);
        return updated == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(updated);
    }

}
