/*
 IRoleService.java

 Service contract for Role.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.service.identity;

import za.ac.cput.domain.enums.RoleType;
import za.ac.cput.domain.identity.Role;
import za.ac.cput.service.IService;

public interface IRoleService extends IService<Role, Long> {

    Role findByName(RoleType name);

}
