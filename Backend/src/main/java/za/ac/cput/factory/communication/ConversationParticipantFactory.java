/*
 ConversationParticipantFactory.java

 Factory for ConversationParticipant. All construction goes through here so that every
 ConversationParticipant is validated with Helper before it exists - the entity itself
 exposes only a Builder and a protected JPA constructor.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.factory.communication;

import java.time.LocalDateTime;

import za.ac.cput.domain.communication.ConversationParticipant;
import za.ac.cput.util.Helper;

public class ConversationParticipantFactory {

    // Prevent instantiation - factory class
    private ConversationParticipantFactory() {}

    public static ConversationParticipant createConversationParticipant(long conversationId, long userId) {
        if (!Helper.isValidId(conversationId)) {
            throw new IllegalArgumentException("ConversationParticipant: conversationId must be a positive id");
        }

        if (!Helper.isValidId(userId)) {
            throw new IllegalArgumentException("ConversationParticipant: userId must be a positive id");
        }

        LocalDateTime now = LocalDateTime.now();

        return new ConversationParticipant.Builder()
                .setConversationId(conversationId)
                .setUserId(userId)
                .setJoinedAt(now)
                .build();
    }

    public static ConversationParticipant updateConversationParticipant(
            ConversationParticipant existing, long conversationId, long userId) {
        if (!Helper.isValidObject(existing)) {
            throw new IllegalArgumentException("ConversationParticipant: existing record is required for an update");
        }

        if (!Helper.isValidId(conversationId)) {
            throw new IllegalArgumentException("ConversationParticipant: conversationId must be a positive id");
        }

        if (!Helper.isValidId(userId)) {
            throw new IllegalArgumentException("ConversationParticipant: userId must be a positive id");
        }

        return new ConversationParticipant.Builder()
                .copy(existing)
                .setConversationId(conversationId)
                .setUserId(userId)
                .build();
    }

}
