/*
 Conversation.java

 Conversation POJO class

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
@Table(name = "conversation")
public class Conversation {
    //  Variables/Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long conversationId;

    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;

    //  Constructors
    protected Conversation() {
        // Required by JPA
    }

    private Conversation(Builder builder) {
        this.conversationId = builder.conversationId;
        this.createdAt = builder.createdAt;
    }

    //  Getters
    public long getConversationId() {
        return conversationId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    //  toString
    @Override
    public String toString() {
        return "Conversation{" +
                "conversationId=" + conversationId +
                ", createdAt=" + createdAt +
                '}';
    }

    //  Builder Class
    public static class Builder {

        //  Variables/Attributes
        private long conversationId;
        private LocalDateTime createdAt;

        //  Setters
        public Builder setConversationId(long conversationId) {
            this.conversationId = conversationId;
            return this;
        }

        public Builder setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder copy(Conversation conversation) {
            this.conversationId = conversation.conversationId;
            this.createdAt = conversation.createdAt;
            return this;
        }

        //  build method
        public Conversation build() {
            return new Conversation(this);
        }
    }
}