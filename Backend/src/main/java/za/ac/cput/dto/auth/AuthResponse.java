/*
 AuthResponse.java

 What /api/auth/verify-otp and a trusted /api/auth/login hand back to the client.

 deviceToken is populated ONLY by /verify-otp, which is the one place a device
 can earn its trust. Every other response leaves it null, because the browser
 already holds the token and re-sending it would put a live credential into more
 responses than necessary.

 A record, so Jackson serialises these by component name - the "is" prefix
 stripping that affects entity getters does not apply here.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.dto.auth;

import java.util.List;

public record AuthResponse(
        String token,
        String tokenType,
        long expiresIn,
        long userId,
        String email,
        List<String> roles,
        String deviceToken) {
}
