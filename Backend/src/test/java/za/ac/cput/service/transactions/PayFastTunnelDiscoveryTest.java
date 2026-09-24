/*
 PayFastTunnelDiscoveryTest.java

 Reading the notify URL from cloudflared's /quicktunnel reply. The hostname ends
 up inside a signed PayFast field, so anything that is not a plain hostname must
 be refused rather than passed through.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 24 September 2026
*/

package za.ac.cput.service.transactions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class PayFastTunnelDiscoveryTest {

    @Test
    void buildsTheItnUrlFromTheQuickTunnelHostname() {
        assertEquals("https://parliament-reflections-exact-persistent.trycloudflare.com/api/payfast/itn",
                PayFastService.notifyUrlFromQuickTunnel(
                        "{\"hostname\":\"parliament-reflections-exact-persistent.trycloudflare.com\"}"));
    }

    @Test
    void toleratesWhitespaceInTheReply() {
        assertEquals("https://a-b.trycloudflare.com/api/payfast/itn",
                PayFastService.notifyUrlFromQuickTunnel("{ \"hostname\" : \"a-b.trycloudflare.com\" }\n"));
    }

    @Test
    void returnsNullWhenThereIsNoHostnameYet() {
        assertNull(PayFastService.notifyUrlFromQuickTunnel("{\"hostname\":\"\"}"));
        assertNull(PayFastService.notifyUrlFromQuickTunnel("{}"));
        assertNull(PayFastService.notifyUrlFromQuickTunnel(null));
    }

    @Test
    void refusesAnythingThatIsNotAPlainHostname() {
        assertNull(PayFastService.notifyUrlFromQuickTunnel(
                "{\"hostname\":\"evil.example/steal?x=\"}"));
    }

}
