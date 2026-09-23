/*
 ConversationRepository.java

 Spring Data JPA repository for the Conversation entity.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.repository.communication;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import za.ac.cput.domain.communication.Conversation;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
}
