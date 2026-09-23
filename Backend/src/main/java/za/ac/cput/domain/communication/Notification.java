/*
 Notification.java

 Notification POJO class

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.domain.communication;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import za.ac.cput.domain.enums.NotificationType;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
public class Notification {
    //  Variables/Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long notificationId;

    @Column(nullable = false, name = "user_id")
    private long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(length = 30, name = "entity_type")
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(nullable = false, name = "is_read")
    private boolean isRead;

    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;

    //  Constructors
    protected Notification() {
        // Required by JPA
    }

    private Notification(Builder builder) {
        this.notificationId = builder.notificationId;
        this.userId = builder.userId;
        this.type = builder.type;
        this.title = builder.title;
        this.content = builder.content;
        this.entityType = builder.entityType;
        this.entityId = builder.entityId;
        this.isRead = builder.isRead;
        this.createdAt = builder.createdAt;
    }

    //  Getters
    public long getNotificationId() {
        return notificationId;
    }

    public long getUserId() {
        return userId;
    }

    public NotificationType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getEntityType() {
        return entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public boolean isRead() {
        return isRead;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    //  toString
    @Override
    public String toString() {
        return "Notification{" +
                "notificationId=" + notificationId +
                ", userId=" + userId +
                ", type=" + type +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", entityType='" + entityType + '\'' +
                ", entityId=" + entityId +
                ", isRead=" + isRead +
                ", createdAt=" + createdAt +
                '}';
    }

    //  Builder Class
    public static class Builder {

        //  Variables/Attributes
        private long notificationId;
        private long userId;
        private NotificationType type;
        private String title;
        private String content;
        private String entityType;
        private Long entityId;
        private boolean isRead;
        private LocalDateTime createdAt;

        //  Setters
        public Builder setNotificationId(long notificationId) {
            this.notificationId = notificationId;
            return this;
        }

        public Builder setUserId(long userId) {
            this.userId = userId;
            return this;
        }

        public Builder setType(NotificationType type) {
            this.type = type;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setContent(String content) {
            this.content = content;
            return this;
        }

        public Builder setEntityType(String entityType) {
            this.entityType = entityType;
            return this;
        }

        public Builder setEntityId(Long entityId) {
            this.entityId = entityId;
            return this;
        }

        public Builder setRead(boolean read) {
            isRead = read;
            return this;
        }

        public Builder setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder copy(Notification notification) {
            this.notificationId = notification.notificationId;
            this.userId = notification.userId;
            this.type = notification.type;
            this.title = notification.title;
            this.content = notification.content;
            this.entityType = notification.entityType;
            this.entityId = notification.entityId;
            this.isRead = notification.isRead;
            this.createdAt = notification.createdAt;
            return this;
        }

        //  build method
        public Notification build() {
            return new Notification(this);
        }
    }
}