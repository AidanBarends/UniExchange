/*
 ListingRepository.java

 Spring Data JPA repository for the Listing entity.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.repository.marketplace;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import za.ac.cput.domain.enums.ListingStatus;
import za.ac.cput.domain.marketplace.Listing;

@Repository
public interface ListingRepository extends JpaRepository<Listing, Long> {

    List<Listing> findBySellerId(long sellerId);

    List<Listing> findByCategoryId(long categoryId);

    List<Listing> findByCampusId(long campusId);

    List<Listing> findByStatus(ListingStatus status);

    List<Listing> findByTitleContainingIgnoreCase(String title);

    List<Listing> findByStatusAndCampusId(ListingStatus status, long campusId);

}
