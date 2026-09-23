/*
 NotificationFactory.java

 Factory for Notification. All construction goes through here so that every
 Notification is validated with Helper before it exists - the entity itself
 exposes only a Builder and a protected JPA constructor.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.factory.communication;

import java.time.LocalDateTime;

import za.ac.cput.domain.communication.Notification;
import za.ac.cput.domain.enums.NotificationType;
import za.ac.cput.util.Helper;

public class NotificationFactory {

    // Prevent instantiation - factory class
    private NotificationFactory() {}

    public static Notification createNotification(long userId, NotificationType type, String title,
                                                  String content, String entityType, Long entityId) {
        if (!Helper.isValidId(userId)) {
            throw new IllegalArgumentException("Notification: userId must be a positive id");
        }

        if (!Helper.isValidObject(type)) {
            throw new IllegalArgumentException("Notification: type is required");
        }

        if (Helper.isNullOrEmpty(title)) {
            throw new IllegalArgumentException("Notification: title is required");
        }

        if (entityId != null && !Helper.isValidId(entityId)) {
            throw new IllegalArgumentException("Notification: entityId must be a positive id when supplied");
        }

        LocalDateTime now = LocalDateTime.now();

        return new Notification.Builder()
                .setUserId(userId)
                .setType(type)
                .setTitle(title)
                .setContent(content)
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setRead(false)
                .setCreatedAt(now)
                .build();
    }

    public static Notification updateNotification(Notification existing, long userId, NotificationType type,
                                                  String title, String content, String entityType,
                                                  Long entityId) {
        if (!Helper.isValidObject(existing)) {
            throw new IllegalArgumentException("Notification: existing record is required for an update");
        }

        if (!Helper.isValidId(userId)) {
            throw new IllegalArgumentException("Notification: userId must be a positive id");
        }

        if (!Helper.isValidObject(type)) {
            throw new IllegalArgumentException("Notification: type is required");
        }

        if (Helper.isNullOrEmpty(title)) {
            throw new IllegalArgumentException("Notification: title is required");
        }

        if (entityId != null && !Helper.isValidId(entityId)) {
            throw new IllegalArgumentException("Notification: entityId must be a positive id when supplied");
        }

        return new Notification.Builder()
                .copy(existing)
                .setUserId(userId)
                .setType(type)
                .setTitle(title)
                .setContent(content)
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setRead(false)
                .build();
    }

}
