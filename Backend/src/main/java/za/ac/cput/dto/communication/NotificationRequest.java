/*
 NotificationRequest.java

 Inbound payload for creating/updating a Notification. Entities have no public
 setters, so requests arrive as a record and are handed to NotificationFactory.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.dto.communication;

import za.ac.cput.domain.enums.NotificationType;

public record NotificationRequest(
        long userId,
        NotificationType type,
        String title,
        String content,
        String entityType,
        Long entityId) {
}
