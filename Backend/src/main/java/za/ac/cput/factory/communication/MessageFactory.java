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

    /**
     * A message that carries an attachment, where the text is an optional caption.
     *
     * Separate from createMessage rather than loosening it, because the invariant
     * genuinely differs: an ordinary message must have text, while this one must
     * have an attachment and may have none. Loosening createMessage would let a
     * completely empty message through both paths.
     *
     * content is normalised to "" rather than null on purpose. message.content is
     * NOT NULL, and ddl-auto=update will not relax an existing NOT NULL column -
     * so making it nullable would need a hand-run ALTER that every teammate's
     * database and the deployed one would each need separately, with the ones
     * that missed it failing only at runtime.
     *
     * @param hasAttachment whether a ChatMedia row will be linked to this message
     */
    public static Message createMediaMessage(long conversationId, long senderId,
                                             String caption, boolean hasAttachment) {
        if (!Helper.isValidId(conversationId)) {
            throw new IllegalArgumentException("Message: conversationId must be a positive id");
        }

        if (!Helper.isValidId(senderId)) {
            throw new IllegalArgumentException("Message: senderId must be a positive id");
        }

        if (!hasAttachment && Helper.isNullOrEmpty(caption)) {
            throw new IllegalArgumentException("Message: a message needs either text or an attachment");
        }

        return new Message.Builder()
                .setConversationId(conversationId)
                .setSenderId(senderId)
                .setContent(caption == null ? "" : caption.trim())
                .setSentAt(LocalDateTime.now())
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
