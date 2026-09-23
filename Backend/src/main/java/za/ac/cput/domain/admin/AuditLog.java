/*
 AuditLog.java

 AuditLog POJO class

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/
package za.ac.cput.domain.admin;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
public class AuditLog {

    //  Variables/Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long auditLogId;

    @Column(name = "admin_id")
    private long adminId;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(nullable = false, length = 50, name = "target_type")
    private String targetType;

    @Column(name = "target_id")
    private Long targetId;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;

    //  Constructors
    protected AuditLog() {
        // Required by JPA
    }

    private AuditLog(Builder builder) {
        this.auditLogId = builder.auditLogId;
        this.adminId = builder.adminId;
        this.action = builder.action;
        this.targetType = builder.targetType;
        this.targetId = builder.targetId;
        this.details = builder.details;
        this.createdAt = builder.createdAt;
    }

    //  Getters
    public Long getAuditLogId() {
        return auditLogId;
    }

    public long getAdminId() {
        return adminId;
    }

    public String getAction() {
        return action;
    }

    public String getTargetType() {
        return targetType;
    }

    public Long getTargetId() {
        return targetId;
    }

    public String getDetails() {
        return details;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    //  toString
    @Override
    public String toString() {
        return "AuditLog{" +
                "auditLogId=" + auditLogId +
                ", adminId=" + adminId +
                ", action='" + action + '\'' +
                ", targetType='" + targetType + '\'' +
                ", targetId=" + targetId +
                ", details='" + details + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }

    //  Builder Class
    public static class Builder{

        //  Variables/Attributes
        private long auditLogId;
        private Long adminId;
        private String action;
        private String targetType;
        private Long targetId;
        private String details;
        private LocalDateTime createdAt;

        //  Setters
        public Builder setAuditLogId(long auditLogId) {
            this.auditLogId = auditLogId;
            return this;
        }

        public Builder setAdminId(Long adminId) {
            this.adminId = adminId;
            return this;
        }

        public Builder setAction(String action) {
            this.action = action;
            return this;
        }

        public Builder setTargetType(String targetType) {
            this.targetType = targetType;
            return this;
        }

        public Builder setTargetId(Long targetId) {
            this.targetId = targetId;
            return this;
        }

        public Builder setDetails(String details) {
            this.details = details;
            return this;
        }

        public Builder setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder copy(AuditLog auditLog) {
            this.auditLogId = auditLog.auditLogId;
            this.adminId = auditLog.adminId;
            this.action = auditLog.action;
            this.targetType = auditLog.targetType;
            this.targetId = auditLog.targetId;
            this.details = auditLog.details;
            this.createdAt = auditLog.createdAt;
            return this;
        }

        //  build method
        public AuditLog build() {
            return new AuditLog(this);
        }
    }
}
