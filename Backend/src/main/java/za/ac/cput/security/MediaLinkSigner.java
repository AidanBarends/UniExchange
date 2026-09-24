/*
 MediaLinkSigner.java

 Mints and verifies the short-lived signed URLs that chat attachments are served
 under.

 WHY THIS EXISTS AT ALL

 Chat media has to be private, but <img>, <audio> and <video> fetch their src
 through the browser's own loader, which cannot attach an Authorization header -
 and this app is stateless JWT with no cookies. So a normal authenticated
 endpoint would return 401 to every media tag on the page. Downloading the bytes
 with fetch() and handing the element a blob: URL does work, but it forces the
 whole file into memory before playback starts and makes range requests
 impossible, which defeats seeking in a video.

 A signed URL solves it: the capability travels in the query string, so no header
 is needed. Three properties keep that safe:

   - it is bound to one viewer and one file, so a leaked link is not a general key
   - it expires
   - ChatMediaController still re-checks conversation participation on every single
     request, so removing someone from a thread cuts their access immediately
     rather than whenever the link happens to expire

 The JWT itself is deliberately NOT used as the query parameter. A remembered
 token lasts 30 days and query strings end up in browser history, Referer headers
 and Azure's HTTP logs.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 24 September 2026
*/

package za.ac.cput.security;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class MediaLinkSigner {

    private static final Duration WINDOW = Duration.ofMinutes(30);

    private final SecretKeySpec key;

    public MediaLinkSigner(@Value("${app.media.link-secret:${app.jwt.secret}}") String secret) {
        this.key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    /**
     * A relative, signed URL for this file and this viewer.
     *
     * The caller appends nothing: the frontend uses the string exactly as given.
     */
    public String urlFor(long mediaId, long viewerId) {
        long expiresAt = bucketedExpiry();
        return "/api/chat/media/" + mediaId
                + "?u=" + viewerId
                + "&exp=" + expiresAt
                + "&sig=" + sign(mediaId, viewerId, expiresAt);
    }

    /**
     * @throws AccessDeniedException if the link is expired or the signature does not match
     */
    public void verify(long mediaId, long viewerId, long expiresAt, String signature) {
        if (expiresAt < Instant.now().getEpochSecond()) {
            throw new AccessDeniedException("This media link has expired");
        }

        byte[] expected = sign(mediaId, viewerId, expiresAt).getBytes(StandardCharsets.US_ASCII);
        byte[] supplied = signature == null
                ? new byte[0]
                : signature.getBytes(StandardCharsets.US_ASCII);

        // Constant-time. A plain String.equals() leaks how many leading characters
        // were right, which is enough to forge a signature one byte at a time.
        if (!MessageDigest.isEqual(expected, supplied)) {
            throw new AccessDeniedException("Invalid media link");
        }
    }

    /*
     Rounded up to the next whole window rather than "now plus 30 minutes", which
     matters more than it looks.

     The chat page re-polls every few seconds and re-renders the same attachments.
     With a per-request expiry the URL string would differ every time, so the
     browser's cache - which is keyed on the full URL - would miss on every poll
     and re-download every photo in the thread. Bucketing makes the URL
     byte-identical for everyone inside the window, so Cache-Control does its job
     and React's re-render of an unchanged src is a no-op.

     +2 windows, not +1, so a link minted at the very end of a window still has a
     useful life ahead of it - 30 to 60 minutes rather than a few seconds. That
     also keeps a link valid long enough to finish scrubbing through a video.
    */
    private static long bucketedExpiry() {
        long window = WINDOW.toSeconds();
        return ((Instant.now().getEpochSecond() / window) + 2) * window;
    }

    private String sign(long mediaId, long viewerId, long expiresAt) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(this.key);
            byte[] raw = mac.doFinal(
                    (mediaId + ":" + viewerId + ":" + expiresAt).getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Could not sign media link", e);
        }
    }

}
