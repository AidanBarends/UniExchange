/*
 TrustedDeviceRepository.java

 TrustedDevice repository interface

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 05 September 2026
*/

package za.ac.cput.repository.identity;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import za.ac.cput.domain.identity.TrustedDevice;

@Repository
public interface TrustedDeviceRepository extends JpaRepository<TrustedDevice, Long> {

    /*
     The lookup that replaces a password-style comparison. The stored value is a
     SHA-256 of 256 random bits, so finding the row IS the check - there is
     nothing to guess. A salted hash would make this query impossible.
    */
    Optional<TrustedDevice> findByTokenHash(String tokenHash);

    List<TrustedDevice> findByUserId(long userId);

}
