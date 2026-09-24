/*
 ChatMediaStorage.java

 Owns the chat-attachment directory (app.chat.media.dir): validates an upload,
 saves it under a server-generated name, and resolves it back for streaming.

 Deliberately separate from LocalFileStorage even though the two look alike.
 LocalFileStorage writes into a directory that WebConfig serves publicly at
 /uploads/**; chat attachments must never live there. A student's voice note is
 private, so it is kept outside the public tree and only ever reaches the browser
 through ChatMediaController, which checks the signature and the conversation
 membership first.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 24 September 2026
*/

package za.ac.cput.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import za.ac.cput.domain.enums.ChatMediaType;

@Component
public class ChatMediaStorage {

    private static final Logger log = LoggerFactory.getLogger(ChatMediaStorage.class);

    /*
     Only formats that are inert when a browser renders them inline.

     image/svg+xml is absent on purpose: SVG is a scriptable document, and while an
     <img> tag will not run its script, navigating straight to the media URL would.
     Given the file is served from the API origin, that would be stored XSS.
    */
    private static final Map<String, String> ALLOWED_TYPES = Map.ofEntries(
            Map.entry("image/jpeg", ".jpg"),
            Map.entry("image/png", ".png"),
            Map.entry("image/gif", ".gif"),
            Map.entry("image/webp", ".webp"),
            Map.entry("audio/webm", ".weba"),
            Map.entry("audio/ogg", ".ogg"),
            Map.entry("audio/mp4", ".m4a"),
            Map.entry("audio/mpeg", ".mp3"),
            Map.entry("video/mp4", ".mp4"),
            Map.entry("video/webm", ".webm"));

    /** Exactly what save() generates: a UUID plus a whitelisted extension. */
    private static final Pattern STORAGE_KEY = Pattern.compile("^[0-9a-fA-F-]{36}\\.[a-z0-9]{2,5}$");

    private final Path baseDir;

    public ChatMediaStorage(@Value("${app.chat.media.dir:chat-media}") String baseDir) {
        this.baseDir = Path.of(baseDir).toAbsolutePath().normalize();
    }

    /** Null when the type is not one we accept. */
    public String extensionFor(String contentType) {
        return contentType == null ? null : ALLOWED_TYPES.get(contentType.toLowerCase());
    }

    public ChatMediaType mediaTypeFor(String contentType) {
        if (contentType == null) return null;
        String type = contentType.toLowerCase();
        if (type.startsWith("image/")) return ChatMediaType.IMAGE;
        if (type.startsWith("video/")) return ChatMediaType.VIDEO;
        if (type.startsWith("audio/")) return ChatMediaType.AUDIO;
        return null;
    }

    /**
     * Confirms the bytes really are what the client claimed.
     *
     * MultipartFile.getContentType() is just a header the uploader wrote, and it is
     * trivially forged with curl. That matters more here than for listing images,
     * because the stored type is echoed back as the Content-Type when the file is
     * streamed - so an unchecked value turns a forged header into stored XSS.
     */
    public boolean contentMatchesType(MultipartFile file, String declaredType) throws IOException {
        byte[] head = new byte[16];
        int read;
        try (InputStream in = file.getInputStream()) {
            read = in.readNBytes(head, 0, head.length);
        }
        if (read < 12) {
            return false;
        }

        return switch (declaredType.toLowerCase()) {
            case "image/jpeg" -> startsWith(head, 0xFF, 0xD8, 0xFF);
            case "image/png" -> startsWith(head, 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A);
            case "image/gif" -> startsWith(head, 'G', 'I', 'F', '8');
            case "image/webp" -> startsWith(head, 'R', 'I', 'F', 'F') && matchesAt(head, 8, 'W', 'E', 'B', 'P');
            // WebM and Matroska share the EBML magic, and audio/webm is
            // indistinguishable from video/webm this early in the file. Both are
            // inert containers, so accepting either for either is fine.
            case "audio/webm", "video/webm" -> startsWith(head, 0x1A, 0x45, 0xDF, 0xA3);
            // ISO base media: the ftyp box starts at offset 4, after its length.
            case "audio/mp4", "video/mp4" -> matchesAt(head, 4, 'f', 't', 'y', 'p');
            case "audio/ogg" -> startsWith(head, 'O', 'g', 'g', 'S');
            case "audio/mpeg" -> startsWith(head, 'I', 'D', '3')
                    || (head[0] == (byte) 0xFF && (head[1] & 0xE0) == 0xE0);
            default -> false;
        };
    }

    /** Saves under a fresh random name and returns that name - never the uploader's. */
    public String save(MultipartFile file, String extension) throws IOException {
        Files.createDirectories(this.baseDir);
        String storageKey = UUID.randomUUID() + extension;
        file.transferTo(this.baseDir.resolve(storageKey));
        return storageKey;
    }

    /**
     * Resolves a stored key to a path on disk.
     *
     * The key is server-generated, so traversal is already impossible by
     * construction - but this is the read path, and it takes its input from the
     * database. If a key ever became attacker-influenced through some other bug,
     * the pattern check and the startsWith guard stop that turning into arbitrary
     * file disclosure.
     */
    public Path resolve(String storageKey) {
        if (storageKey == null || !STORAGE_KEY.matcher(storageKey).matches()) {
            throw new IllegalArgumentException("ChatMedia: malformed storage key");
        }
        Path resolved = this.baseDir.resolve(storageKey).normalize();
        if (!resolved.startsWith(this.baseDir)) {
            throw new IllegalArgumentException("ChatMedia: storage key escapes the media directory");
        }
        return resolved;
    }

    /** Best effort - the row is what matters, a stray file is only wasted disk. */
    public void deleteQuietly(String storageKey) {
        try {
            Files.deleteIfExists(resolve(storageKey));
        } catch (IOException | IllegalArgumentException e) {
            log.warn("Could not delete chat media {}: {}", storageKey, e.getMessage());
        }
    }

    private static boolean startsWith(byte[] data, int... expected) {
        return matchesAt(data, 0, expected);
    }

    private static boolean matchesAt(byte[] data, int offset, int... expected) {
        if (data.length < offset + expected.length) return false;
        for (int i = 0; i < expected.length; i++) {
            if (data[offset + i] != (byte) expected[i]) return false;
        }
        return true;
    }

}
