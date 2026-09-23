/*
 ConversationParticipantRequest.java

 Inbound payload for creating/updating a ConversationParticipant. Entities have no public
 setters, so requests arrive as a record and are handed to ConversationParticipantFactory.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.dto.communication;

public record ConversationParticipantRequest(
        long conversationId,
        long userId) {
}
