/*
 LoginRequest.java

 Login payload: email plus raw password, and what the browser knows about
 itself.

 deviceToken is the opaque value handed out by a previous /verify-otp on this
 browser. Present and still valid means the OTP can be skipped; absent, unknown
 or expired means a code is sent. It is optional by design - a first sign-in has
 nothing to send - and it never stands in for the password.

 rememberMe carries the checkbox through, so a trusted sign-in gets the same
 long-lived token a freshly verified one would.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank String email,
        @NotBlank String password,
        String deviceToken,
        boolean rememberMe) {
}
