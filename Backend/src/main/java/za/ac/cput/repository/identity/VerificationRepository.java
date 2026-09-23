/*
 VerificationRepository.java

 Spring Data JPA repository for the Verification entity.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.repository.identity;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import za.ac.cput.domain.enums.VerificationType;
import za.ac.cput.domain.identity.Verification;

@Repository
public interface VerificationRepository extends JpaRepository<Verification, Long> {

    Optional<Verification> findByToken(String token);

    List<Verification> findByUserIdAndVerificationType(long userId, VerificationType verificationType);

}
