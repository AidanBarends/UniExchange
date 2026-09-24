/*
 WalletTopUpFactory.java

 Factory for WalletTopUp. All construction goes through here so that every
 WalletTopUp is validated with Helper before it exists - the entity itself
 exposes only a Builder and a protected JPA constructor.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 24 September 2026
*/

package za.ac.cput.factory.transactions;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import za.ac.cput.domain.enums.PaymentStatus;
import za.ac.cput.domain.transactions.WalletTopUp;
import za.ac.cput.util.Helper;

public class WalletTopUpFactory {

    // Prevent instantiation - factory class
    private WalletTopUpFactory() {}

    /**
     * A new, unpaid top-up. The merchant reference is generated here rather than
     * taken from the caller so it is always unique and never guessable - PayFast
     * echoes it back in the callback, and it is the key the callback is matched on.
     */
    public static WalletTopUp createWalletTopUp(long userId, BigDecimal amount) {
        if (!Helper.isValidId(userId)) {
            throw new IllegalArgumentException("WalletTopUp: userId must be a positive id");
        }

        if (!Helper.isPositiveMoney(amount)) {
            throw new IllegalArgumentException(
                    "WalletTopUp: amount must be greater than zero with at most 2 decimal places");
        }

        return new WalletTopUp.Builder()
                .setUserId(userId)
                .setAmount(amount)
                .setStatus(PaymentStatus.PENDING)
                .setMerchantPaymentId(UUID.randomUUID().toString())
                .setCreatedAt(LocalDateTime.now())
                .build();
    }

}
