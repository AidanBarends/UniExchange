/*
 AuditLogRepository.java

 Spring Data JPA repository for the AuditLog entity.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.repository.admin;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import za.ac.cput.domain.admin.AuditLog;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByAdminId(long adminId);

    List<AuditLog> findByTargetTypeAndTargetId(String targetType, Long targetId);

}
