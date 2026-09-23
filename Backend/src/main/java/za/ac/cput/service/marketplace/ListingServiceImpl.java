/*
 ListingServiceImpl.java

 Business logic for Listing. Implements the generic CRUD contract
 IService<Listing, Long> plus the Listing-specific operations.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.service.marketplace;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import za.ac.cput.domain.enums.ListingStatus;
import za.ac.cput.domain.marketplace.Listing;
import za.ac.cput.repository.marketplace.ListingRepository;
import za.ac.cput.util.Helper;

@Service
public class ListingServiceImpl implements IListingService {

    private final ListingRepository repository;

    public ListingServiceImpl(ListingRepository repository) {
        this.repository = repository;
    }

    @Override
    public Listing create(Listing listing) {
        return this.repository.save(listing);
    }

    @Override
    public Listing read(Long id) {
        return id == null ? null : this.repository.findById(id).orElse(null);
    }

    @Override
    public Listing update(Listing listing) {
        return this.repository.save(listing);
    }

    @Override
    public boolean delete(Long id) {
        if (id == null || !this.repository.existsById(id)) {
            return false;
        }
        this.repository.deleteById(id);
        return true;
    }

    @Override
    public List<Listing> getAll() {
        return this.repository.findAll();
    }

    @Override
    public List<Listing> findBySellerId(long sellerId) {
        return this.repository.findBySellerId(sellerId);
    }

    @Override
    public List<Listing> search(Long campusId, Long categoryId, String title) {
        List<Listing> results = this.repository.findByStatus(ListingStatus.ACTIVE);
        if (campusId != null) {
            results = results.stream().filter(l -> l.getCampusId() == campusId).toList();
        }
        if (categoryId != null) {
            results = results.stream().filter(l -> l.getCategoryId() == categoryId).toList();
        }
        if (!Helper.isNullOrEmpty(title)) {
            String needle = title.toLowerCase();
            results = results.stream()
                    .filter(l -> l.getTitle() != null && l.getTitle().toLowerCase().contains(needle))
                    .toList();
        }
        return results;
    }

    @Override
    public Listing markAsSold(Long listingId) {
        Listing found = read(listingId);
        if (found == null) {
            return null;
        }
        return this.repository.save(new Listing.Builder()
                .copy(found)
                .setStatus(ListingStatus.SOLD)
                .setUpdatedAt(LocalDateTime.now())
                .build());
    }

}
