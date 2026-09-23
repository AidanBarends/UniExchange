/*
 WalletTransaction.java

 WalletTransaction POJO class

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.domain.transactions;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import za.ac.cput.domain.enums.WalletTransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallet_transaction")
public class WalletTransaction {
    //  Variables/Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long walletTransactionId;

    @Column(nullable = false, name = "wallet_id")
    private long walletId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WalletTransactionType type;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 10, scale = 2, name = "balance_after")
    private BigDecimal balanceAfter;

    @Column(length = 30, name = "reference_type")
    private String referenceType;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(length = 255)
    private String description;

    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;

    //  Constructors
    protected WalletTransaction() {
        // Required by JPA
    }

    private WalletTransaction(Builder builder) {
        this.walletTransactionId = builder.walletTransactionId;
        this.walletId = builder.walletId;
        this.type = builder.type;
        this.amount = builder.amount;
        this.balanceAfter = builder.balanceAfter;
        this.referenceType = builder.referenceType;
        this.referenceId = builder.referenceId;
        this.description = builder.description;
        this.createdAt = builder.createdAt;
    }

    //  Getters
    public long getWalletTransactionId() {
        return walletTransactionId;
    }

    public long getWalletId() {
        return walletId;
    }

    public WalletTransactionType getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    //  toString
    @Override
    public String toString() {
        return "WalletTransaction{" +
                "walletTransactionId=" + walletTransactionId +
                ", walletId=" + walletId +
                ", type=" + type +
                ", amount=" + amount +
                ", balanceAfter=" + balanceAfter +
                ", referenceType='" + referenceType + '\'' +
                ", referenceId=" + referenceId +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }

    //  Builder Class
    public static class Builder {

        //  Variables/Attributes
        private long walletTransactionId;
        private long walletId;
        private WalletTransactionType type;
        private BigDecimal amount;
        private BigDecimal balanceAfter;
        private String referenceType;
        private Long referenceId;
        private String description;
        private LocalDateTime createdAt;

        //  Setters
        public Builder setWalletTransactionId(long walletTransactionId) {
            this.walletTransactionId = walletTransactionId;
            return this;
        }

        public Builder setWalletId(long walletId) {
            this.walletId = walletId;
            return this;
        }

        public Builder setType(WalletTransactionType type) {
            this.type = type;
            return this;
        }

        public Builder setAmount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Builder setBalanceAfter(BigDecimal balanceAfter) {
            this.balanceAfter = balanceAfter;
            return this;
        }

        public Builder setReferenceType(String referenceType) {
            this.referenceType = referenceType;
            return this;
        }

        public Builder setReferenceId(Long referenceId) {
            this.referenceId = referenceId;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder copy(WalletTransaction walletTransaction) {
            this.walletTransactionId = walletTransaction.walletTransactionId;
            this.walletId = walletTransaction.walletId;
            this.type = walletTransaction.type;
            this.amount = walletTransaction.amount;
            this.balanceAfter = walletTransaction.balanceAfter;
            this.referenceType = walletTransaction.referenceType;
            this.referenceId = walletTransaction.referenceId;
            this.description = walletTransaction.description;
            this.createdAt = walletTransaction.createdAt;
            return this;
        }

        //  build method
        public WalletTransaction build() {
            return new WalletTransaction(this);
        }
    }
}