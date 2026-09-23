/*
 ReportRequest.java

 Inbound payload for creating/updating a Report. Entities have no public
 setters, so requests arrive as a record and are handed to ReportFactory.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.dto.trust;

import za.ac.cput.domain.enums.ReportStatus;
import za.ac.cput.domain.enums.ReportTargetType;

public record ReportRequest(
        long reporterId,
        ReportTargetType targetType,
        long targetId,
        String reason,
        ReportStatus status) {
}
