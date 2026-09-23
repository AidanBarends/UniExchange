/*
 UserRoleFactory.java

 Factory for UserRole. All construction goes through here so that every
 UserRole is validated with Helper before it exists - the entity itself
 exposes only a Builder and a protected JPA constructor.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.factory.identity;

import java.time.LocalDateTime;

import za.ac.cput.domain.identity.UserRole;
import za.ac.cput.util.Helper;

public class UserRoleFactory {

    // Prevent instantiation - factory class
    private UserRoleFactory() {}

    public static UserRole createUserRole(long userId, long roleId) {
        if (!Helper.isValidId(userId)) {
            throw new IllegalArgumentException("UserRole: userId must be a positive id");
        }

        if (!Helper.isValidId(roleId)) {
            throw new IllegalArgumentException("UserRole: roleId must be a positive id");
        }

        LocalDateTime now = LocalDateTime.now();

        return new UserRole.Builder()
                .setUserId(userId)
                .setRoleId(roleId)
                .setAssignedAt(now)
                .build();
    }

    public static UserRole updateUserRole(UserRole existing, long userId, long roleId) {
        if (!Helper.isValidObject(existing)) {
            throw new IllegalArgumentException("UserRole: existing record is required for an update");
        }

        if (!Helper.isValidId(userId)) {
            throw new IllegalArgumentException("UserRole: userId must be a positive id");
        }

        if (!Helper.isValidId(roleId)) {
            throw new IllegalArgumentException("UserRole: roleId must be a positive id");
        }

        return new UserRole.Builder()
                .copy(existing)
                .setUserId(userId)
                .setRoleId(roleId)
                .build();
    }

}
