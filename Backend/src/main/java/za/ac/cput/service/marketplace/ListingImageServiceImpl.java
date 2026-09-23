/*
 ListingImageServiceImpl.java

 Business logic for ListingImage. Implements the generic CRUD contract
 IService<ListingImage, Long> plus the ListingImage-specific operations.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.service.marketplace;

import java.util.List;

import org.springframework.stereotype.Service;

import za.ac.cput.domain.marketplace.ListingImage;
import za.ac.cput.repository.marketplace.ListingImageRepository;

@Service
public class ListingImageServiceImpl implements IListingImageService {

    private final ListingImageRepository repository;

    public ListingImageServiceImpl(ListingImageRepository repository) {
        this.repository = repository;
    }

    @Override
    public ListingImage create(ListingImage listingImage) {
        return this.repository.save(listingImage);
    }

    @Override
    public ListingImage read(Long id) {
        return id == null ? null : this.repository.findById(id).orElse(null);
    }

    @Override
    public ListingImage update(ListingImage listingImage) {
        return this.repository.save(listingImage);
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
    public List<ListingImage> getAll() {
        return this.repository.findAll();
    }

    @Override
    public List<ListingImage> findByListingId(long listingId) {
        return this.repository.findByListingIdOrderByPositionAsc(listingId);
    }

    @Override
    public ListingImage findPrimaryForListing(long listingId) {
        return this.repository.findByListingIdAndIsPrimaryTrue(listingId).stream().findFirst().orElse(null);
    }

}
