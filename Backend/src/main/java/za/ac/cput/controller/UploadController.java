/*
 UploadController.java

 Accepts an image file from any authenticated user and saves it to disk
 under app.uploads.dir, returning the URL it is now reachable at (served by
 WebConfig's /uploads/** resource handler). Listings and bulletin posts both
 store images as a plain URL string, so this is the one place that turns a
 file the user picked into a URL those existing endpoints can store - it does
 not touch ListingImage or BulletinPostImage itself.

 Author: Aidan Barends 230255639
 Date: 18 September 2026
*/

package za.ac.cput.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {

    private static final Map<String, String> ALLOWED_TYPES = Map.of(
            "image/png", ".png",
            "image/jpeg", ".jpg",
            "image/gif", ".gif",
            "image/webp", ".webp");

    private final Path uploadsDir;

    public UploadController(@Value("${app.uploads.dir:uploads}") String uploadsDir) {
        this.uploadsDir = Path.of(uploadsDir).toAbsolutePath().normalize();
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "No file was uploaded"));
        }

        String extension = ALLOWED_TYPES.get(file.getContentType());
        if (extension == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Only PNG, JPEG, GIF or WEBP images are allowed"));
        }

        Files.createDirectories(this.uploadsDir);

        String filename = UUID.randomUUID() + extension;
        file.transferTo(this.uploadsDir.resolve(filename));

        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/")
                .path(filename)
                .toUriString();

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("url", url));
    }

}
