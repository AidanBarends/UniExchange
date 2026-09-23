/*
 AuditLogRequest.java

 Inbound payload for creating/updating a AuditLog. Entities have no public
 setters, so requests arrive as a record and are handed to AuditLogFactory.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.dto.admin;

public record AuditLogRequest(
        long adminId,
        String action,
        String targetType,
        Long targetId,
        String details) {
}
