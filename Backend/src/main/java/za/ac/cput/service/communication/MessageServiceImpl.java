/*
 MessageServiceImpl.java

 Business logic for Message. Implements the generic CRUD contract
 IService<Message, Long> plus the Message-specific operations.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.service.communication;

import java.util.List;

import org.springframework.stereotype.Service;

import za.ac.cput.domain.communication.Message;
import za.ac.cput.repository.communication.MessageRepository;

@Service
public class MessageServiceImpl implements IMessageService {

    private final MessageRepository repository;

    public MessageServiceImpl(MessageRepository repository) {
        this.repository = repository;
    }

    @Override
    public Message create(Message message) {
        return this.repository.save(message);
    }

    @Override
    public Message read(Long id) {
        return id == null ? null : this.repository.findById(id).orElse(null);
    }

    @Override
    public Message update(Message message) {
        return this.repository.save(message);
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
    public List<Message> getAll() {
        return this.repository.findAll();
    }

    @Override
    public List<Message> findByConversationId(long conversationId) {
        return this.repository.findByConversationIdOrderBySentAtAsc(conversationId);
    }

}
