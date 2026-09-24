/*
 PayFastController.java

 Receives PayFast's Instant Transaction Notification.

 This endpoint is permitAll in SecurityConfig. It has to be: the request comes
 from PayFast's servers, not from a browser, and carries no JWT. Without that rule
 every notification would 401, PayFast would retry indefinitely, and no top-up
 would ever be credited. PayFastService authenticates the caller instead - by
 signature, by source address, and by asking PayFast to confirm it.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 24 September 2026
*/

package za.ac.cput.controller.transactions;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import za.ac.cput.service.transactions.PayFastService;

@RestController
@RequestMapping("/api/payfast")
public class PayFastController {

    private static final Logger log = LoggerFactory.getLogger(PayFastController.class);

    private final PayFastService payFastService;

    public PayFastController(PayFastService payFastService) {
        this.payFastService = payFastService;
    }

    @PostMapping(path = "/itn", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Void> itn(HttpServletRequest request) {
        try {
            /*
             Read the RAW body rather than taking @RequestParam Map.

             The signature is computed over the fields in the order they were sent,
             and a parameter map gives no ordering guarantee (it also collapses
             repeated keys). Parsing the body ourselves into a LinkedHashMap is the
             only way to reproduce that order faithfully.

             This does mean nothing earlier in the filter chain may call
             getParameter() on this request - doing so consumes the stream and
             leaves the body empty here.
            */
            String raw = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
            log.info("PayFast ITN received from {} ({} bytes)", request.getRemoteAddr(), raw.length());
            this.payFastService.handleNotification(parseOrdered(raw), request.getRemoteAddr());
        } catch (IOException e) {
            log.warn("Could not read PayFast ITN body: {}", e.getMessage());
        } catch (Exception e) {
            // Swallowed deliberately - see the 200 below.
            log.error("PayFast ITN handling failed", e);
        }

        /*
         Always 200, even for a notification we rejected.

         Any other status makes PayFast retry. Retries are harmless for a genuine
         notification because applying one is idempotent, but retrying a
         permanently-invalid one forever helps nobody and buries the logs.
        */
        return ResponseEntity.ok().build();
    }

    private static LinkedHashMap<String, String> parseOrdered(String body) {
        LinkedHashMap<String, String> fields = new LinkedHashMap<>();
        if (body == null || body.isBlank()) {
            return fields;
        }
        for (String pair : body.split("&")) {
            int equals = pair.indexOf('=');
            if (equals < 0) continue;
            String key = URLDecoder.decode(pair.substring(0, equals), StandardCharsets.UTF_8);
            String value = URLDecoder.decode(pair.substring(equals + 1), StandardCharsets.UTF_8);
            fields.put(key, value);
        }
        return fields;
    }

}
