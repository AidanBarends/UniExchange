/*
 PurchaseController.java

 Buying a listing with wallet money, held in escrow: /api/purchases.

 The generic /api/transactions CRUD controller is ADMIN-only, because it would
 otherwise let a student POST any transaction they liked - including a PENDING one
 with an invented amount, which would inflate their apparent escrow balance and
 break reconciliation. Every real purchase goes through here.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 24 September 2026
*/

package za.ac.cput.controller.transactions;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import za.ac.cput.domain.transactions.Transaction;
import za.ac.cput.dto.transactions.WalletDtos.PurchaseRequest;
import za.ac.cput.security.UniExchangeUserDetailsService.AuthenticatedUser;
import za.ac.cput.service.transactions.IEscrowService;

@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {

    private final IEscrowService escrowService;

    public PurchaseController(IEscrowService escrowService) {
        this.escrowService = escrowService;
    }

    /** Everything the signed-in student has bought or sold. */
    @GetMapping
    public List<Transaction> history(@AuthenticationPrincipal AuthenticatedUser principal) {
        return this.escrowService.historyFor(me(principal));
    }

    /**
     * Buys a listing. The money leaves the buyer's wallet now and is held until
     * they confirm receipt - the seller is not paid yet.
     */
    @PostMapping
    public ResponseEntity<Transaction> purchase(@RequestBody PurchaseRequest request,
                                                @AuthenticationPrincipal AuthenticatedUser principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.escrowService.purchase(
                me(principal), request.listingId(), request.expectedAmount()));
    }

    /** Buyer confirms the item arrived; this is what actually pays the seller. */
    @PostMapping("/{transactionId}/confirm")
    public Transaction confirm(@PathVariable long transactionId,
                               @AuthenticationPrincipal AuthenticatedUser principal) {
        return this.escrowService.confirmReceipt(me(principal), transactionId);
    }

    /** Either party calls off an open purchase; the buyer is refunded in full. */
    @PostMapping("/{transactionId}/cancel")
    public Transaction cancel(@PathVariable long transactionId,
                              @AuthenticationPrincipal AuthenticatedUser principal) {
        return this.escrowService.cancel(me(principal), transactionId);
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
            throw new AccessDeniedException("You must be signed in to use purchases");
        }
        return principal.getUser().getUserId();
    }

}
