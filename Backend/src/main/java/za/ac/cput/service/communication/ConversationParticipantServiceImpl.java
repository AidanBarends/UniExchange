/*
 ConversationParticipantServiceImpl.java

 Business logic for ConversationParticipant. Implements the generic CRUD contract
 IService<ConversationParticipant, Long> plus the ConversationParticipant-specific operations.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.service.communication;

import java.util.List;

import org.springframework.stereotype.Service;

import za.ac.cput.domain.communication.ConversationParticipant;
import za.ac.cput.repository.communication.ConversationParticipantRepository;

@Service
public class ConversationParticipantServiceImpl implements IConversationParticipantService {

    private final ConversationParticipantRepository repository;

    public ConversationParticipantServiceImpl(ConversationParticipantRepository repository) {
        this.repository = repository;
    }

    @Override
    public ConversationParticipant create(ConversationParticipant conversationParticipant) {
        return this.repository.save(conversationParticipant);
    }

    @Override
    public ConversationParticipant read(Long id) {
        return id == null ? null : this.repository.findById(id).orElse(null);
    }

    @Override
    public ConversationParticipant update(ConversationParticipant conversationParticipant) {
        return this.repository.save(conversationParticipant);
    }

    @Override
    public boolean delete(Long id) {
        if (id == null || !this.repository.existsById(id)) {
            return false;
        }
        this.repository.deleteById(id);
        return true;
    }

    @Override
    public List<ConversationParticipant> getAll() {
        return this.repository.findAll();
    }

    @Override
    public List<ConversationParticipant> findByConversationId(long conversationId) {
        return this.repository.findByConversationId(conversationId);
    }

    @Override
    public List<ConversationParticipant> findByUserId(long userId) {
        return this.repository.findByUserId(userId);
    }

}
