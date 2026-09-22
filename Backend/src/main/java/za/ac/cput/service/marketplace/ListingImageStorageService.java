package za.ac.cput.service.marketplace;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ListingImageStorageService {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final Map<String, String> EXTENSIONS = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/gif", ".gif",
            "image/webp", ".webp");

    private final Path root;

    public ListingImageStorageService(
            @Value("${app.listing-images.upload-dir:uploads/listing-images}") String uploadDirectory) {
        this.root = Paths.get(uploadDirectory).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.root);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not create the listing image upload directory", exception);
        }
    }

    public String store(MultipartFile file) {
        validate(file);
        String fileName = fileName(file);
        Path destination = root.resolve(fileName).normalize();
        try {
            try (var inputStream = file.getInputStream()) {
                Files.copy(inputStream, destination);
            }
            return fileName;
        } catch (IOException exception) {
            throw new IllegalStateException("Could not store the listing image", exception);
        }
    }

    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please select an image to upload");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Image must be 10 MB or smaller");
        }

        String contentType = file.getContentType() == null
                ? ""
                : file.getContentType().toLowerCase(Locale.ROOT);
        String extension = EXTENSIONS.get(contentType);
        if (extension == null) {
            throw new IllegalArgumentException("Only JPEG, PNG, GIF, and WebP images are supported");
        }
    }

    public String fileName(MultipartFile file) {
        validate(file);
        String contentType = file.getContentType().toLowerCase(Locale.ROOT);
        String fileName = UUID.randomUUID() + EXTENSIONS.get(contentType);
        Path destination = root.resolve(fileName).normalize();
        if (!destination.startsWith(root)) {
            throw new IllegalArgumentException("Invalid image filename");
        }
        return fileName;
    }

    public Resource load(String fileName) {
        if (!StringUtils.hasText(fileName) || fileName.contains("/") || fileName.contains("\\")) {
            throw new IllegalArgumentException("Invalid image filename");
        }
        try {
            return new UrlResource(root.resolve(fileName).normalize().toUri());
        } catch (IOException exception) {
            throw new IllegalStateException("Could not load the listing image", exception);
        }
    }

    public MediaType mediaType(String fileName) {
        String lowerName = fileName.toLowerCase(Locale.ROOT);
        if (lowerName.endsWith(".png")) return MediaType.IMAGE_PNG;
        if (lowerName.endsWith(".gif")) return MediaType.IMAGE_GIF;
        if (lowerName.endsWith(".webp")) return MediaType.parseMediaType("image/webp");
        return MediaType.IMAGE_JPEG;
    }

    public void delete(String fileName) {
        try {
            Files.deleteIfExists(root.resolve(fileName).normalize());
        } catch (IOException exception) {
            throw new IllegalStateException("Could not remove the listing image", exception);
        }
    }
}
