/*
 IChatService.java

 Service contract for the chat feature.

 Every method takes the acting user's id as its first argument and enforces
 participation itself. Nothing here trusts a conversationId from the client -
 knowing an id must never be enough to read a thread.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 24 September 2026
*/

package za.ac.cput.service.communication;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import za.ac.cput.domain.communication.ChatMedia;
import za.ac.cput.dto.communication.ChatDtos.ChatMediaUploaded;
import za.ac.cput.dto.communication.ChatDtos.ChatMessageView;
import za.ac.cput.dto.communication.ChatDtos.ChatThreadView;

public interface IChatService {

    /** Every thread this user is in, newest activity first. */
    List<ChatThreadView> threadsFor(long userId);

    /** Finds the existing conversation with this person about this listing, or starts one. */
    ChatThreadView startThread(long userId, long otherUserId, Long listingId);

    /**
     * Messages after the given cursor. Pass 0 for the whole thread.
     * Returns oldest-first so the client can append.
     */
    List<ChatMessageView> messagesIn(long userId, long conversationId, long afterMessageId);

    ChatMessageView sendMessage(long userId, long conversationId, String content, Long mediaId);

    ChatMediaUploaded uploadMedia(long userId, long conversationId, MultipartFile file, Integer durationMs);

    /** Marks the thread read up to now for this user. */
    void markRead(long userId, long conversationId);

    long unreadCountFor(long userId);

    /**
     * Loads an attachment for streaming, having confirmed the viewer is in its
     * conversation. Returns null when the media does not exist OR the viewer is
     * not a participant - the caller turns both into a 404 so the endpoint cannot
     * be used to discover which conversations exist.
     */
    ChatMedia mediaForViewer(long mediaId, long viewerId);

}
