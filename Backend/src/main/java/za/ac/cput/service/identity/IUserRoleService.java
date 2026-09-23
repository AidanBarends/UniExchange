/*
 IUserRoleService.java

 Service contract for UserRole.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.service.identity;

import java.util.List;

import za.ac.cput.domain.identity.UserRole;
import za.ac.cput.service.IService;

public interface IUserRoleService extends IService<UserRole, Long> {

    List<UserRole> findByUserId(long userId);

    UserRole assignRole(long userId, long roleId);

}
