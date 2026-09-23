/*
 RoleRequest.java

 Inbound payload for creating/updating a Role. Entities have no public
 setters, so requests arrive as a record and are handed to RoleFactory.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.dto.identity;

import za.ac.cput.domain.enums.RoleType;

public record RoleRequest(
        RoleType name,
        String description) {
}
