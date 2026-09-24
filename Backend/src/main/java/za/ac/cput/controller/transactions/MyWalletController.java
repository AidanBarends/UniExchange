/*
 MyWalletController.java

 The student's own wallet: /api/wallet (singular).

 Distinct from WalletController (/api/wallets, plural), which is the generic CRUD
 stack and is ADMIN-only. Nothing here takes a userId from the client - the wallet
 you get is always the wallet of whoever holds the token.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 24 September 2026
*/

package za.ac.cput.controller.transactions;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import za.ac.cput.domain.transactions.Wallet;
import za.ac.cput.domain.transactions.WalletTransaction;
import za.ac.cput.dto.transactions.WalletDtos.PayFastRedirect;
import za.ac.cput.dto.transactions.WalletDtos.TopUpRequest;
import za.ac.cput.dto.transactions.WalletDtos.TransferRequest;
import za.ac.cput.dto.transactions.WalletDtos.TransferResult;
import za.ac.cput.dto.transactions.WalletDtos.WalletSummary;
import za.ac.cput.repository.transactions.WalletTransactionRepository;
import za.ac.cput.security.UniExchangeUserDetailsService.AuthenticatedUser;
import za.ac.cput.service.transactions.IEscrowService;
import za.ac.cput.service.transactions.ITransferService;
import za.ac.cput.service.transactions.IWalletService;
import za.ac.cput.service.transactions.PayFastService;

@RestController
@RequestMapping("/api/wallet")
public class MyWalletController {

    private final IWalletService walletService;
    private final IEscrowService escrowService;
    private final PayFastService payFastService;
    private final WalletTransactionRepository ledgerRepository;
    private final ITransferService transferService;

    public MyWalletController(IWalletService walletService,
                              IEscrowService escrowService,
                              PayFastService payFastService,
                              WalletTransactionRepository ledgerRepository,
                              ITransferService transferService) {
        this.walletService = walletService;
        this.escrowService = escrowService;
        this.payFastService = payFastService;
        this.ledgerRepository = ledgerRepository;
        this.transferService = transferService;
    }

    @GetMapping
    public WalletSummary summary(@AuthenticationPrincipal AuthenticatedUser principal) {
        long userId = me(principal);
        Wallet wallet = this.walletService.getOrCreateForUser(userId);
        BigDecimal held = this.escrowService.heldFor(userId);

        return new WalletSummary(
                wallet.getBalance(),
                held,
                wallet.getBalance().add(held),
                wallet.getCurrency(),
                this.payFastService.topUpMode());
    }

    /** Every movement, newest first - the audit trail behind the balance. */
    @GetMapping("/ledger")
    public List<WalletTransaction> ledger(@AuthenticationPrincipal AuthenticatedUser principal) {
        Wallet wallet = this.walletService.getOrCreateForUser(me(principal));
        return this.ledgerRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getWalletId());
    }

    /**
     * Starts a PayFast top-up and hands back the form to submit.
     *
     * Nothing is credited here. The wallet only moves when PayFast's server-to-server
     * notification arrives at /api/payfast/itn and passes every check - the student
     * returning to the site proves nothing, since anyone can open the return URL.
     */
    @PostMapping("/topup")
    public ResponseEntity<PayFastRedirect> topUp(@RequestBody TopUpRequest request,
                                                 @AuthenticationPrincipal AuthenticatedUser principal) {
        LinkedHashMap<String, String> fields = this.payFastService.beginTopUp(me(principal), request.amount());

        return ResponseEntity.ok(new PayFastRedirect(
                this.payFastService.processUrl(),
                fields,
                fields.get("m_payment_id"),
                this.payFastService.isSimulatorEnabled()));
    }

    /** Sends money to another student. The sender is whoever holds the token, never the body. */
    @PostMapping("/transfer")
    public TransferResult transfer(@RequestBody TransferRequest request,
                                   @AuthenticationPrincipal AuthenticatedUser principal) {
        return this.transferService.send(me(principal), request.recipientEmail(), request.amount());
    }

    /*
     Guarded rather than dereferenced straight away. The JWT filter always puts an
     AuthenticatedUser in the context, so on the normal path this never triggers -
     but if the principal is ever absent or some other type, a bare
     principal.getUser() throws NullPointerException and the student gets a 500
     with a stack trace where they should get a plain "sign in again".
    */
    private static long me(AuthenticatedUser principal) {
        if (principal == null || principal.getUser() == null) {
            throw new AccessDeniedException("You must be signed in to use your wallet");
        }
        return principal.getUser().getUserId();
    }

}
