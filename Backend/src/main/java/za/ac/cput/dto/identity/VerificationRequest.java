/*
 VerificationRequest.java

 Inbound payload for creating/updating a Verification. Entities have no public
 setters, so requests arrive as a record and are handed to VerificationFactory.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.dto.identity;

import java.time.LocalDateTime;

import za.ac.cput.domain.enums.VerificationType;

public record VerificationRequest(
        long userId,
        VerificationType verificationType,
        String token,
        LocalDateTime expiresAt) {
}
