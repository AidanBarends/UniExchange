/*
 WalletTopUp.java

 WalletTopUp POJO class - one attempt to put real money into a wallet via PayFast.

 Payment cannot be reused for this: its transactionId is NOT NULL and a top-up
 has no marketplace transaction behind it - nobody is buying anything, the
 student is funding their own balance.

 The two unique columns are what make the callback safe to receive more than
 once, which PayFast guarantees will happen (a PENDING notification is followed
 by another when the status settles):

   merchantPaymentId - our id, sent as m_payment_id and echoed back
   pfPaymentId       - PayFast's id, null until they tell us, and unique so one
                       payment can never end up split across two rows

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 24 September 2026
*/

package za.ac.cput.domain.transactions;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import za.ac.cput.domain.enums.PaymentStatus;

@Entity
@Table(name = "wallet_top_up", indexes = {
        @Index(name = "idx_topup_user_status", columnList = "user_id, status")
})
public class WalletTopUp {
    //  Variables/Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long topUpId;

    @Column(nullable = false, name = "user_id")
    private long userId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    /** Our reference, sent to PayFast as m_payment_id. A UUID, so unguessable. */
    @Column(nullable = false, unique = true, length = 64, name = "merchant_payment_id")
    private String merchantPaymentId;

    /** PayFast's own id for the payment. Null until the callback arrives. */
    @Column(unique = true, length = 64, name = "pf_payment_id")
    private String pfPaymentId;

    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    //  Constructors
    protected WalletTopUp() {
        // Required by JPA
    }

    private WalletTopUp(Builder builder) {
        this.topUpId = builder.topUpId;
        this.userId = builder.userId;
        this.amount = builder.amount;
        this.status = builder.status;
        this.merchantPaymentId = builder.merchantPaymentId;
        this.pfPaymentId = builder.pfPaymentId;
        this.createdAt = builder.createdAt;
        this.completedAt = builder.completedAt;
    }

    //  Getters
    public long getTopUpId() {
        return topUpId;
    }

    public long getUserId() {
        return userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getMerchantPaymentId() {
        return merchantPaymentId;
    }

    public String getPfPaymentId() {
        return pfPaymentId;
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
        return "WalletTopUp{" +
                "topUpId=" + topUpId +
                ", userId=" + userId +
                ", amount=" + amount +
                ", status=" + status +
                ", merchantPaymentId='" + merchantPaymentId + '\'' +
                ", pfPaymentId='" + pfPaymentId + '\'' +
                ", createdAt=" + createdAt +
                ", completedAt=" + completedAt +
                '}';
    }

    //  Builder Class
    public static class Builder {

        //  Variables/Attributes
        private long topUpId;
        private long userId;
        private BigDecimal amount;
        private PaymentStatus status;
        private String merchantPaymentId;
        private String pfPaymentId;
        private LocalDateTime createdAt;
        private LocalDateTime completedAt;

        //  Setters
        public Builder setTopUpId(long topUpId) {
            this.topUpId = topUpId;
            return this;
        }

        public Builder setUserId(long userId) {
            this.userId = userId;
            return this;
        }

        public Builder setAmount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Builder setStatus(PaymentStatus status) {
            this.status = status;
            return this;
        }

        public Builder setMerchantPaymentId(String merchantPaymentId) {
            this.merchantPaymentId = merchantPaymentId;
            return this;
        }

        public Builder setPfPaymentId(String pfPaymentId) {
            this.pfPaymentId = pfPaymentId;
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

        public Builder copy(WalletTopUp walletTopUp) {
            this.topUpId = walletTopUp.topUpId;
            this.userId = walletTopUp.userId;
            this.amount = walletTopUp.amount;
            this.status = walletTopUp.status;
            this.merchantPaymentId = walletTopUp.merchantPaymentId;
            this.pfPaymentId = walletTopUp.pfPaymentId;
            this.createdAt = walletTopUp.createdAt;
            this.completedAt = walletTopUp.completedAt;
            return this;
        }

        //  build method
        public WalletTopUp build() {
            return new WalletTopUp(this);
        }
    }
}
