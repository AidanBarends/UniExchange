/*
 RoleFactory.java

 Factory for Role. All construction goes through here so that every
 Role is validated with Helper before it exists - the entity itself
 exposes only a Builder and a protected JPA constructor.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.factory.identity;

import za.ac.cput.domain.enums.RoleType;
import za.ac.cput.domain.identity.Role;
import za.ac.cput.util.Helper;

public class RoleFactory {

    // Prevent instantiation - factory class
    private RoleFactory() {}

    public static Role createRole(RoleType name, String description) {
        if (!Helper.isValidObject(name)) {
            throw new IllegalArgumentException("Role: name is required");
        }

        return new Role.Builder()
                .setName(name)
                .setDescription(description)
                .build();
    }

    public static Role updateRole(Role existing, RoleType name, String description) {
        if (!Helper.isValidObject(existing)) {
            throw new IllegalArgumentException("Role: existing record is required for an update");
        }

        if (!Helper.isValidObject(name)) {
            throw new IllegalArgumentException("Role: name is required");
        }

        return new Role.Builder()
                .copy(existing)
                .setName(name)
                .setDescription(description)
                .build();
    }

}
