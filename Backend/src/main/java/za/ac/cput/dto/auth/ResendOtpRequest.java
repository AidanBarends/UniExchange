/*
 ResendOtpRequest.java

 Request a fresh verification code.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record ResendOtpRequest(@NotBlank String email) {
}
