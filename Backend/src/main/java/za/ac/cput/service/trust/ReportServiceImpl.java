/*
 ReportServiceImpl.java

 Business logic for Report. Implements the generic CRUD contract
 IService<Report, Long> plus the Report-specific operations.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.service.trust;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import za.ac.cput.domain.enums.ReportStatus;
import za.ac.cput.domain.trust.Report;
import za.ac.cput.repository.trust.ReportRepository;
import za.ac.cput.util.Helper;

@Service
public class ReportServiceImpl implements IReportService {

    private final ReportRepository repository;

    public ReportServiceImpl(ReportRepository repository) {
        this.repository = repository;
    }

    @Override
    public Report create(Report report) {
        return this.repository.save(report);
    }

    @Override
    public Report read(Long id) {
        return id == null ? null : this.repository.findById(id).orElse(null);
    }

    @Override
    public Report update(Report report) {
        return this.repository.save(report);
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
    public List<Report> getAll() {
        return this.repository.findAll();
    }

    @Override
    public List<Report> findByStatus(ReportStatus status) {
        return this.repository.findByStatus(status);
    }

    @Override
    public Report resolve(Long reportId, long handledBy, String resolutionNote) {
        Report found = read(reportId);
        if (found == null) {
            return null;
        }
        if (!Helper.isValidId(handledBy)) {
            throw new IllegalArgumentException("Report: handledBy must be a positive id");
        }
        return this.repository.save(new Report.Builder()
                .copy(found)
                .setStatus(ReportStatus.RESOLVED)
                .setHandledBy(handledBy)
                .setResolutionNote(resolutionNote)
                .setResolvedAt(LocalDateTime.now())
                .build());
    }

}
