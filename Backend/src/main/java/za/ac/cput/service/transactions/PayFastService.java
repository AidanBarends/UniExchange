/*
 PayFastService.java

 PayFast sandbox integration: builds the redirect form for a wallet top-up, and
 validates + applies the Instant Transaction Notification that follows.

 THE CALLBACK IS THE ONLY SOURCE OF TRUTH

 return_url is just where the student's browser lands afterwards. Anyone can open
 it without paying, so it must never credit anything. Only a validated ITN moves
 money.

 ORDERING MATTERS

 Validation talks to PayFast over the network (step 4 below). That must happen
 OUTSIDE any database transaction - holding a row lock across an HTTP call turns
 a slow payment gateway into a stalled database.

   1. signature      - rebuilt over the raw body, in the order received
   2. source IP      - resolved from PayFast's hostnames at runtime
   3. amount         - compared to what we recorded, within PayFast's tolerance
   4. confirmation   - post it all back and expect "VALID"
   then, and only then, a short transaction credits the wallet.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 24 September 2026
*/

package za.ac.cput.service.transactions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import za.ac.cput.domain.enums.PaymentStatus;
import za.ac.cput.domain.identity.User;
import za.ac.cput.domain.transactions.WalletTopUp;
import za.ac.cput.dto.transactions.WalletDtos;
import za.ac.cput.factory.transactions.WalletTopUpFactory;
import za.ac.cput.repository.identity.UserRepository;
import za.ac.cput.repository.transactions.WalletTopUpRepository;

@Service
public class PayFastService {

    private static final Logger log = LoggerFactory.getLogger(PayFastService.class);

    /** PayFast compares money with a tolerance rather than exactly. */
    private static final BigDecimal AMOUNT_TOLERANCE = new BigDecimal("0.01");

    private static final Duration CONFIRM_TIMEOUT = Duration.ofSeconds(15);

    /*
     PayFast publish no IP ranges, only hostnames, and they change. Both of their
     own reference implementations resolve these at runtime and compare the peer
     address against the union.
    */
    private static final List<String> PAYFAST_HOSTS = List.of(
            "www.payfast.co.za",
            "sandbox.payfast.co.za",
            "w1w.payfast.co.za",
            "w2w.payfast.co.za");

    private final WalletTopUpRepository topUpRepository;
    private final UserRepository userRepository;
    private final IWalletService walletService;
    private final HttpClient httpClient;

    private final boolean sandbox;
    private final String merchantId;
    private final String merchantKey;
    private final String passphrase;
    private final String returnUrl;
    private final String cancelUrl;
    private final String notifyUrl;
    private final boolean validateSourceIp;
    private final boolean simulatorEnabled;

    public PayFastService(WalletTopUpRepository topUpRepository,
                          UserRepository userRepository,
                          IWalletService walletService,
                          @Value("${app.payfast.sandbox:true}") boolean sandbox,
                          @Value("${app.payfast.merchant-id:10000100}") String merchantId,
                          @Value("${app.payfast.merchant-key:46f0cd694581a}") String merchantKey,
                          @Value("${app.payfast.passphrase:}") String passphrase,
                          @Value("${app.payfast.return-url:http://localhost:5173/wallet?topup=done}") String returnUrl,
                          @Value("${app.payfast.cancel-url:http://localhost:5173/wallet?topup=cancelled}") String cancelUrl,
                          @Value("${app.payfast.notify-url:http://localhost:8080/api/payfast/itn}") String notifyUrl,
                          @Value("${app.payfast.validate-source-ip:true}") boolean validateSourceIp,
                          @Value("${app.payfast.simulator.enabled:false}") boolean simulatorEnabled) {
        this.topUpRepository = topUpRepository;
        this.userRepository = userRepository;
        this.walletService = walletService;
        this.httpClient = HttpClient.newBuilder().connectTimeout(CONFIRM_TIMEOUT).build();
        this.sandbox = sandbox;
        this.merchantId = merchantId;
        this.merchantKey = merchantKey;
        this.passphrase = passphrase;
        this.returnUrl = returnUrl;
        this.cancelUrl = cancelUrl;
        this.notifyUrl = notifyUrl;
        this.validateSourceIp = validateSourceIp;
        this.simulatorEnabled = simulatorEnabled;
    }

