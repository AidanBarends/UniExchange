/*
 TrustedDevice.java

 TrustedDevice POJO class

 A browser that has already proved itself with an emailed OTP, so the next
 sign-in from it can skip the code. Modelled on Verification: per-user, holds a
 hashed secret rather than the secret itself, and expires.

 Two lifetimes, chosen by whether the student ticked "Remember me":
  - persistent  -> weeks, and the window slides forward on every use.
  - not persistent -> hours, a hard cap. The browser stores this one in
    sessionStorage, so closing the browser loses it and the OTP comes back.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 05 September 2026
*/

package za.ac.cput.domain.identity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "trusted_device")
public class TrustedDevice {
    //  Variables/Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long trustedDeviceId;

    @Column(nullable = false, name = "user_id")
    private long userId;

    // SHA-256 of the opaque token held by the browser, hex encoded. Unique so it
    // can be looked up directly - see DeviceTrustService for why not BCrypt.
    @Column(nullable = false, unique = true, length = 64, name = "token_hash")
    private String tokenHash;

    // Truncated User-Agent, so a future "your devices" screen has something to show.
    @Column(length = 255)
    private String label;

    // True when "Remember me" was ticked: longer lifetime, sliding expiry.
    @Column(nullable = false)
    private boolean persistent;

    @Column(nullable = false, name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;

    //  Constructors
    protected TrustedDevice() {
        // Required by JPA
    }

    private TrustedDevice(Builder builder) {
        this.trustedDeviceId = builder.trustedDeviceId;
        this.userId = builder.userId;
        this.tokenHash = builder.tokenHash;
        this.label = builder.label;
        this.persistent = builder.persistent;
        this.expiresAt = builder.expiresAt;
        this.lastUsedAt = builder.lastUsedAt;
        this.revokedAt = builder.revokedAt;
        this.createdAt = builder.createdAt;
    }

    //  Getters
    public long getTrustedDeviceId() {
        return trustedDeviceId;
    }

    public long getUserId() {
        return userId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public String getLabel() {
        return label;
    }

    public boolean isPersistent() {
        return persistent;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getLastUsedAt() {
        return lastUsedAt;
    }

    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    //  toString
    @Override
    public String toString() {
        // tokenHash is deliberately omitted - it is the credential, and this
        // string ends up in logs. Same reasoning as User.toString() and its
        // redacted passwordHash.
        return "TrustedDevice{" +
                "trustedDeviceId=" + trustedDeviceId +
                ", userId=" + userId +
                ", tokenHash='[REDACTED]'" +
                ", label='" + label + '\'' +
                ", persistent=" + persistent +
                ", expiresAt=" + expiresAt +
                ", lastUsedAt=" + lastUsedAt +
                ", revokedAt=" + revokedAt +
                ", createdAt=" + createdAt +
                '}';
    }

    //  Builder Class
    public static class Builder {

        //  Variables/Attributes
        private long trustedDeviceId;
        private long userId;
        private String tokenHash;
        private String label;
        private boolean persistent;
        private LocalDateTime expiresAt;
        private LocalDateTime lastUsedAt;
        private LocalDateTime revokedAt;
        private LocalDateTime createdAt;

        //  Setters
        public Builder setTrustedDeviceId(long trustedDeviceId) {
            this.trustedDeviceId = trustedDeviceId;
            return this;
        }

        public Builder setUserId(long userId) {
            this.userId = userId;
            return this;
        }

        public Builder setTokenHash(String tokenHash) {
            this.tokenHash = tokenHash;
            return this;
        }

        public Builder setLabel(String label) {
            this.label = label;
            return this;
        }

        public Builder setPersistent(boolean persistent) {
            this.persistent = persistent;
            return this;
        }

        public Builder setExpiresAt(LocalDateTime expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public Builder setLastUsedAt(LocalDateTime lastUsedAt) {
            this.lastUsedAt = lastUsedAt;
            return this;
        }

        public Builder setRevokedAt(LocalDateTime revokedAt) {
            this.revokedAt = revokedAt;
            return this;
        }

        public Builder setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder copy(TrustedDevice trustedDevice) {
            this.trustedDeviceId = trustedDevice.trustedDeviceId;
            this.userId = trustedDevice.userId;
            this.tokenHash = trustedDevice.tokenHash;
            this.label = trustedDevice.label;
            this.persistent = trustedDevice.persistent;
            this.expiresAt = trustedDevice.expiresAt;
            this.lastUsedAt = trustedDevice.lastUsedAt;
            this.revokedAt = trustedDevice.revokedAt;
            this.createdAt = trustedDevice.createdAt;
            return this;
        }

        //  build method
        public TrustedDevice build() {
            return new TrustedDevice(this);
        }
    }
}
