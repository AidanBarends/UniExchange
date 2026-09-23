/*
 TrustedSellerBadge.java

 TrustedSellerBadge POJO class

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.domain.trust;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "trusted_seller_badge")
public class TrustedSellerBadge {
    //  Variables/Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long trustedSellerBadgeId;

    @Column(nullable = false, unique = true, name = "user_id")
    private long userId;

    @Column(nullable = false, name = "earned_at")
    private LocalDateTime earnedAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    //  Constructors
    protected TrustedSellerBadge() {
        // Required by JPA
    }

    private TrustedSellerBadge(Builder builder) {
        this.trustedSellerBadgeId = builder.trustedSellerBadgeId;
        this.userId = builder.userId;
        this.earnedAt = builder.earnedAt;
        this.revokedAt = builder.revokedAt;
    }

    //  Getters
    public long getTrustedSellerBadgeId() {
        return trustedSellerBadgeId;
    }

    public long getUserId() {
        return userId;
    }

    public LocalDateTime getEarnedAt() {
        return earnedAt;
    }

    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }

    //  toString
    @Override
    public String toString() {
        return "TrustedSellerBadge{" +
                "trustedSellerBadgeId=" + trustedSellerBadgeId +
                ", userId=" + userId +
                ", earnedAt=" + earnedAt +
                ", revokedAt=" + revokedAt +
                '}';
    }

    //  Builder Class
    public static class Builder {

        //  Variables/Attributes
        private long trustedSellerBadgeId;
        private long userId;
        private LocalDateTime earnedAt;
        private LocalDateTime revokedAt;

        //  Setters
        public Builder setTrustedSellerBadgeId(long trustedSellerBadgeId) {
            this.trustedSellerBadgeId = trustedSellerBadgeId;
            return this;
        }

        public Builder setUserId(long userId) {
            this.userId = userId;
            return this;
        }

        public Builder setEarnedAt(LocalDateTime earnedAt) {
            this.earnedAt = earnedAt;
            return this;
        }

        public Builder setRevokedAt(LocalDateTime revokedAt) {
            this.revokedAt = revokedAt;
            return this;
        }

        public Builder copy(TrustedSellerBadge trustedSellerBadge) {
            this.trustedSellerBadgeId = trustedSellerBadge.trustedSellerBadgeId;
            this.userId = trustedSellerBadge.userId;
            this.earnedAt = trustedSellerBadge.earnedAt;
            this.revokedAt = trustedSellerBadge.revokedAt;
            return this;
        }

        //  build method
        public TrustedSellerBadge build() {
            return new TrustedSellerBadge(this);
        }
    }
}