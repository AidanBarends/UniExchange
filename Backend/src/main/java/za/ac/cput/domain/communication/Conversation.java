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
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversation", indexes = {
        @Index(name = "idx_conversation_listing", columnList = "listing_id")
})
public class Conversation {
    //  Variables/Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long conversationId;

    /*
     The listing this chat is about, when it started from a "Message seller"
     button. Null for a general conversation - and null for every conversation
     that existed before this column was added, so callers must handle it.

     Long, never primitive long: a primitive would generate a NOT NULL column,
     and ddl-auto=update cannot add one of those to a table that already has rows.
    */
    @Column(name = "listing_id")
    private Long listingId;

    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;

    //  Constructors
    protected Conversation() {
        // Required by JPA
    }

    private Conversation(Builder builder) {
        this.conversationId = builder.conversationId;
        this.listingId = builder.listingId;
        this.createdAt = builder.createdAt;
    }

    //  Getters
    public long getConversationId() {
        return conversationId;
    }

    public Long getListingId() {
        return listingId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    //  toString
    @Override
    public String toString() {
        return "Conversation{" +
                "conversationId=" + conversationId +
                ", listingId=" + listingId +
                ", createdAt=" + createdAt +
                '}';
    }

    //  Builder Class
    public static class Builder {

        //  Variables/Attributes
        private long conversationId;
        private Long listingId;
        private LocalDateTime createdAt;

        //  Setters
        public Builder setConversationId(long conversationId) {
            this.conversationId = conversationId;
            return this;
        }

        public Builder setListingId(Long listingId) {
            this.listingId = listingId;
            return this;
        }

        public Builder setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder copy(Conversation conversation) {
            this.conversationId = conversation.conversationId;
            this.listingId = conversation.listingId;
            this.createdAt = conversation.createdAt;
            return this;
        }

        //  build method
        public Conversation build() {
            return new Conversation(this);
        }
    }
}