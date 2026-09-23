/*
 WalletFactory.java

 Factory for Wallet. All construction goes through here so that every
 Wallet is validated with Helper before it exists - the entity itself
 exposes only a Builder and a protected JPA constructor.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.factory.transactions;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import za.ac.cput.domain.transactions.Wallet;
import za.ac.cput.util.Helper;

public class WalletFactory {

    // Prevent instantiation - factory class
    private WalletFactory() {}

    public static Wallet createWallet(long userId, BigDecimal balance, String currency) {
        if (!Helper.isValidId(userId)) {
            throw new IllegalArgumentException("Wallet: userId must be a positive id");
        }

        if (!Helper.isValidBigDecimal(balance)) {
            throw new IllegalArgumentException("Wallet: balance must be a non-negative amount");
        }

        if (!Helper.isValidCurrency(currency)) {
            throw new IllegalArgumentException("Wallet: currency must be 3 uppercase letters, e.g. ZAR");
        }

        LocalDateTime now = LocalDateTime.now();

        return new Wallet.Builder()
                .setUserId(userId)
                .setBalance(balance)
                .setCurrency(currency)
                .setCreatedAt(now)
                .setUpdatedAt(now)
                .build();
    }

    public static Wallet updateWallet(Wallet existing, long userId, BigDecimal balance, String currency) {
        if (!Helper.isValidObject(existing)) {
            throw new IllegalArgumentException("Wallet: existing record is required for an update");
        }

        if (!Helper.isValidId(userId)) {
            throw new IllegalArgumentException("Wallet: userId must be a positive id");
        }

        if (!Helper.isValidBigDecimal(balance)) {
            throw new IllegalArgumentException("Wallet: balance must be a non-negative amount");
        }

        if (!Helper.isValidCurrency(currency)) {
            throw new IllegalArgumentException("Wallet: currency must be 3 uppercase letters, e.g. ZAR");
        }

        return new Wallet.Builder()
                .copy(existing)
                .setUserId(userId)
                .setBalance(balance)
                .setCurrency(currency)
                .setUpdatedAt(LocalDateTime.now())
                .build();
    }

}
