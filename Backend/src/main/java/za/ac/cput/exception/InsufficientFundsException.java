/*
 InsufficientFundsException.java

 Raised when a wallet debit would take the balance below zero.

 A dedicated type rather than IllegalArgumentException because the two mean
 different things to the caller: a bad amount is the client's mistake (400),
 while "you do not have enough money right now" is a perfectly well-formed
 request that conflicts with current state (409). The frontend needs to tell
 those apart to show "Top up to continue" instead of a validation error, so
 GlobalExceptionHandler gives this one the code INSUFFICIENT_FUNDS.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 24 September 2026
*/

package za.ac.cput.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends RuntimeException {

    private final BigDecimal balance;
    private final BigDecimal required;

    public InsufficientFundsException(BigDecimal balance, BigDecimal required) {
        super("Insufficient funds: balance is " + balance + " but " + required + " is required");
        this.balance = balance;
        this.required = required;
    }

    public BigDecimal getBalance() {
        return this.balance;
    }

    public BigDecimal getRequired() {
        return this.required;
    }

}
