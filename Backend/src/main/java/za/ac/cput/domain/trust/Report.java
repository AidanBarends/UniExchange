/*
 Report.java

 Report POJO class

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.domain.trust;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import za.ac.cput.domain.enums.ReportStatus;
import za.ac.cput.domain.enums.ReportTargetType;

import java.time.LocalDateTime;

@Entity
@Table(name = "report")
public class Report {
    //  Variables/Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long reportId;

    @Column(nullable = false, name = "reporter_id")
    private long reporterId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "target_type")
    private ReportTargetType targetType;

    @Column(nullable = false, name = "target_id")
    private long targetId;

    @Column(nullable = false, length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status;

    @Column(name = "handled_by")
    private Long handledBy;

    @Column(length = 500, name = "resolution_note")
    private String resolutionNote;

    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    //  Constructors
    protected Report() {
        // Required by JPA
    }

    private Report(Builder builder) {
        this.reportId = builder.reportId;
        this.reporterId = builder.reporterId;
        this.targetType = builder.targetType;
        this.targetId = builder.targetId;
        this.reason = builder.reason;
        this.status = builder.status;
        this.handledBy = builder.handledBy;
        this.resolutionNote = builder.resolutionNote;
        this.createdAt = builder.createdAt;
        this.resolvedAt = builder.resolvedAt;
    }

    //  Getters
    public long getReportId() {
        return reportId;
    }

    public long getReporterId() {
        return reporterId;
    }

    public ReportTargetType getTargetType() {
        return targetType;
    }

    public long getTargetId() {
        return targetId;
    }

    public String getReason() {
        return reason;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public Long getHandledBy() {
        return handledBy;
    }

    public String getResolutionNote() {
        return resolutionNote;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    //  toString
    @Override
    public String toString() {
        return "Report{" +
                "reportId=" + reportId +
                ", reporterId=" + reporterId +
                ", targetType=" + targetType +
                ", targetId=" + targetId +
                ", reason='" + reason + '\'' +
                ", status=" + status +
                ", handledBy=" + handledBy +
                ", resolutionNote='" + resolutionNote + '\'' +
                ", createdAt=" + createdAt +
                ", resolvedAt=" + resolvedAt +
                '}';
    }

    //  Builder Class
    public static class Builder {

        //  Variables/Attributes
        private long reportId;
        private long reporterId;
        private ReportTargetType targetType;
        private long targetId;
        private String reason;
        private ReportStatus status;
        private Long handledBy;
        private String resolutionNote;
        private LocalDateTime createdAt;
        private LocalDateTime resolvedAt;

        //  Setters
        public Builder setReportId(long reportId) {
            this.reportId = reportId;
            return this;
        }

        public Builder setReporterId(long reporterId) {
            this.reporterId = reporterId;
            return this;
        }

        public Builder setTargetType(ReportTargetType targetType) {
            this.targetType = targetType;
            return this;
        }

        public Builder setTargetId(long targetId) {
            this.targetId = targetId;
            return this;
        }

        public Builder setReason(String reason) {
            this.reason = reason;
            return this;
        }

        public Builder setStatus(ReportStatus status) {
            this.status = status;
            return this;
        }

        public Builder setHandledBy(Long handledBy) {
            this.handledBy = handledBy;
            return this;
        }

        public Builder setResolutionNote(String resolutionNote) {
            this.resolutionNote = resolutionNote;
            return this;
        }

        public Builder setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder setResolvedAt(LocalDateTime resolvedAt) {
            this.resolvedAt = resolvedAt;
            return this;
        }

        public Builder copy(Report report) {
            this.reportId = report.reportId;
            this.reporterId = report.reporterId;
            this.targetType = report.targetType;
            this.targetId = report.targetId;
            this.reason = report.reason;
            this.status = report.status;
            this.handledBy = report.handledBy;
            this.resolutionNote = report.resolutionNote;
            this.createdAt = report.createdAt;
            this.resolvedAt = report.resolvedAt;
            return this;
        }

        //  build method
        public Report build() {
            return new Report(this);
        }
    }
}