/*
 VendorApplication.java

 VendorApplication POJO class

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
import za.ac.cput.domain.enums.VendorApplicationStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "vendor_application")
public class VendorApplication {
    //  Variables/Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long vendorApplicationId;

    @Column(nullable = false, name = "applicant_id")
    private long applicantId;

    @Column(nullable = false, length = 150, name = "business_name")
    private String businessName;

    @Column(columnDefinition = "TEXT", name = "business_description")
    private String businessDescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VendorApplicationStatus status;

    @Column(name = "reviewed_by")
    private Long reviewedBy;

    @Column(length = 500, name = "review_note")
    private String reviewNote;

    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    //  Constructors
    protected VendorApplication() {
        // Required by JPA
    }

    private VendorApplication(Builder builder) {
        this.vendorApplicationId = builder.vendorApplicationId;
        this.applicantId = builder.applicantId;
        this.businessName = builder.businessName;
        this.businessDescription = builder.businessDescription;
        this.status = builder.status;
        this.reviewedBy = builder.reviewedBy;
        this.reviewNote = builder.reviewNote;
        this.createdAt = builder.createdAt;
        this.reviewedAt = builder.reviewedAt;
    }

    //  Getters
    public long getVendorApplicationId() {
        return vendorApplicationId;
    }

    public long getApplicantId() {
        return applicantId;
    }

    public String getBusinessName() {
        return businessName;
    }

    public String getBusinessDescription() {
        return businessDescription;
    }

    public VendorApplicationStatus getStatus() {
        return status;
    }

    public Long getReviewedBy() {
        return reviewedBy;
    }

    public String getReviewNote() {
        return reviewNote;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    //  toString
    @Override
    public String toString() {
        return "VendorApplication{" +
                "vendorApplicationId=" + vendorApplicationId +
                ", applicantId=" + applicantId +
                ", businessName='" + businessName + '\'' +
                ", businessDescription='" + businessDescription + '\'' +
                ", status=" + status +
                ", reviewedBy=" + reviewedBy +
                ", reviewNote='" + reviewNote + '\'' +
                ", createdAt=" + createdAt +
                ", reviewedAt=" + reviewedAt +
                '}';
    }

    //  Builder Class
    public static class Builder {

        //  Variables/Attributes
        private long vendorApplicationId;
        private long applicantId;
        private String businessName;
        private String businessDescription;
        private VendorApplicationStatus status;
        private Long reviewedBy;
        private String reviewNote;
        private LocalDateTime createdAt;
        private LocalDateTime reviewedAt;

        //  Setters
        public Builder setVendorApplicationId(long vendorApplicationId) {
            this.vendorApplicationId = vendorApplicationId;
            return this;
        }

        public Builder setApplicantId(long applicantId) {
            this.applicantId = applicantId;
            return this;
        }

        public Builder setBusinessName(String businessName) {
            this.businessName = businessName;
            return this;
        }

        public Builder setBusinessDescription(String businessDescription) {
            this.businessDescription = businessDescription;
            return this;
        }

        public Builder setStatus(VendorApplicationStatus status) {
            this.status = status;
            return this;
        }

        public Builder setReviewedBy(Long reviewedBy) {
            this.reviewedBy = reviewedBy;
            return this;
        }

        public Builder setReviewNote(String reviewNote) {
            this.reviewNote = reviewNote;
            return this;
        }

        public Builder setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder setReviewedAt(LocalDateTime reviewedAt) {
            this.reviewedAt = reviewedAt;
            return this;
        }

        public Builder copy(VendorApplication vendorApplication) {
            this.vendorApplicationId = vendorApplication.vendorApplicationId;
            this.applicantId = vendorApplication.applicantId;
            this.businessName = vendorApplication.businessName;
            this.businessDescription = vendorApplication.businessDescription;
            this.status = vendorApplication.status;
            this.reviewedBy = vendorApplication.reviewedBy;
            this.reviewNote = vendorApplication.reviewNote;
            this.createdAt = vendorApplication.createdAt;
            this.reviewedAt = vendorApplication.reviewedAt;
            return this;
        }

        //  build method
        public VendorApplication build() {
            return new VendorApplication(this);
        }
    }
}