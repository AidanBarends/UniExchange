/*
 IUserService.java

 Service contract for User.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.service.identity;

import java.util.List;

import za.ac.cput.domain.enums.AccountStatus;
import za.ac.cput.domain.identity.User;
import za.ac.cput.service.IService;

public interface IUserService extends IService<User, Long> {

    User findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByAccountStatus(AccountStatus accountStatus);

}
