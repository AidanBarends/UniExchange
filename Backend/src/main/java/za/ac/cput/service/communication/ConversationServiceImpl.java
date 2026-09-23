/*
 ConversationServiceImpl.java

 Business logic for Conversation. Implements the generic CRUD contract
 IService<Conversation, Long> plus the Conversation-specific operations.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.service.communication;

import java.util.List;

import org.springframework.stereotype.Service;

import za.ac.cput.domain.communication.Conversation;
import za.ac.cput.repository.communication.ConversationRepository;

@Service
public class ConversationServiceImpl implements IConversationService {

    private final ConversationRepository repository;

    public ConversationServiceImpl(ConversationRepository repository) {
        this.repository = repository;
    }

    @Override
    public Conversation create(Conversation conversation) {
        return this.repository.save(conversation);
    }

    @Override
    public Conversation read(Long id) {
        return id == null ? null : this.repository.findById(id).orElse(null);
    }

    @Override
    public Conversation update(Conversation conversation) {
        return this.repository.save(conversation);
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
    public List<Conversation> getAll() {
        return this.repository.findAll();
    }

}
