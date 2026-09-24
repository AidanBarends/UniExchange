/*
 PayFastSignature.java

 Builds and checks the MD5 signature PayFast puts on redirect payments and on
 the callback it sends back.

 THE ONE THING THAT BREAKS JAVA INTEGRATIONS

 PayFast's reference implementation is PHP, and the signature is an MD5 over
 "key=urlencode(value)" pairs. PHP's urlencode() keeps only alphanumerics plus
 '-', '_' and '.', and turns a space into '+'. Java's URLEncoder keeps that set
 PLUS '*'. So:

     '*'  PHP encodes as %2A,  Java leaves it literal

 That one character is enough to produce a signature PayFast rejects, and '*' is
 perfectly ordinary in a listing title ("Calculator *sealed*"). Because most
 titles contain no asterisk, the integration works for weeks and then fails on a
 single payment - a miserable thing to debug. phpUrlEncode below is the whole fix,
 and PayFastSignatureTest pins the behaviour with an asterisk in the item name.

 (Do not reach for URLEncoder "just for the passphrase" either - the same rule
 applies to every value in the string.)

 Two further rules that are easy to get wrong:

  - Fields are signed in the order they are SENT, not alphabetically. (The
    subscription/refund REST API signs alphabetically; the redirect form does
    not. Do not share code between them.)
  - Empty values are skipped entirely, and the passphrase is appended LAST.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 24 September 2026
*/

package za.ac.cput.service.transactions;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;

public final class PayFastSignature {

    private PayFastSignature() {}

    /**
     * Signs an ordered map of form fields.
     *
     * @param fields     insertion-ordered; the order IS the contract
     * @param passphrase may be null or blank, in which case nothing is appended
     */
    public static String sign(LinkedHashMap<String, String> fields, String passphrase) {
        StringBuilder builder = new StringBuilder();

        for (Map.Entry<String, String> entry : fields.entrySet()) {
            String value = entry.getValue();
            // Skipped, not sent as empty: PayFast's own implementation drops empty
            // values before hashing, so including them changes the digest.
            if (value == null || value.isEmpty()) {
                continue;
            }
            builder.append(entry.getKey())
                    .append('=')
                    .append(phpUrlEncode(value.trim()))
                    .append('&');
        }

        if (passphrase != null && !passphrase.isBlank()) {
            builder.append("passphrase=").append(phpUrlEncode(passphrase.trim()));
        } else if (!builder.isEmpty()) {
            builder.setLength(builder.length() - 1);   // drop the trailing '&'
        }

        return md5(builder.toString());
    }

    /**
     * Rebuilds the signature over a callback exactly as it arrived.
     *
     * The map must preserve the order PayFast sent, and must stop at "signature" -
     * the signature cannot be part of what it signs.
     */
    public static String signNotification(LinkedHashMap<String, String> posted, String passphrase) {
        LinkedHashMap<String, String> signable = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : posted.entrySet()) {
            if ("signature".equals(entry.getKey())) {
                break;
            }
            signable.put(entry.getKey(), entry.getValue());
        }
        return sign(signable, passphrase);
    }

    /**
     * PHP's urlencode(), which is NOT Java's URLEncoder. See the class comment -
     * the '*' difference is the entire reason this method exists.
     */
    static String phpUrlEncode(String value) {
        StringBuilder out = new StringBuilder(value.length() * 2);

        for (byte b : value.getBytes(StandardCharsets.UTF_8)) {
            int c = b & 0xFF;
            boolean unreserved = (c >= 'A' && c <= 'Z')
                    || (c >= 'a' && c <= 'z')
                    || (c >= '0' && c <= '9')
                    || c == '-' || c == '_' || c == '.';

            if (unreserved) {
                out.append((char) c);
            } else if (c == ' ') {
                out.append('+');
            } else {
                // Uppercase hex, as PHP emits.
                out.append('%').append(HexFormat.of().withUpperCase().toHexDigits((byte) c));
            }
        }
        return out.toString();
    }

    private static String md5(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("MD5")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            // MD5 is required of every JRE, so this cannot happen.
            throw new IllegalStateException("MD5 is unavailable", e);
        }
    }

}
