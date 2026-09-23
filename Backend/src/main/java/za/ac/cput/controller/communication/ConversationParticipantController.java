/*
 ConversationParticipantController.java

 REST endpoints for ConversationParticipant.

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

import za.ac.cput.domain.communication.ConversationParticipant;
import za.ac.cput.dto.communication.ConversationParticipantRequest;
import za.ac.cput.factory.communication.ConversationParticipantFactory;
import za.ac.cput.service.communication.IConversationParticipantService;

@RestController
@RequestMapping("/api/conversation-participants")
public class ConversationParticipantController {

    private final IConversationParticipantService service;

    public ConversationParticipantController(IConversationParticipantService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ConversationParticipant> create(@RequestBody ConversationParticipantRequest request) {
        ConversationParticipant created = this.service.create(ConversationParticipantFactory.createConversationParticipant(
                request.conversationId(), request.userId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConversationParticipant> read(@PathVariable Long id) {
        ConversationParticipant found = this.service.read(id);
        return found == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(found);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConversationParticipant> update(@PathVariable Long id,
                                                          @RequestBody ConversationParticipantRequest request) {
        ConversationParticipant existing = this.service.read(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(this.service.update(ConversationParticipantFactory.updateConversationParticipant(
                existing, request.conversationId(), request.userId())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return this.service.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping
    public List<ConversationParticipant> getAll() {
        return this.service.getAll();
    }

    @GetMapping("/conversation/{conversationId}")
    public List<ConversationParticipant> byConversation(@PathVariable long conversationId) {
        return this.service.findByConversationId(conversationId);
    }

    @GetMapping("/user/{userId}")
    public List<ConversationParticipant> byUser(@PathVariable long userId) {
        return this.service.findByUserId(userId);
    }

}
