/*
 UserRoleRepository.java

 Spring Data JPA repository for the UserRole entity.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.repository.identity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import za.ac.cput.domain.identity.UserRole;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    List<UserRole> findByUserId(long userId);

    List<UserRole> findByRoleId(long roleId);

    boolean existsByUserIdAndRoleId(long userId, long roleId);

}
