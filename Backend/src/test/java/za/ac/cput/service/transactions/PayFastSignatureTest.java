/*
 PayFastSignatureTest.java

 Pins the PHP-compatible URL encoding that PayFast signatures depend on.

 The asterisk case is the whole point of this file. Java's URLEncoder leaves '*'
 literal while PHP's urlencode() writes %2A, so an integration built on
 URLEncoder produces a correct signature for most item names and a rejected one
 for anything containing an asterisk. That is the kind of bug that ships.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 24 September 2026
*/

package za.ac.cput.service.transactions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;

import org.junit.jupiter.api.Test;

class PayFastSignatureTest {

    @Test
    void encodesAsteriskThePhpWayNotTheJavaWay() {
        assertEquals("%2A", PayFastSignature.phpUrlEncode("*"),
                "PHP urlencode() writes %2A; Java's URLEncoder leaves '*' alone");

        // Guards the regression directly: if someone swaps in URLEncoder, this fails.
        assertNotEquals(URLEncoder.encode("*", StandardCharsets.UTF_8),
                PayFastSignature.phpUrlEncode("*"));
    }

    @Test
    void encodesSpacesAsPlusAndUsesUppercaseHex() {
        assertEquals("Calculator+%2Asealed%2A", PayFastSignature.phpUrlEncode("Calculator *sealed*"));
        assertEquals("a%2Fb", PayFastSignature.phpUrlEncode("a/b"));
    }

    @Test
    void leavesTheUnreservedSetAlone() {
        assertEquals("abcXYZ019-_.", PayFastSignature.phpUrlEncode("abcXYZ019-_."));
    }

    @Test
    void skipsEmptyValuesSoTheDigestMatchesPayFast() {
        LinkedHashMap<String, String> withEmpty = new LinkedHashMap<>();
        withEmpty.put("merchant_id", "10000100");
        withEmpty.put("name_first", "");
        withEmpty.put("amount", "100.00");

        LinkedHashMap<String, String> without = new LinkedHashMap<>();
        without.put("merchant_id", "10000100");
        without.put("amount", "100.00");

        assertEquals(PayFastSignature.sign(without, null), PayFastSignature.sign(withEmpty, null));
    }

    @Test
    void fieldOrderChangesTheSignature() {
        LinkedHashMap<String, String> one = new LinkedHashMap<>();
        one.put("merchant_id", "10000100");
        one.put("amount", "100.00");

        LinkedHashMap<String, String> other = new LinkedHashMap<>();
        other.put("amount", "100.00");
        other.put("merchant_id", "10000100");

        // PayFast signs the fields in the order they are SENT, so a map that does not
        // preserve insertion order silently breaks the integration.
        assertNotEquals(PayFastSignature.sign(one, null), PayFastSignature.sign(other, null));
    }

    @Test
    void passphraseIsAppendedLastAndChangesTheSignature() {
        LinkedHashMap<String, String> fields = new LinkedHashMap<>();
        fields.put("merchant_id", "10000100");
        fields.put("amount", "100.00");

        assertNotEquals(PayFastSignature.sign(fields, null),
                PayFastSignature.sign(fields, "jt7NOE43FZPn"));
    }

    @Test
    void notificationSignatureStopsAtTheSignatureField() {
        LinkedHashMap<String, String> posted = new LinkedHashMap<>();
        posted.put("m_payment_id", "abc");
        posted.put("amount_gross", "100.00");
        posted.put("signature", "whatever-payfast-sent");

        LinkedHashMap<String, String> withoutSignature = new LinkedHashMap<>();
        withoutSignature.put("m_payment_id", "abc");
        withoutSignature.put("amount_gross", "100.00");

        // The signature can never be part of what it signs.
        assertEquals(PayFastSignature.sign(withoutSignature, null),
                PayFastSignature.signNotification(posted, null));
    }

}
