/*
 ConversationParticipant.java

 ConversationParticipant POJO class

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
@Table(name = "conversation_participant")
public class ConversationParticipant {
    //  Variables/Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long participantId;

    @Column(nullable = false, name = "conversation_id")
    private long conversationId;

    @Column(nullable = false, name = "user_id")
    private long userId;

    @Column(nullable = false, name = "joined_at")
    private LocalDateTime joinedAt;

    @Column(name = "last_read_at")
    private LocalDateTime lastReadAt;

    //  Constructors
    protected ConversationParticipant() {
        // Required by JPA
    }

    private ConversationParticipant(Builder builder) {
        this.participantId = builder.participantId;
        this.conversationId = builder.conversationId;
        this.userId = builder.userId;
        this.joinedAt = builder.joinedAt;
        this.lastReadAt = builder.lastReadAt;
    }

    //  Getters
    public long getParticipantId() {
        return participantId;
    }

    public long getConversationId() {
        return conversationId;
    }

    public long getUserId() {
        return userId;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public LocalDateTime getLastReadAt() {
        return lastReadAt;
    }

    //  toString
    @Override
    public String toString() {
        return "ConversationParticipant{" +
                "participantId=" + participantId +
                ", conversationId=" + conversationId +
                ", userId=" + userId +
                ", joinedAt=" + joinedAt +
                ", lastReadAt=" + lastReadAt +
                '}';
    }

    //  Builder Class
    public static class Builder {

        //  Variables/Attributes
        private long participantId;
        private long conversationId;
        private long userId;
        private LocalDateTime joinedAt;
        private LocalDateTime lastReadAt;

        //  Setters
        public Builder setParticipantId(long participantId) {
            this.participantId = participantId;
            return this;
        }

        public Builder setConversationId(long conversationId) {
            this.conversationId = conversationId;
            return this;
        }

        public Builder setUserId(long userId) {
            this.userId = userId;
            return this;
        }

        public Builder setJoinedAt(LocalDateTime joinedAt) {
            this.joinedAt = joinedAt;
            return this;
        }

        public Builder setLastReadAt(LocalDateTime lastReadAt) {
            this.lastReadAt = lastReadAt;
            return this;
        }

        public Builder copy(ConversationParticipant conversationParticipant) {
            this.participantId = conversationParticipant.participantId;
            this.conversationId = conversationParticipant.conversationId;
            this.userId = conversationParticipant.userId;
            this.joinedAt = conversationParticipant.joinedAt;
            this.lastReadAt = conversationParticipant.lastReadAt;
            return this;
        }

        //  build method
        public ConversationParticipant build() {
            return new ConversationParticipant(this);
        }
    }
}