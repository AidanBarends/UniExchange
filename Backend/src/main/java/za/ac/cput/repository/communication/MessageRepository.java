/*
 MessageRepository.java

 Spring Data JPA repository for the Message entity.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.repository.communication;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import za.ac.cput.domain.communication.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByConversationIdOrderBySentAtAsc(long conversationId);

    List<Message> findBySenderId(long senderId);

    /*
     The polling query. Ordered by id rather than sentAt because two messages sent
     in the same millisecond would otherwise have no stable order, and the client
     advances its cursor by the last id it saw.

     Covered by idx_message_conversation_id - see the Message entity.
    */
    List<Message> findByConversationIdAndMessageIdGreaterThanOrderByMessageIdAsc(
            long conversationId, long afterMessageId);

    /** Newest message in a thread, for the conversation-list preview. */
    Optional<Message> findTopByConversationIdOrderByMessageIdDesc(long conversationId);

    /**
     * Unread count for one thread: everything after the reader's last-read time that
     * they did not send themselves. lastReadAt is null for a participant who has
     * never opened the thread, so callers pass a far-past date in that case.
     */
    long countByConversationIdAndSenderIdNotAndSentAtAfter(
            long conversationId, long readerId, LocalDateTime lastReadAt);

    /*
     Previews for every thread at once - avoids a query per conversation.

     The subquery alias is "latest", not "inner": INNER is a reserved word in HQL
     (as in INNER JOIN) and using it fails at startup with a parser error that
     points at the following token rather than the alias.
    */
    @Query("select m from Message m where m.messageId in (" +
           "  select max(latest.messageId) from Message latest " +
           "  where latest.conversationId in :conversationIds group by latest.conversationId)")
    List<Message> findLatestPerConversation(@Param("conversationIds") List<Long> conversationIds);

}
