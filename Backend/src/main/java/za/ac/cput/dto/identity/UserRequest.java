/*
 UserRequest.java

 Inbound payload for creating/updating a User. Entities have no public
 setters, so requests arrive as a record and are handed to UserFactory.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.dto.identity;

import java.time.LocalDate;

import za.ac.cput.domain.enums.AccountStatus;

public record UserRequest(
        String email,
        String firstName,
        String middleName,
        String lastName,
        String cellPhone,
        String passwordHash,
        LocalDate dateOfBirth,
        AccountStatus accountStatus,
        Long campusId) {
}
