/*
 ListingImageRepository.java

 Spring Data JPA repository for the ListingImage entity.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.repository.marketplace;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import za.ac.cput.domain.marketplace.ListingImage;

@Repository
public interface ListingImageRepository extends JpaRepository<ListingImage, Long> {

    List<ListingImage> findByListingIdOrderByPositionAsc(long listingId);

    List<ListingImage> findByListingIdAndIsPrimaryTrue(long listingId);

}
