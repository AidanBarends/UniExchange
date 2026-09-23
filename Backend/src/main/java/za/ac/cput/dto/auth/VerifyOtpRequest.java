/*
 VerifyOtpRequest.java

 The code the student typed in, plus the address it was sent to.

 rememberMe is the "Remember me on this device" checkbox. It decides both how
 long the returned device token lasts and how long the session does - see
 AuthController.verifyOtp.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyOtpRequest(
        @NotBlank String email,
        @NotBlank @Pattern(regexp = "\\d{4,10}", message = "code must be digits only") String code,
        boolean rememberMe) {
}
