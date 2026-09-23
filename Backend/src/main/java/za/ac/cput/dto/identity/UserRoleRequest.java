/*
 UserRoleRequest.java

 Inbound payload for creating/updating a UserRole. Entities have no public
 setters, so requests arrive as a record and are handed to UserRoleFactory.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.dto.identity;

public record UserRoleRequest(
        long userId,
        long roleId) {
}
