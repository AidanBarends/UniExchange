/*
 ChatDtos.java

 Request and response shapes for /api/chat.

 These are the one place in the backend that deliberately breaks the
 "controllers return raw entities" habit. Chat needs joined data the domain
 cannot express - the other participant's name, the last message in a thread, an
 unread count, a signed media URL - and there are no JPA relationships to walk.
 Returning entities here would force the frontend into a request per thread and
 a request per attachment.

 Grouped in one file because they are a single cohesive API contract; splitting
 six small records across six files would obscure rather than clarify.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 24 September 2026
*/

package za.ac.cput.dto.communication;

import java.time.LocalDateTime;
import java.util.List;

import za.ac.cput.domain.enums.ChatMediaType;

public final class ChatDtos {

    private ChatDtos() {}

    /** Body of POST /api/chat/threads - starts or reuses a conversation. */
    public record StartThreadRequest(long otherUserId, Long listingId) {}

    /** Body of POST /api/chat/threads/{id}/messages. Either field may be absent, but not both. */
    public record SendMessageRequest(String content, Long mediaId) {}

    /** The other person in a thread. Never carries an email - that is not the counterparty's business. */
    public record ChatParticipant(long userId, String firstName, String lastName) {}

    /**
     * An attachment as the client sees it. url is pre-signed and viewer-bound, so
     * it can be dropped straight into an img/audio/video src with no auth header.
     */
    public record ChatMediaView(
            long mediaId,
            ChatMediaType mediaType,
            String mimeType,
            String url,
            Integer durationMs,
            long sizeBytes,
            String originalFilename) {}

    /** One message bubble. media is null for a plain text message. */
    public record ChatMessageView(
            long messageId,
            long conversationId,
            long senderId,
            String content,
            LocalDateTime sentAt,
            ChatMediaView media) {}

    /** One row in the conversation list. */
    public record ChatThreadView(
            long conversationId,
            Long listingId,
            String listingTitle,
            ChatParticipant otherParticipant,
            String lastMessagePreview,
            LocalDateTime lastMessageAt,
            long unreadCount) {}

    /** Response of the message poll. */
    public record ChatMessagePage(List<ChatMessageView> messages) {}

    /** Response of POST .../media - the client sends this id with the message. */
    public record ChatMediaUploaded(long mediaId, ChatMediaType mediaType, Integer durationMs) {}

    /** Drives the unread dot on the Messages nav item. */
    public record UnreadCount(long count) {}

}
