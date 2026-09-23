/*
 MessageController.java

 REST endpoints for Message.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.controller.communication;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import za.ac.cput.domain.communication.Message;
import za.ac.cput.dto.communication.MessageRequest;
import za.ac.cput.factory.communication.MessageFactory;
import za.ac.cput.service.communication.IMessageService;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final IMessageService service;

    public MessageController(IMessageService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Message> create(@RequestBody MessageRequest request) {
        Message created = this.service.create(MessageFactory.createMessage(
                request.conversationId(), request.senderId(), request.content()));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Message> read(@PathVariable Long id) {
        Message found = this.service.read(id);
        return found == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(found);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Message> update(@PathVariable Long id, @RequestBody MessageRequest request) {
        Message existing = this.service.read(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(this.service.update(MessageFactory.updateMessage(
                existing, request.conversationId(), request.senderId(), request.content())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return this.service.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping
    public List<Message> getAll() {
        return this.service.getAll();
    }

    @GetMapping("/conversation/{conversationId}")
    public List<Message> byConversation(@PathVariable long conversationId) {
        return this.service.findByConversationId(conversationId);
    }

}
