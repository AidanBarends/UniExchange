/*
 NotificationController.java

 REST endpoints for Notification.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.controller.communication;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import za.ac.cput.domain.communication.Notification;
import za.ac.cput.dto.communication.NotificationRequest;
import za.ac.cput.factory.communication.NotificationFactory;
import za.ac.cput.service.communication.INotificationService;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final INotificationService service;

    public NotificationController(INotificationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Notification> create(@RequestBody NotificationRequest request) {
        Notification created = this.service.create(NotificationFactory.createNotification(
                request.userId(), request.type(), request.title(), request.content(), request.entityType(),
                request.entityId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Notification> read(@PathVariable Long id) {
        Notification found = this.service.read(id);
        return found == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(found);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Notification> update(@PathVariable Long id,
                                               @RequestBody NotificationRequest request) {
        Notification existing = this.service.read(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(this.service.update(NotificationFactory.updateNotification(
                existing, request.userId(), request.type(), request.title(), request.content(),
                request.entityType(), request.entityId())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return this.service.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping
    public List<Notification> getAll() {
        return this.service.getAll();
    }

    @GetMapping("/user/{userId}")
    public List<Notification> byUser(@PathVariable long userId) {
        return this.service.findByUserId(userId);
    }

    @GetMapping("/user/{userId}/unread")
    public List<Notification> unreadByUser(@PathVariable long userId) {
        return this.service.findUnreadForUser(userId);
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Notification> markRead(@PathVariable Long id) {
        Notification updated = this.service.markRead(id);
        return updated == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(updated);
    }

}
