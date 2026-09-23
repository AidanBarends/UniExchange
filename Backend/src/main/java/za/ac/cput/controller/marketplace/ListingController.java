/*
 ListingController.java

 REST endpoints for Listing.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.controller.marketplace;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import za.ac.cput.domain.marketplace.Listing;
import za.ac.cput.dto.marketplace.ListingRequest;
import za.ac.cput.factory.marketplace.ListingFactory;
import za.ac.cput.service.marketplace.IListingService;

@RestController
@RequestMapping("/api/listings")
public class ListingController {

    private final IListingService service;

    public ListingController(IListingService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Listing> create(@RequestBody ListingRequest request) {
        Listing created = this.service.create(ListingFactory.createListing(
                request.sellerId(), request.categoryId(), request.campusId(), request.title(),
                request.description(), request.price(), request.status()));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Listing> read(@PathVariable Long id) {
        Listing found = this.service.read(id);
        return found == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(found);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Listing> update(@PathVariable Long id, @RequestBody ListingRequest request) {
        Listing existing = this.service.read(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(this.service.update(ListingFactory.updateListing(
                existing, request.sellerId(), request.categoryId(), request.campusId(), request.title(),
                request.description(), request.price(), request.status())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return this.service.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping
    public List<Listing> getAll() {
        return this.service.getAll();
    }

    @GetMapping("/search")
    public List<Listing> search(@RequestParam(required = false) Long campusId,
                                @RequestParam(required = false) Long categoryId,
                                @RequestParam(required = false) String title) {
        return this.service.search(campusId, categoryId, title);
    }

    @GetMapping("/seller/{sellerId}")
    public List<Listing> bySeller(@PathVariable long sellerId) {
        return this.service.findBySellerId(sellerId);
    }

    @PatchMapping("/{id}/sold")
    public ResponseEntity<Listing> markSold(@PathVariable Long id) {
        Listing updated = this.service.markAsSold(id);
        return updated == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(updated);
    }

}