    /** True only in local development with the ITN simulator switched on. */
    public boolean isSimulatorEnabled() {
        return this.simulatorEnabled;
    }

    /**
     * What will actually happen when a student tops up here.
     *
     * The simulator wins over sandbox, because when it is on the student never
     * reaches PayFast at all - telling them about a payment screen they will not
     * see is worse than saying nothing.
     */
    public WalletDtos.TopUpMode topUpMode() {
        if (this.simulatorEnabled) return WalletDtos.TopUpMode.SIMULATED;
        return this.sandbox ? WalletDtos.TopUpMode.SANDBOX : WalletDtos.TopUpMode.LIVE;
    }

    public String processUrl() {
        return baseUrl() + "/eng/process";
    }

    private String baseUrl() {
        return this.sandbox ? "https://sandbox.payfast.co.za" : "https://www.payfast.co.za";
    }

    /**
     * Records a pending top-up and returns the exact fields to POST to PayFast.
     *
     * The frontend renders these as a self-submitting form rather than a link:
     * PayFast's redirect endpoint takes a POST, and the signature covers the field
     * values, so nothing may be altered on the way.
     */
    @Transactional
    public LinkedHashMap<String, String> beginTopUp(long userId, BigDecimal amount) {
        User user = this.userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Top-up: no such user"));

        BigDecimal scaled = amount == null
                ? null
                : amount.setScale(2, RoundingMode.HALF_UP);

        WalletTopUp topUp = this.topUpRepository.save(
                WalletTopUpFactory.createWalletTopUp(userId, scaled));

        /*
         Insertion order IS the signature contract - PayFast hashes the fields in
         the order they are sent, so this must stay a LinkedHashMap and the order
         below must not be rearranged.
        */
        LinkedHashMap<String, String> fields = new LinkedHashMap<>();
        fields.put("merchant_id", this.merchantId);
        fields.put("merchant_key", this.merchantKey);
        fields.put("return_url", this.returnUrl);
        fields.put("cancel_url", this.cancelUrl);
        fields.put("notify_url", this.notifyUrl);
        fields.put("name_first", user.getFirstName());
        fields.put("name_last", user.getLastName());
        fields.put("email_address", user.getEmail());
        fields.put("m_payment_id", topUp.getMerchantPaymentId());
        // Plain string with exactly two decimals - "100.00", never "100.0" or "1,00".
        fields.put("amount", topUp.getAmount().toPlainString());
        fields.put("item_name", "UniExchange wallet top-up");

        fields.put("signature", PayFastSignature.sign(fields, this.passphrase));
        return fields;
    }

    /**
     * Validates a callback and credits the wallet if it is genuine and new.
     *
     * Returns quietly on anything invalid: the controller always answers 200
     * regardless, because a non-2xx makes PayFast retry, and retrying a
     * permanently-invalid notification helps nobody.
     *
     * @param posted the form fields in the order they arrived
     * @param sourceIp the peer address of the request
     */
    public void handleNotification(LinkedHashMap<String, String> posted, String sourceIp) {
        String merchantPaymentId = posted.get("m_payment_id");
        if (merchantPaymentId == null) {
            log.warn("PayFast ITN without m_payment_id - ignoring");
            return;
        }

        // 1. Signature.
        String expected = PayFastSignature.signNotification(posted, this.passphrase);
        if (!expected.equalsIgnoreCase(posted.get("signature"))) {
            log.warn("PayFast ITN for {} failed signature check", merchantPaymentId);
            return;
        }

        // 2. Source. Disabled by property for local testing through a tunnel, where
        //    the peer address is the tunnel's rather than PayFast's.
        if (this.validateSourceIp && !isFromPayFast(sourceIp)) {
            log.warn("PayFast ITN for {} came from unexpected address {}", merchantPaymentId, sourceIp);
            return;
        }

        WalletTopUp topUp = this.topUpRepository.findByMerchantPaymentId(merchantPaymentId).orElse(null);
        if (topUp == null) {
            log.warn("PayFast ITN for unknown payment {}", merchantPaymentId);
            return;
        }

        // 3. Amount. compareTo, never equals - BigDecimal.equals is scale sensitive,
        //    and PayFast sends "100.00" where we may hold "100.0".
        BigDecimal grossPaid = parseAmount(posted.get("amount_gross"));
        if (grossPaid == null
                || topUp.getAmount().subtract(grossPaid).abs().compareTo(AMOUNT_TOLERANCE) > 0) {
            log.warn("PayFast ITN for {} had amount {} but we expected {}",
                    merchantPaymentId, grossPaid, topUp.getAmount());
            return;
        }

        if (!this.merchantId.equals(posted.get("merchant_id"))) {
            log.warn("PayFast ITN for {} was for a different merchant", merchantPaymentId);
            return;
        }

        String paymentStatus = posted.get("payment_status");
        if (!"COMPLETE".equals(paymentStatus)) {
            // PENDING is normal and means another notification follows. FAILED is
            // terminal. Neither credits anything.
            log.info("PayFast ITN for {} has status {} - not crediting", merchantPaymentId, paymentStatus);
            return;
        }

        // 4. Ask PayFast whether they really sent this. Outside any transaction.
        if (!confirmWithPayFast(posted)) {
            log.warn("PayFast did not confirm ITN for {}", merchantPaymentId);
            return;
        }

        applyCompletedTopUp(merchantPaymentId, posted.get("pf_payment_id"));
    }

