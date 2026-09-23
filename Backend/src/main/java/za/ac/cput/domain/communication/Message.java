/*
 Message.java

 Message POJO class

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.domain.communication;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "message")
public class Message {
    //  Variables/Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long messageId;

    @Column(nullable = false, name = "conversation_id")
    private long conversationId;

    @Column(nullable = false, name = "sender_id")
    private long senderId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, name = "sent_at")
    private LocalDateTime sentAt;

    //  Constructors
    protected Message() {
        // Required by JPA
    }

    private Message(Builder builder) {
        this.messageId = builder.messageId;
        this.conversationId = builder.conversationId;
        this.senderId = builder.senderId;
        this.content = builder.content;
        this.sentAt = builder.sentAt;
    }

    //  Getters
    public long getMessageId() {
        return messageId;
    }

    public long getConversationId() {
        return conversationId;
    }

    public long getSenderId() {
        return senderId;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    //  toString
    @Override
    public String toString() {
        return "Message{" +
                "messageId=" + messageId +
                ", conversationId=" + conversationId +
                ", senderId=" + senderId +
                ", content='" + content + '\'' +
                ", sentAt=" + sentAt +
                '}';
    }

    //  Builder Class
    public static class Builder {

        //  Variables/Attributes
        private long messageId;
        private long conversationId;
        private long senderId;
        private String content;
        private LocalDateTime sentAt;

        //  Setters
        public Builder setMessageId(long messageId) {
            this.messageId = messageId;
            return this;
        }

        public Builder setConversationId(long conversationId) {
            this.conversationId = conversationId;
            return this;
        }

        public Builder setSenderId(long senderId) {
            this.senderId = senderId;
            return this;
        }

        public Builder setContent(String content) {
            this.content = content;
            return this;
        }

        public Builder setSentAt(LocalDateTime sentAt) {
            this.sentAt = sentAt;
            return this;
        }

        public Builder copy(Message message) {
            this.messageId = message.messageId;
            this.conversationId = message.conversationId;
            this.senderId = message.senderId;
            this.content = message.content;
            this.sentAt = message.sentAt;
            return this;
        }

        //  build method
        public Message build() {
            return new Message(this);
        }
    }
}