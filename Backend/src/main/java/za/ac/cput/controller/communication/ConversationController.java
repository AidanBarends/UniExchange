/*
 ConversationController.java

 REST endpoints for Conversation.

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import za.ac.cput.domain.communication.Conversation;
import za.ac.cput.factory.communication.ConversationFactory;
import za.ac.cput.service.communication.IConversationService;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final IConversationService service;

    public ConversationController(IConversationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Conversation> create() {
        Conversation created = this.service.create(ConversationFactory.createConversation());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Conversation> read(@PathVariable Long id) {
        Conversation found = this.service.read(id);
        return found == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(found);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Conversation> update(@PathVariable Long id) {
        Conversation existing = this.service.read(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(this.service.update(ConversationFactory.updateConversation(existing)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return this.service.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping
    public List<Conversation> getAll() {
        return this.service.getAll();
    }

}
