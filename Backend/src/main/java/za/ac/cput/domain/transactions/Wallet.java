/*
 Wallet.java

 Wallet POJO class

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.domain.transactions;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallet")
public class Wallet {
    //  Variables/Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long walletId;

    @Column(nullable = false, unique = true, name = "user_id")
    private long userId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal balance;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;

    @Column(nullable = false, name = "updated_at")
    private LocalDateTime updatedAt;

    //  Constructors
    protected Wallet() {
        // Required by JPA
    }

    private Wallet(Builder builder) {
        this.walletId = builder.walletId;
        this.userId = builder.userId;
        this.balance = builder.balance;
        this.currency = builder.currency;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    //  Getters
    public long getWalletId() {
        return walletId;
    }

    public long getUserId() {
        return userId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public String getCurrency() {
        return currency;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    //  toString
    @Override
    public String toString() {
        return "Wallet{" +
                "walletId=" + walletId +
                ", userId=" + userId +
                ", balance=" + balance +
                ", currency='" + currency + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    //  Builder Class
    public static class Builder {

        //  Variables/Attributes
        private long walletId;
        private long userId;
        private BigDecimal balance;
        private String currency;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        //  Setters
        public Builder setWalletId(long walletId) {
            this.walletId = walletId;
            return this;
        }

        public Builder setUserId(long userId) {
            this.userId = userId;
            return this;
        }

        public Builder setBalance(BigDecimal balance) {
            this.balance = balance;
            return this;
        }

        public Builder setCurrency(String currency) {
            this.currency = currency;
            return this;
        }

        public Builder setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder setUpdatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Builder copy(Wallet wallet) {
            this.walletId = wallet.walletId;
            this.userId = wallet.userId;
            this.balance = wallet.balance;
            this.currency = wallet.currency;
            this.createdAt = wallet.createdAt;
            this.updatedAt = wallet.updatedAt;
            return this;
        }

        //  build method
        public Wallet build() {
            return new Wallet(this);
        }
    }
}