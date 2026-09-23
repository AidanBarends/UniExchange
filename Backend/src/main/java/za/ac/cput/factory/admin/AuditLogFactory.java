/*
 AuditLogFactory.java

 Factory for AuditLog. All construction goes through here so that every
 AuditLog is validated with Helper before it exists - the entity itself
 exposes only a Builder and a protected JPA constructor.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.factory.admin;

import java.time.LocalDateTime;

import za.ac.cput.domain.admin.AuditLog;
import za.ac.cput.util.Helper;

public class AuditLogFactory {

    // Prevent instantiation - factory class
    private AuditLogFactory() {}

    public static AuditLog createAuditLog(long adminId, String action, String targetType, Long targetId,
                                          String details) {
        if (!Helper.isValidId(adminId)) {
            throw new IllegalArgumentException("AuditLog: adminId must be a positive id");
        }

        if (Helper.isNullOrEmpty(action)) {
            throw new IllegalArgumentException("AuditLog: action is required");
        }

        if (Helper.isNullOrEmpty(targetType)) {
            throw new IllegalArgumentException("AuditLog: targetType is required");
        }

        if (targetId != null && !Helper.isValidId(targetId)) {
            throw new IllegalArgumentException("AuditLog: targetId must be a positive id when supplied");
        }

        LocalDateTime now = LocalDateTime.now();

        return new AuditLog.Builder()
                .setAdminId(adminId)
                .setAction(action)
                .setTargetType(targetType)
                .setTargetId(targetId)
                .setDetails(details)
                .setCreatedAt(now)
                .build();
    }

    public static AuditLog updateAuditLog(AuditLog existing, long adminId, String action, String targetType,
                                          Long targetId, String details) {
        if (!Helper.isValidObject(existing)) {
            throw new IllegalArgumentException("AuditLog: existing record is required for an update");
        }

        if (!Helper.isValidId(adminId)) {
            throw new IllegalArgumentException("AuditLog: adminId must be a positive id");
        }

        if (Helper.isNullOrEmpty(action)) {
            throw new IllegalArgumentException("AuditLog: action is required");
        }

        if (Helper.isNullOrEmpty(targetType)) {
            throw new IllegalArgumentException("AuditLog: targetType is required");
        }

        if (targetId != null && !Helper.isValidId(targetId)) {
            throw new IllegalArgumentException("AuditLog: targetId must be a positive id when supplied");
        }

        return new AuditLog.Builder()
                .copy(existing)
                .setAdminId(adminId)
                .setAction(action)
                .setTargetType(targetType)
                .setTargetId(targetId)
                .setDetails(details)
                .build();
    }

}
