/*
 Transaction.java

 Transaction POJO class

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
import za.ac.cput.domain.enums.PaymentMethod;
import za.ac.cput.domain.enums.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "`transaction`")
public class Transaction {
    //  Variables/Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long transactionId;

    @Column(nullable = false, name = "buyer_id")
    private long buyerId;

    @Column(nullable = false, name = "seller_id")
    private long sellerId;

    @Column(nullable = false, name = "listing_id")
    private long listingId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "payment_method")
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    //  Constructors
    protected Transaction() {
        // Required by JPA
    }

    private Transaction(Builder builder) {
        this.transactionId = builder.transactionId;
        this.buyerId = builder.buyerId;
        this.sellerId = builder.sellerId;
        this.listingId = builder.listingId;
        this.amount = builder.amount;
        this.paymentMethod = builder.paymentMethod;
        this.status = builder.status;
        this.createdAt = builder.createdAt;
        this.completedAt = builder.completedAt;
    }

    //  Getters
    public long getTransactionId() {
        return transactionId;
    }

    public long getBuyerId() {
        return buyerId;
    }

    public long getSellerId() {
        return sellerId;
    }

    public long getListingId() {
        return listingId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    //  toString
    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId=" + transactionId +
                ", buyerId=" + buyerId +
                ", sellerId=" + sellerId +
                ", listingId=" + listingId +
                ", amount=" + amount +
                ", paymentMethod=" + paymentMethod +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", completedAt=" + completedAt +
                '}';
    }

    //  Builder Class
    public static class Builder {

        //  Variables/Attributes
        private long transactionId;
        private long buyerId;
        private long sellerId;
        private long listingId;
        private BigDecimal amount;
        private PaymentMethod paymentMethod;
        private TransactionStatus status;
        private LocalDateTime createdAt;
        private LocalDateTime completedAt;

        //  Setters
        public Builder setTransactionId(long transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        public Builder setBuyerId(long buyerId) {
            this.buyerId = buyerId;
            return this;
        }

        public Builder setSellerId(long sellerId) {
            this.sellerId = sellerId;
            return this;
        }

        public Builder setListingId(long listingId) {
            this.listingId = listingId;
            return this;
        }

        public Builder setAmount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Builder setPaymentMethod(PaymentMethod paymentMethod) {
            this.paymentMethod = paymentMethod;
            return this;
        }

        public Builder setStatus(TransactionStatus status) {
            this.status = status;
            return this;
        }

        public Builder setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder setCompletedAt(LocalDateTime completedAt) {
            this.completedAt = completedAt;
            return this;
        }

        public Builder copy(Transaction transaction) {
            this.transactionId = transaction.transactionId;
            this.buyerId = transaction.buyerId;
            this.sellerId = transaction.sellerId;
            this.listingId = transaction.listingId;
            this.amount = transaction.amount;
            this.paymentMethod = transaction.paymentMethod;
            this.status = transaction.status;
            this.createdAt = transaction.createdAt;
            this.completedAt = transaction.completedAt;
            return this;
        }

        //  build method
        public Transaction build() {
            return new Transaction(this);
        }
    }
}