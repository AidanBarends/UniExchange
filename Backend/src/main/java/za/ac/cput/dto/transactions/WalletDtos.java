/*
 WalletDtos.java

 Request and response shapes for /api/wallet, /api/purchases and /api/payfast.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 24 September 2026
*/

package za.ac.cput.dto.transactions;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;

import za.ac.cput.domain.transactions.WalletTransaction;

public final class WalletDtos {

    private WalletDtos() {}

    /**
     * The wallet summary.
     *
     * available is the real, spendable balance and is ALREADY net of anything in
     * escrow - money leaves the buyer's wallet the moment they buy. held is shown
     * alongside it so the student can see where the difference went, and total is
     * available + held. Do not subtract held from available in the UI; that would
     * deduct it twice.
     */
    public record WalletSummary(BigDecimal available, BigDecimal held, BigDecimal total,
                                String currency,
                                /*
                                 Which top-up path this environment uses, so the UI can say
                                 what will actually happen BEFORE the student commits rather
                                 than describing a PayFast screen they may never see.
                                */
                                TopUpMode topUpMode) {}

    /** How a top-up completes in this environment. */
    public enum TopUpMode {
        /** Real PayFast, real money. Only when app.payfast.sandbox=false. */
        LIVE,
        /** PayFast's test environment. A real payment screen, but no money moves. */
        SANDBOX,
        /** Local development: completed in-app, PayFast never contacted. */
        SIMULATED
    }

    public record TopUpRequest(BigDecimal amount) {}

    /**
     * What the browser must POST to PayFast.
     *
     * Returned as data rather than a redirect because the signature covers the
     * field values: the frontend renders a hidden self-submitting form so nothing
     * is altered in transit.
     *
     * @param merchantPaymentId our reference for this attempt, so the UI can ask
     *                          about it afterwards
     * @param simulatorEnabled  true only in local development with the simulator
     *                          switched on, in which case the UI offers to complete
     *                          the top-up itself instead of sending the student to
     *                          PayFast - whose callback could never reach localhost
     */
    public record PayFastRedirect(String processUrl,
                                  LinkedHashMap<String, String> fields,
                                  String merchantPaymentId,
                                  boolean simulatorEnabled) {}

    public record LedgerPage(List<WalletTransaction> entries) {}

    /** Body of POST /api/purchases. expectedAmount is what the buyer was shown. */
    public record PurchaseRequest(long listingId, BigDecimal expectedAmount) {}

    /** Body of POST /api/wallet/transfer. The sender is always the token holder. */
    public record TransferRequest(String recipientEmail, BigDecimal amount) {}

    /** balanceAfter is the SENDER's new balance; nothing about the recipient's wallet is revealed. */
    public record TransferResult(BigDecimal amount, String recipientName, BigDecimal balanceAfter) {}

}
