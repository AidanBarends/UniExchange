/*
 ListingRepository.java

 Spring Data JPA repository for the Listing entity.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.repository.marketplace;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /*
     Claims a listing for one buyer, atomically.

     This is the ONLY thing standing between two students buying the same item.
     Locking the wallet does not help here: the two buyers debit two DIFFERENT
     wallet rows, so there is no contention on that side at all - the contested
     resource is the listing. A read-then-write would let both buyers pass the
     "is it still ACTIVE?" check before either of them writes.

     The row count is the authority: 1 means this caller claimed it, 0 means
     someone else got there first and the purchase must fail.

     No new ListingStatus constant is introduced for this (there is no RESERVED).
     Hibernate maps @Enumerated(STRING) to a native MySQL enum column, and
     ddl-auto=update will never alter it - so a new constant would pass every test
     against H2's freshly created schema and then be rejected by the deployed
     database. ACTIVE -> SOLD on purchase, and back to ACTIVE if the sale is
     cancelled, stays within the constants that already exist.
    */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Listing l set l.status = :to, l.updatedAt = :at " +
           "where l.listingId = :id and l.status = :from")
    int compareAndSetStatus(@Param("id") long listingId,
                            @Param("from") ListingStatus from,
                            @Param("to") ListingStatus to,
                            @Param("at") LocalDateTime at);

}
