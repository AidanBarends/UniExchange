/*
 TrustedSellerBadgeRepository.java

 Spring Data JPA repository for the TrustedSellerBadge entity.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.repository.trust;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import za.ac.cput.domain.trust.TrustedSellerBadge;

@Repository
public interface TrustedSellerBadgeRepository extends JpaRepository<TrustedSellerBadge, Long> {

    Optional<TrustedSellerBadge> findByUserId(long userId);

}
