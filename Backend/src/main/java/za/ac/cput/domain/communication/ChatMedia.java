/*
 ChatMedia.java

 ChatMedia POJO class - one uploaded chat attachment (photo, video or voice note).

 Two design points worth knowing before changing this:

 1. The link to Message points THIS way (chat_media.message_id), not the other
    way (message.media_id). That gives several attachments per message for free,
    makes "never attached to anything" a single indexed query the cleanup job can
    run, and - most importantly - means Message.Builder.copy() never has to learn
    about attachments. copy() there is field-by-field, so a forgotten line would
    silently drop the attachment on every edit with no compiler error.

 2. conversationId is stored here rather than being reached through the message.
    Upload happens BEFORE the message is sent, so at that moment there is no
    message to authorise against - but there is always a conversation.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 24 September 2026
*/

package za.ac.cput.domain.communication;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

import za.ac.cput.domain.enums.ChatMediaType;

@Entity
@Table(name = "chat_media", indexes = {
        @Index(name = "idx_chat_media_conversation", columnList = "conversation_id"),
        @Index(name = "idx_chat_media_message", columnList = "message_id")
})
public class ChatMedia {
    //  Variables/Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long chatMediaId;

    @Column(nullable = false, name = "conversation_id")
    private long conversationId;

    @Column(nullable = false, name = "uploader_id")
    private long uploaderId;

    /** Null until the message referencing this upload is actually sent. */
    @Column(name = "message_id")
    private Long messageId;

    /** Server-generated UUID filename. Never anything the uploader supplied. */
    @Column(nullable = false, unique = true, length = 64, name = "storage_key")
    private String storageKey;

    /*
     The type the server CONFIRMED by inspecting the file's magic bytes - not the
     Content-Type the client claimed. This value is echoed straight back as the
     Content-Type response header when the file is streamed, so trusting the
     client here would be a stored-XSS vector.
    */
    @Column(nullable = false, length = 100, name = "mime_type")
    private String mimeType;

    // EnumType.STRING, explicitly: the JPA default is ORDINAL, which silently
    // reinterprets every existing row if a constant is ever inserted in the middle.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16, name = "media_type")
    private ChatMediaType mediaType;

    @Column(nullable = false, name = "size_bytes")
    private long sizeBytes;

    /*
     Voice notes and video only. Measured by the browser while recording, because
     MediaRecorder's WebM output carries no Duration element - the resulting blob
     reports Infinity in an <audio> tag. This is untrusted client input and is
     clamped by the factory.
    */
    @Column(name = "duration_ms")
    private Integer durationMs;

    @Column(length = 120, name = "original_filename")
    private String originalFilename;

    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;

    //  Constructors
    protected ChatMedia() {
        // Required by JPA
    }

    private ChatMedia(Builder builder) {
        this.chatMediaId = builder.chatMediaId;
        this.conversationId = builder.conversationId;
        this.uploaderId = builder.uploaderId;
        this.messageId = builder.messageId;
        this.storageKey = builder.storageKey;
        this.mimeType = builder.mimeType;
        this.mediaType = builder.mediaType;
        this.sizeBytes = builder.sizeBytes;
        this.durationMs = builder.durationMs;
        this.originalFilename = builder.originalFilename;
        this.createdAt = builder.createdAt;
    }

    //  Getters
    public long getChatMediaId() {
        return chatMediaId;
    }

    public long getConversationId() {
        return conversationId;
    }

    public long getUploaderId() {
        return uploaderId;
    }

    public Long getMessageId() {
        return messageId;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public String getMimeType() {
        return mimeType;
    }

    public ChatMediaType getMediaType() {
        return mediaType;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public Integer getDurationMs() {
        return durationMs;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    //  toString
    @Override
    public String toString() {
        return "ChatMedia{" +
                "chatMediaId=" + chatMediaId +
                ", conversationId=" + conversationId +
                ", uploaderId=" + uploaderId +
                ", messageId=" + messageId +
                ", storageKey='" + storageKey + '\'' +
                ", mimeType='" + mimeType + '\'' +
                ", mediaType=" + mediaType +
                ", sizeBytes=" + sizeBytes +
                ", durationMs=" + durationMs +
                ", originalFilename='" + originalFilename + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }

    //  Builder Class
    public static class Builder {

        //  Variables/Attributes
        private long chatMediaId;
        private long conversationId;
        private long uploaderId;
        private Long messageId;
        private String storageKey;
        private String mimeType;
        private ChatMediaType mediaType;
        private long sizeBytes;
        private Integer durationMs;
        private String originalFilename;
        private LocalDateTime createdAt;

        //  Setters
        public Builder setChatMediaId(long chatMediaId) {
            this.chatMediaId = chatMediaId;
            return this;
        }

        public Builder setConversationId(long conversationId) {
            this.conversationId = conversationId;
            return this;
        }

        public Builder setUploaderId(long uploaderId) {
            this.uploaderId = uploaderId;
            return this;
        }

        public Builder setMessageId(Long messageId) {
            this.messageId = messageId;
            return this;
        }

        public Builder setStorageKey(String storageKey) {
            this.storageKey = storageKey;
            return this;
        }

        public Builder setMimeType(String mimeType) {
            this.mimeType = mimeType;
            return this;
        }

        public Builder setMediaType(ChatMediaType mediaType) {
            this.mediaType = mediaType;
            return this;
        }

        public Builder setSizeBytes(long sizeBytes) {
            this.sizeBytes = sizeBytes;
            return this;
        }

        public Builder setDurationMs(Integer durationMs) {
            this.durationMs = durationMs;
            return this;
        }

        public Builder setOriginalFilename(String originalFilename) {
            this.originalFilename = originalFilename;
            return this;
        }

        public Builder setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder copy(ChatMedia chatMedia) {
            this.chatMediaId = chatMedia.chatMediaId;
            this.conversationId = chatMedia.conversationId;
            this.uploaderId = chatMedia.uploaderId;
            this.messageId = chatMedia.messageId;
            this.storageKey = chatMedia.storageKey;
            this.mimeType = chatMedia.mimeType;
            this.mediaType = chatMedia.mediaType;
            this.sizeBytes = chatMedia.sizeBytes;
            this.durationMs = chatMedia.durationMs;
            this.originalFilename = chatMedia.originalFilename;
            this.createdAt = chatMedia.createdAt;
            return this;
        }

        //  build method
        public ChatMedia build() {
            return new ChatMedia(this);
        }
    }
}
