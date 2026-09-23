/*
 IListingService.java

 Service contract for Listing.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.service.marketplace;

import java.util.List;

import za.ac.cput.domain.marketplace.Listing;
import za.ac.cput.service.IService;

public interface IListingService extends IService<Listing, Long> {

    List<Listing> findBySellerId(long sellerId);

    List<Listing> search(Long campusId, Long categoryId, String title);

    Listing markAsSold(Long listingId);

}
