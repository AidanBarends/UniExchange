/*
 ChatMediaFactory.java

 Factory for ChatMedia. All construction goes through here so that every
 ChatMedia is validated with Helper before it exists - the entity itself
 exposes only a Builder and a protected JPA constructor.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 24 September 2026
*/

package za.ac.cput.factory.communication;

import java.time.LocalDateTime;

import za.ac.cput.domain.communication.ChatMedia;
import za.ac.cput.domain.enums.ChatMediaType;
import za.ac.cput.util.Helper;

public class ChatMediaFactory {

    /*
     Voice notes and clips are capped at 10 minutes. The value arrives from the
     browser (MediaRecorder cannot be trusted to put a duration in the container,
     so we time the recording in JS instead), which makes it untrusted input:
     without a clamp a crafted upload renders a multi-hour scrub bar in the other
     student's chat.
    */
    private static final int MAX_DURATION_MS = 10 * 60 * 1000;

    /** Matches the column length; anything longer is truncated rather than rejected. */
    private static final int MAX_FILENAME = 120;

    // Prevent instantiation - factory class
    private ChatMediaFactory() {}

    public static ChatMedia createChatMedia(long conversationId, long uploaderId, String storageKey,
                                            String mimeType, ChatMediaType mediaType, long sizeBytes,
                                            Integer durationMs, String originalFilename) {
        if (!Helper.isValidId(conversationId)) {
            throw new IllegalArgumentException("ChatMedia: conversationId must be a positive id");
        }

        if (!Helper.isValidId(uploaderId)) {
            throw new IllegalArgumentException("ChatMedia: uploaderId must be a positive id");
        }

        if (Helper.isNullOrEmpty(storageKey)) {
            throw new IllegalArgumentException("ChatMedia: storageKey is required");
        }

        if (Helper.isNullOrEmpty(mimeType)) {
            throw new IllegalArgumentException("ChatMedia: mimeType is required");
        }

        if (!Helper.isValidObject(mediaType)) {
            throw new IllegalArgumentException("ChatMedia: mediaType is required");
        }

        if (sizeBytes <= 0) {
            throw new IllegalArgumentException("ChatMedia: sizeBytes must be greater than zero");
        }

        return new ChatMedia.Builder()
                .setConversationId(conversationId)
                .setUploaderId(uploaderId)
                .setStorageKey(storageKey)
                .setMimeType(mimeType)
                .setMediaType(mediaType)
                .setSizeBytes(sizeBytes)
                .setDurationMs(clampDuration(durationMs))
                .setOriginalFilename(sanitiseFilename(originalFilename))
                .setCreatedAt(LocalDateTime.now())
                .build();
    }

    /** Links an upload to the message that was finally sent with it. */
    public static ChatMedia attachToMessage(ChatMedia existing, long messageId) {
        if (!Helper.isValidObject(existing)) {
            throw new IllegalArgumentException("ChatMedia: existing record is required for an update");
        }

        if (!Helper.isValidId(messageId)) {
            throw new IllegalArgumentException("ChatMedia: messageId must be a positive id");
        }

        return new ChatMedia.Builder()
                .copy(existing)
                .setMessageId(messageId)
                .build();
    }

    private static Integer clampDuration(Integer durationMs) {
        if (durationMs == null || durationMs <= 0) {
            return null;
        }
        return Math.min(durationMs, MAX_DURATION_MS);
    }

    /*
     Strips anything that is not a plain filename character. This value ends up in
     a Content-Disposition header, where a CR or LF would let an uploader inject
     arbitrary response headers. ContentDisposition already encodes it properly,
     so this is defence in depth rather than the only guard.
    */
    private static String sanitiseFilename(String originalFilename) {
        if (Helper.isNullOrEmpty(originalFilename)) {
            return null;
        }
        String cleaned = originalFilename.trim().replaceAll("[^A-Za-z0-9._ -]", "_");
        return cleaned.length() > MAX_FILENAME ? cleaned.substring(0, MAX_FILENAME) : cleaned;
    }

}
