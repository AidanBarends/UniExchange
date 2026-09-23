/*
 IAuditLogService.java

 Service contract for AuditLog.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.service.admin;

import java.util.List;

import za.ac.cput.domain.admin.AuditLog;
import za.ac.cput.service.IService;

public interface IAuditLogService extends IService<AuditLog, Long> {

    List<AuditLog> findByAdminId(long adminId);

}