    /**
     * The only part that touches the database, kept as short as possible.
     *
     * The compare-and-set is what makes this idempotent: PayFast sends the same
     * notification more than once by design, and only the caller that actually
     * moves the row from PENDING to COMPLETED credits the wallet.
     */
    @Transactional
    public void applyCompletedTopUp(String merchantPaymentId, String pfPaymentId) {
        WalletTopUp topUp = this.topUpRepository.findByMerchantPaymentId(merchantPaymentId)
                .orElseThrow(() -> new IllegalStateException("Top-up vanished: " + merchantPaymentId));

        // Read what we need BEFORE the CAS: clearAutomatically detaches everything
        // in the persistence context, so touching topUp afterwards is unreliable.
        long userId = topUp.getUserId();
        BigDecimal amount = topUp.getAmount();
        long topUpId = topUp.getTopUpId();

        int updated = this.topUpRepository.compareAndSetStatus(merchantPaymentId,
                PaymentStatus.PENDING, PaymentStatus.COMPLETED, pfPaymentId, LocalDateTime.now());

        if (updated != 1) {
            log.info("PayFast ITN for {} was already applied - ignoring duplicate", merchantPaymentId);
            return;
        }

        this.walletService.credit(userId, amount, "TOPUP", topUpId, "Wallet top-up via PayFast");
        log.info("Credited {} to user {} from PayFast top-up {}", amount, userId, merchantPaymentId);
    }

    private boolean confirmWithPayFast(LinkedHashMap<String, String> posted) {
        StringBuilder body = new StringBuilder();
        posted.forEach((key, value) -> {
            if (!body.isEmpty()) body.append('&');
            body.append(key).append('=').append(PayFastSignature.phpUrlEncode(value));
        });

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl() + "/eng/query/validate"))
                    .timeout(CONFIRM_TIMEOUT)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = this.httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            // First line, case-insensitive: the documented reply is the bare word
            // VALID, but the body has carried trailing whitespace in the past.
            String first = response.body() == null ? "" : response.body().strip().lines()
                    .findFirst().orElse("");
            return "VALID".equalsIgnoreCase(first.strip());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            log.warn("Could not confirm ITN with PayFast: {}", e.getMessage());
            return false;
        }
    }

    private boolean isFromPayFast(String sourceIp) {
        if (sourceIp == null) return false;

        Set<String> allowed = new HashSet<>();
        for (String host : PAYFAST_HOSTS) {
            try {
                for (InetAddress address : InetAddress.getAllByName(host)) {
                    allowed.add(address.getHostAddress());
                }
            } catch (UnknownHostException e) {
                log.warn("Could not resolve PayFast host {}", host);
            }
        }
        return allowed.contains(sourceIp);
    }

    private static BigDecimal parseAmount(String raw) {
        try {
            return raw == null ? null : new BigDecimal(raw.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

}
