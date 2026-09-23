/*
 ConversationFactory.java

 Factory for Conversation. All construction goes through here so that every
 Conversation is validated with Helper before it exists - the entity itself
 exposes only a Builder and a protected JPA constructor.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.factory.communication;

import java.time.LocalDateTime;

import za.ac.cput.domain.communication.Conversation;
import za.ac.cput.util.Helper;

public class ConversationFactory {

    // Prevent instantiation - factory class
    private ConversationFactory() {}

    public static Conversation createConversation() {
        LocalDateTime now = LocalDateTime.now();

        return new Conversation.Builder()
                .setCreatedAt(now)
                .build();
    }

    public static Conversation updateConversation(Conversation existing) {
        if (!Helper.isValidObject(existing)) {
            throw new IllegalArgumentException("Conversation: existing record is required for an update");
        }

        return new Conversation.Builder()
                .copy(existing)
                .build();
    }

}
