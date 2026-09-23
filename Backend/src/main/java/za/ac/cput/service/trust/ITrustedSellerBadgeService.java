/*
 ITrustedSellerBadgeService.java

 Service contract for TrustedSellerBadge.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.service.trust;

import za.ac.cput.domain.trust.TrustedSellerBadge;
import za.ac.cput.service.IService;

public interface ITrustedSellerBadgeService extends IService<TrustedSellerBadge, Long> {

    TrustedSellerBadge findByUserId(long userId);

    TrustedSellerBadge revoke(Long trustedSellerBadgeId);

}
