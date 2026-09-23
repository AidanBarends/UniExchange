/*
 CampusRepository.java

 Spring Data JPA repository for the Campus entity.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.repository.identity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import za.ac.cput.domain.identity.Campus;

@Repository
public interface CampusRepository extends JpaRepository<Campus, Long> {

    List<Campus> findByNameIgnoreCase(String name);

    List<Campus> findByCity(String city);

}
