/*
 Verification.java

 Verification POJO class

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.domain.identity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import za.ac.cput.domain.enums.VerificationType;

import java.time.LocalDateTime;

@Entity
@Table(name = "verification")
public class Verification {
    //  Variables/Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long verificationId;

    @Column(nullable = false, name = "user_id")
    private long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "verification_type")
    private VerificationType verificationType;

    @Column(nullable = false, length = 255)
    private String token;

    @Column(nullable = false, name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    // Number of failed code submissions, so a short numeric OTP cannot be brute-forced.
    @Column(nullable = false)
    private int attempts;

    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;

    //  Constructors
    protected Verification() {
        // Required by JPA
    }

    private Verification(Builder builder) {
        this.verificationId = builder.verificationId;
        this.userId = builder.userId;
        this.verificationType = builder.verificationType;
        this.token = builder.token;
        this.expiresAt = builder.expiresAt;
        this.verifiedAt = builder.verifiedAt;
        this.attempts = builder.attempts;
        this.createdAt = builder.createdAt;
    }

    //  Getters
    public long getVerificationId() {
        return verificationId;
    }

    public long getUserId() {
        return userId;
    }

    public VerificationType getVerificationType() {
        return verificationType;
    }

    public String getToken() {
        return token;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public int getAttempts() {
        return attempts;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    //  toString
    @Override
    public String toString() {
        return "Verification{" +
                "verificationId=" + verificationId +
                ", userId=" + userId +
                ", verificationType=" + verificationType +
                ", token='" + token + '\'' +
                ", expiresAt=" + expiresAt +
                ", verifiedAt=" + verifiedAt +
                ", attempts=" + attempts +
                ", createdAt=" + createdAt +
                '}';
    }

    //  Builder Class
    public static class Builder {

        //  Variables/Attributes
        private long verificationId;
        private long userId;
        private VerificationType verificationType;
        private String token;
        private LocalDateTime expiresAt;
        private LocalDateTime verifiedAt;
        private int attempts;
        private LocalDateTime createdAt;

        //  Setters
        public Builder setVerificationId(long verificationId) {
            this.verificationId = verificationId;
            return this;
        }

        public Builder setUserId(long userId) {
            this.userId = userId;
            return this;
        }

        public Builder setVerificationType(VerificationType verificationType) {
            this.verificationType = verificationType;
            return this;
        }

        public Builder setToken(String token) {
            this.token = token;
            return this;
        }

        public Builder setExpiresAt(LocalDateTime expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public Builder setVerifiedAt(LocalDateTime verifiedAt) {
            this.verifiedAt = verifiedAt;
            return this;
        }

        public Builder setAttempts(int attempts) {
            this.attempts = attempts;
            return this;
        }

        public Builder setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder copy(Verification verification) {
            this.verificationId = verification.verificationId;
            this.userId = verification.userId;
            this.verificationType = verification.verificationType;
            this.token = verification.token;
            this.expiresAt = verification.expiresAt;
            this.verifiedAt = verification.verifiedAt;
            this.attempts = verification.attempts;
            this.createdAt = verification.createdAt;
            return this;
        }

        //  build method
        public Verification build() {
            return new Verification(this);
        }
    }
}