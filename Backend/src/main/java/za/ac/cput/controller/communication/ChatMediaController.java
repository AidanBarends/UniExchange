/*
 ChatMediaController.java

 Streams a chat attachment to a student who is allowed to see it.

 This endpoint is permitAll in SecurityConfig, which looks alarming and is not.
 Browsers cannot attach an Authorization header to an <img>, <audio> or <video>
 src, so a filter-chain rule would 401 every attachment on the page. Instead the
 URL itself carries a short-lived HMAC bound to one file and one viewer
 (MediaLinkSigner), and membership of the conversation is re-checked here on
 every single request. That is strictly stronger than "any signed-in student",
 which is all the filter chain could have offered.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 24 September 2026
*/

package za.ac.cput.controller.communication;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import za.ac.cput.domain.communication.ChatMedia;
import za.ac.cput.security.MediaLinkSigner;
import za.ac.cput.service.communication.IChatService;
import za.ac.cput.storage.ChatMediaStorage;

@RestController
@RequestMapping("/api/chat/media")
public class ChatMediaController {

    private final IChatService chatService;
    private final ChatMediaStorage storage;
    private final MediaLinkSigner linkSigner;

    public ChatMediaController(IChatService chatService,
                               ChatMediaStorage storage,
                               MediaLinkSigner linkSigner) {
        this.chatService = chatService;
        this.storage = storage;
        this.linkSigner = linkSigner;
    }

    @GetMapping("/{mediaId}")
    public ResponseEntity<Resource> stream(@PathVariable long mediaId,
                                           @RequestParam("u") long viewerId,
                                           @RequestParam("exp") long expiresAt,
                                           @RequestParam("sig") String signature) {

        // Throws AccessDeniedException -> 403 via GlobalExceptionHandler.
        this.linkSigner.verify(mediaId, viewerId, expiresAt, signature);

        ChatMedia media = this.chatService.mediaForViewer(mediaId, viewerId);
        if (media == null) {
            // 404 for both "no such media" and "not your conversation". A 403 for the
            // second case would turn this endpoint into an oracle for which
            // conversations exist.
            return ResponseEntity.notFound().build();
        }

        Path path = this.storage.resolve(media.getStorageKey());
        if (!Files.isReadable(path)) {
            return ResponseEntity.notFound().build();
        }

        /*
         Three things here are load-bearing for media playback, and each fails
         silently if changed:

         - The status must stay 200. Spring's HttpEntityMethodProcessor only applies
           Range handling when the response is still 200 when it runs; returning
           PARTIAL_CONTENT here skips the conversion entirely and emits a 206 with
           no Content-Range and the whole body, which Chrome tolerates and Safari
           does not.
         - The body must be a seekable Resource. FileSystemResource knows its length
           and can be read repeatedly; an InputStreamResource cannot, and breaks
           range handling.
         - No .contentLength(). Entity headers are copied onto the response before
           the converter runs, and for a multi-range request the converter writes
           multipart/byteranges without a single Content-Length - leaving a stale
           value that makes the browser wait for bytes that never arrive.

         Accept-Ranges and Content-Range are set by Spring, not by us.
        */
        return ResponseEntity.ok()
                // The stored type, which was confirmed against the file's magic bytes
                // at upload. Never the client's declared type.
                .contentType(MediaType.parseMediaType(media.getMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        // Built rather than concatenated: a filename containing CR/LF
                        // would otherwise be response-header injection.
                        .filename(media.getOriginalFilename() == null
                                ? "attachment"
                                : media.getOriginalFilename(), StandardCharsets.UTF_8)
                        .build().toString())
                // Stops a browser re-sniffing a mislabelled body into something executable.
                .header("X-Content-Type-Options", "nosniff")
                // Neutralises anything that slipped past the whitelist if someone
                // navigates directly to this URL rather than loading it in a tag.
                .header("Content-Security-Policy", "default-src 'none'; sandbox")
                // private: this is one student's attachment and must never be held in
                // a shared or intermediary cache. The lifetime matches the signed
                // link's bucketed window so a cached copy never outlives its URL.
                .cacheControl(CacheControl.maxAge(Duration.ofMinutes(30)).cachePrivate())
                .body(new FileSystemResource(path));
    }

}
