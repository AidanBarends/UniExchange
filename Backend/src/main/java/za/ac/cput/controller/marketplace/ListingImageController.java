/*
 ListingImageController.java

 REST endpoints for ListingImage.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.controller.marketplace;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import za.ac.cput.domain.marketplace.ListingImage;
import za.ac.cput.dto.marketplace.ListingImageRequest;
import za.ac.cput.factory.marketplace.ListingImageFactory;
import za.ac.cput.service.marketplace.IListingImageService;

@RestController
@RequestMapping("/api/listing-images")
public class ListingImageController {

    private final IListingImageService service;

    public ListingImageController(IListingImageService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ListingImage> create(@RequestBody ListingImageRequest request) {
        ListingImage created = this.service.create(ListingImageFactory.createListingImage(
                request.listingId(), request.imageUrl(), request.position(), request.isPrimary()));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ListingImage> read(@PathVariable Long id) {
        ListingImage found = this.service.read(id);
        return found == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(found);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ListingImage> update(@PathVariable Long id,
                                               @RequestBody ListingImageRequest request) {
        ListingImage existing = this.service.read(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(this.service.update(ListingImageFactory.updateListingImage(
                existing, request.listingId(), request.imageUrl(), request.position(), request.isPrimary())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return this.service.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping
    public List<ListingImage> getAll() {
        return this.service.getAll();
    }

    @GetMapping("/listing/{listingId}")
    public List<ListingImage> byListing(@PathVariable long listingId) {
        return this.service.findByListingId(listingId);
    }

}
