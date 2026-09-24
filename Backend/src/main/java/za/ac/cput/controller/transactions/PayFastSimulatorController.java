/*
 PayFastSimulatorController.java

 A local stand-in for PayFast's callback, so the wallet can be demonstrated
 without a public URL.

 WHY IT EXISTS

 PayFast confirms a payment by POSTing to notify_url from their own servers. That
 URL has to be reachable from the internet, which localhost never is - so on a
 laptop a top-up starts, the student pays in the sandbox, and the money never
 arrives. The usual fix is a tunnel (ngrok http 8080). This endpoint is the
 alternative: it marks a pending top-up complete directly, so a demo works on a
 train with no network.

 WHY IT IS SAFE

 It credits wallets, so it is exactly the endpoint an attacker would want. Three
 things keep it shut:

   1. It is OFF unless app.payfast.simulator.enabled is explicitly true. The
      default is false, so the bean does not exist in a default build and the
      route 404s.
   2. @Profile("!prod") means it cannot be switched on in production even by
      setting that property.
   3. A student may only complete THEIR OWN pending top-up. So even with it
      enabled, the worst case is a student topping up their own demo wallet for
      free - which is the entire point of the endpoint - rather than being able
      to touch anyone else's balance.

 Point 3 replaces an earlier ADMIN-only rule. That was strictly safer on paper
 and useless in practice: the person demonstrating the wallet is signed in as a
 student, so the endpoint they need could never be called. A gate nobody can pass
 is not security, it is a broken feature that pushes people to disable something
 else instead.

 It also goes through the same PayFastService.applyCompletedTopUp as the real
 callback, so what a demo exercises is the real crediting path - including its
 idempotency - rather than a shortcut around it.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 24 September 2026
*/

package za.ac.cput.controller.transactions;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import za.ac.cput.domain.transactions.WalletTopUp;
import za.ac.cput.repository.transactions.WalletTopUpRepository;
import za.ac.cput.security.UniExchangeUserDetailsService.AuthenticatedUser;
import za.ac.cput.service.transactions.PayFastService;

@RestController
@RequestMapping("/api/dev/payfast")
@Profile("!prod")
@ConditionalOnProperty(name = "app.payfast.simulator.enabled", havingValue = "true")
public class PayFastSimulatorController {

    private static final Logger log = LoggerFactory.getLogger(PayFastSimulatorController.class);

    private final PayFastService payFastService;
    private final WalletTopUpRepository topUpRepository;

    public PayFastSimulatorController(PayFastService payFastService,
                                      WalletTopUpRepository topUpRepository) {
        this.payFastService = payFastService;
        this.topUpRepository = topUpRepository;
        log.warn("PayFast SIMULATOR is enabled - wallet top-ups can be completed without paying. "
                + "This must never be switched on outside local development.");
    }

    /**
     * Completes YOUR OWN pending top-up as though PayFast had confirmed it.
     *
     * @param merchantPaymentId the m_payment_id from POST /api/wallet/topup
     */
    @PostMapping("/complete/{merchantPaymentId}")
    public ResponseEntity<Map<String, String>> complete(
            @PathVariable String merchantPaymentId,
            @AuthenticationPrincipal AuthenticatedUser principal) {

        if (principal == null || principal.getUser() == null) {
            throw new AccessDeniedException("You must be signed in to complete a top-up");
        }
        long me = principal.getUser().getUserId();

        WalletTopUp topUp = this.topUpRepository.findByMerchantPaymentId(merchantPaymentId)
                .orElseThrow(() -> new IllegalArgumentException("No such top-up"));

        // The whole safety story of this endpoint. Without it, a student could
        // complete anyone's pending top-up, which would be a way to move money
        // they do not own.
        if (topUp.getUserId() != me) {
            throw new AccessDeniedException("That top-up is not yours");
        }

        log.warn("SIMULATED PayFast completion for {} by user {}", merchantPaymentId, me);

        // The real crediting path, including the compare-and-set that makes a
        // repeated callback a no-op - so calling this twice credits once, exactly
        // as a duplicate ITN would.
        this.payFastService.applyCompletedTopUp(merchantPaymentId, "SIMULATED-" + merchantPaymentId);

        return ResponseEntity.ok(Map.of(
                "status", "completed",
                "merchantPaymentId", merchantPaymentId,
                "note", "Simulated locally - no money moved, and PayFast was not contacted."));
    }

}
