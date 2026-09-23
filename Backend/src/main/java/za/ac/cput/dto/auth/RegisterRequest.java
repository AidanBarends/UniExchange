/*
 RegisterRequest.java

 Registration payload. Carries a RAW password, which no entity models - User only
 ever holds a passwordHash - so this cannot be replaced by the entity itself.

 The email must be a CPUT student address; @StudentEmail reports through the
 fields map that GlobalExceptionHandler produces, so the signup form can show
 the error against the email input.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.dto.auth;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import za.ac.cput.validation.StudentEmail;

public record RegisterRequest(
        @NotBlank @Email @StudentEmail String email,
        @NotBlank String firstName,
        String middleName,
        @NotBlank String lastName,
        String cellPhone,
        @NotBlank @Size(min = 8, message = "password must be at least 8 characters") String password,
        LocalDate dateOfBirth,
        Long campusId) {
}
