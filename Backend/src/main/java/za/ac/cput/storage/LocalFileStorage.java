/*
 LocalFileStorage.java

 Owns the uploads directory (app.uploads.dir): saves a file UploadController
 receives, and removes one a listing/bulletin post's image pointed at once
 that image row is gone - so deleting a post or listing doesn't leave its
 photos behind as dead weight on disk. deleteIfManaged is a no-op for any URL
 that isn't one of ours (an external link a student pasted rather than
 uploaded), since there is nothing on this disk to remove for those.

 Author: Aidan Barends 230255639
 Date: 21 September 2026
*/

package za.ac.cput.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class LocalFileStorage {

    private static final Logger log = LoggerFactory.getLogger(LocalFileStorage.class);

    private static final Map<String, String> ALLOWED_TYPES = Map.of(
            "image/png", ".png",
            "image/jpeg", ".jpg",
            "image/gif", ".gif",
            "image/webp", ".webp");

    /** The path segment UploadController serves saved files under - see WebConfig. */
    private static final String URL_MARKER = "/uploads/";

    private final Path uploadsDir;

    public LocalFileStorage(@Value("${app.uploads.dir:uploads}") String uploadsDir) {
        this.uploadsDir = Path.of(uploadsDir).toAbsolutePath().normalize();
    }

    /** Null when the content type is not one of the accepted image types. */
    public String extensionFor(String contentType) {
        return ALLOWED_TYPES.get(contentType);
    }

    /** Saves the file under a fresh random name and returns that name (not a URL - the caller builds that). */
    public String save(MultipartFile file, String extension) throws IOException {
        Files.createDirectories(this.uploadsDir);
        String filename = UUID.randomUUID() + extension;
        file.transferTo(this.uploadsDir.resolve(filename));
        return filename;
    }

    /**
     * Deletes the file an /uploads/... URL points at, if it is one of ours. Best-effort:
     * a missing file or one outside our control is not an error the caller needs to see,
     * since the database row is the thing that actually matters and is removed regardless.
     */
    public void deleteIfManaged(String url) {
        if (url == null) return;

        int markerIndex = url.indexOf(URL_MARKER);
        if (markerIndex < 0) return;

        String filename = url.substring(markerIndex + URL_MARKER.length());
        if (filename.isBlank() || filename.contains("/") || filename.contains("\\") || filename.contains("..")) {
            return;
        }

        try {
            Files.deleteIfExists(this.uploadsDir.resolve(filename));
        } catch (IOException e) {
            log.warn("Could not delete uploaded file {}: {}", filename, e.getMessage());
        }
    }

}
