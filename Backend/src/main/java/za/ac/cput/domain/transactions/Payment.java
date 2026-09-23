/*
 Payment.java

 Payment POJO class

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
import za.ac.cput.domain.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
public class Payment {
    //  Variables/Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long paymentId;

    @Column(nullable = false, name = "transaction_id")
    private long transactionId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(length = 100, name = "external_reference")
    private String externalReference;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;

    //  Constructors
    protected Payment() {
        // Required by JPA
    }

    private Payment(Builder builder) {
        this.paymentId = builder.paymentId;
        this.transactionId = builder.transactionId;
        this.amount = builder.amount;
        this.method = builder.method;
        this.status = builder.status;
        this.externalReference = builder.externalReference;
        this.paidAt = builder.paidAt;
        this.createdAt = builder.createdAt;
    }

    //  Getters
    public long getPaymentId() {
        return paymentId;
    }

    public long getTransactionId() {
        return transactionId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getExternalReference() {
        return externalReference;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    //  toString
    @Override
    public String toString() {
        return "Payment{" +
                "paymentId=" + paymentId +
                ", transactionId=" + transactionId +
                ", amount=" + amount +
                ", method=" + method +
                ", status=" + status +
                ", externalReference='" + externalReference + '\'' +
                ", paidAt=" + paidAt +
                ", createdAt=" + createdAt +
                '}';
    }

    //  Builder Class
    public static class Builder {

        //  Variables/Attributes
        private long paymentId;
        private long transactionId;
        private BigDecimal amount;
        private PaymentMethod method;
        private PaymentStatus status;
        private String externalReference;
        private LocalDateTime paidAt;
        private LocalDateTime createdAt;

        //  Setters
        public Builder setPaymentId(long paymentId) {
            this.paymentId = paymentId;
            return this;
        }

        public Builder setTransactionId(long transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        public Builder setAmount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Builder setMethod(PaymentMethod method) {
            this.method = method;
            return this;
        }

        public Builder setStatus(PaymentStatus status) {
            this.status = status;
            return this;
        }

        public Builder setExternalReference(String externalReference) {
            this.externalReference = externalReference;
            return this;
        }

        public Builder setPaidAt(LocalDateTime paidAt) {
            this.paidAt = paidAt;
            return this;
        }

        public Builder setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder copy(Payment payment) {
            this.paymentId = payment.paymentId;
            this.transactionId = payment.transactionId;
            this.amount = payment.amount;
            this.method = payment.method;
            this.status = payment.status;
            this.externalReference = payment.externalReference;
            this.paidAt = payment.paidAt;
            this.createdAt = payment.createdAt;
            return this;
        }

        //  build method
        public Payment build() {
            return new Payment(this);
        }
    }
}