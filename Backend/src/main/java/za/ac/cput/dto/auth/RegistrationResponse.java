/*
 RegistrationResponse.java

 What /api/auth/register hands back. Deliberately carries NO token: an account
 is unusable until the emailed code proves the student owns the mailbox.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.dto.auth;

public record RegistrationResponse(
        String email,
        String message,
        long codeExpiresInSeconds) {
}
