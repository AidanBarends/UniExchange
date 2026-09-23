/*
 UploadController.java

 Accepts an image file from any authenticated user and saves it to disk via
 LocalFileStorage, returning the URL it is now reachable at (served by
 WebConfig's /uploads/** resource handler). Listings and bulletin posts both
 store images as a plain URL string, so this is the one place that turns a
 file the user picked into a URL those existing endpoints can store - it does
 not touch ListingImage or BulletinPostImage itself.

 Author: Aidan Barends 230255639
 Date: 18 September 2026
*/

package za.ac.cput.controller;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import za.ac.cput.storage.LocalFileStorage;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {

    private final LocalFileStorage storage;

    public UploadController(LocalFileStorage storage) {
        this.storage = storage;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "No file was uploaded"));
        }

        String extension = this.storage.extensionFor(file.getContentType());
        if (extension == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Only PNG, JPEG, GIF or WEBP images are allowed"));
        }

        String filename = this.storage.save(file, extension);

        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/")
                .path(filename)
                .toUriString();

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("url", url));
    }

}
