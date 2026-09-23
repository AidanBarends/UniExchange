/*
 NotificationServiceImpl.java

 Business logic for Notification. Implements the generic CRUD contract
 IService<Notification, Long> plus the Notification-specific operations.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.service.communication;

import java.util.List;

import org.springframework.stereotype.Service;

import za.ac.cput.domain.communication.Notification;
import za.ac.cput.repository.communication.NotificationRepository;

@Service
public class NotificationServiceImpl implements INotificationService {

    private final NotificationRepository repository;

    public NotificationServiceImpl(NotificationRepository repository) {
        this.repository = repository;
    }

    @Override
    public Notification create(Notification notification) {
        return this.repository.save(notification);
    }

    @Override
    public Notification read(Long id) {
        return id == null ? null : this.repository.findById(id).orElse(null);
    }

    @Override
    public Notification update(Notification notification) {
        return this.repository.save(notification);
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
    public List<Notification> getAll() {
        return this.repository.findAll();
    }

    @Override
    public List<Notification> findByUserId(long userId) {
        return this.repository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public List<Notification> findUnreadForUser(long userId) {
        return this.repository.findByUserIdAndIsReadFalse(userId);
    }

    @Override
    public Notification markRead(Long notificationId) {
        Notification found = read(notificationId);
        if (found == null) {
            return null;
        }
        return this.repository.save(new Notification.Builder()
                .copy(found)
                .setRead(true)
                .build());
    }

}
