/*
 MessageFactory.java

 Factory for Message. All construction goes through here so that every
 Message is validated with Helper before it exists - the entity itself
 exposes only a Builder and a protected JPA constructor.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.factory.communication;

import java.time.LocalDateTime;

import za.ac.cput.domain.communication.Message;
import za.ac.cput.util.Helper;

public class MessageFactory {

    // Prevent instantiation - factory class
    private MessageFactory() {}

    public static Message createMessage(long conversationId, long senderId, String content) {
        if (!Helper.isValidId(conversationId)) {
            throw new IllegalArgumentException("Message: conversationId must be a positive id");
        }

        if (!Helper.isValidId(senderId)) {
            throw new IllegalArgumentException("Message: senderId must be a positive id");
        }

        if (Helper.isNullOrEmpty(content)) {
            throw new IllegalArgumentException("Message: content is required");
        }

        LocalDateTime now = LocalDateTime.now();

        return new Message.Builder()
                .setConversationId(conversationId)
                .setSenderId(senderId)
                .setContent(content)
                .setSentAt(now)
                .build();
    }

    public static Message updateMessage(Message existing, long conversationId, long senderId, String content) {
        if (!Helper.isValidObject(existing)) {
            throw new IllegalArgumentException("Message: existing record is required for an update");
        }

        if (!Helper.isValidId(conversationId)) {
            throw new IllegalArgumentException("Message: conversationId must be a positive id");
        }

        if (!Helper.isValidId(senderId)) {
            throw new IllegalArgumentException("Message: senderId must be a positive id");
        }

        if (Helper.isNullOrEmpty(content)) {
            throw new IllegalArgumentException("Message: content is required");
        }

        return new Message.Builder()
                .copy(existing)
                .setConversationId(conversationId)
                .setSenderId(senderId)
                .setContent(content)
                .build();
    }

}
