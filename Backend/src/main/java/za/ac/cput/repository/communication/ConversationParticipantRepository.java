/*
 ConversationParticipantRepository.java

 Spring Data JPA repository for the ConversationParticipant entity.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.repository.communication;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import za.ac.cput.domain.communication.ConversationParticipant;

@Repository
public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, Long> {

    List<ConversationParticipant> findByConversationId(long conversationId);

    List<ConversationParticipant> findByUserId(long userId);

    /** The authorization check behind every /api/chat endpoint. */
    Optional<ConversationParticipant> findByConversationIdAndUserId(long conversationId, long userId);

    boolean existsByConversationIdAndUserId(long conversationId, long userId);

    /** The other people in a set of threads, resolved in one query for the thread list. */
    List<ConversationParticipant> findByConversationIdInAndUserIdNot(
            List<Long> conversationIds, long userId);

    /*
     Find the existing one-to-one conversation between two students about a given
     listing, if there is one.

     MIN(conversationId) rather than "the" id because this cannot be enforced by a
     unique constraint - participation lives in this table, not on conversation -
     so two simultaneous "Message seller" taps can create two rows. Always
     resolving to the lowest id means both students land in the same thread and
     the duplicate is merely orphaned rather than splitting the conversation.

     The listingId comparison is null-safe so that general chats (no listing) match
     each other rather than never matching.
    */
    @Query("select min(a.conversationId) from ConversationParticipant a, ConversationParticipant b, Conversation c " +
           "where a.conversationId = b.conversationId and a.conversationId = c.conversationId " +
           "and a.userId = :userA and b.userId = :userB " +
           "and ((:listingId is null and c.listingId is null) or c.listingId = :listingId)")
    Optional<Long> findSharedConversationId(@Param("userA") long userA,
                                            @Param("userB") long userB,
                                            @Param("listingId") Long listingId);

}
