/*
 AuditLogServiceImpl.java

 Business logic for AuditLog. Implements the generic CRUD contract
 IService<AuditLog, Long> plus the AuditLog-specific operations.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.service.admin;

import java.util.List;

import org.springframework.stereotype.Service;

import za.ac.cput.domain.admin.AuditLog;
import za.ac.cput.repository.admin.AuditLogRepository;

@Service
public class AuditLogServiceImpl implements IAuditLogService {

    private final AuditLogRepository repository;

    public AuditLogServiceImpl(AuditLogRepository repository) {
        this.repository = repository;
    }

    @Override
    public AuditLog create(AuditLog auditLog) {
        return this.repository.save(auditLog);
    }

    @Override
    public AuditLog read(Long id) {
        return id == null ? null : this.repository.findById(id).orElse(null);
    }

    @Override
    public AuditLog update(AuditLog auditLog) {
        return this.repository.save(auditLog);
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
    public List<AuditLog> getAll() {
        return this.repository.findAll();
    }

    @Override
    public List<AuditLog> findByAdminId(long adminId) {
        return this.repository.findByAdminId(adminId);
    }

}
