/*
 MessageRequest.java

 Inbound payload for creating/updating a Message. Entities have no public
 setters, so requests arrive as a record and are handed to MessageFactory.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.dto.communication;

public record MessageRequest(
        long conversationId,
        long senderId,
        String content) {
}
