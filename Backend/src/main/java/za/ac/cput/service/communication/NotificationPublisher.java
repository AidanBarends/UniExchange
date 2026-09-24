/*
 NotificationPublisher.java

 The one place that creates Notification rows.

 Until now nothing in the backend ever did: the entity, repository, service and
 controller all existed, and the frontend had a whole notifications page, but the
 only way a row could appear was a manual POST. So the bell never lit up for
 anything that actually happened.

 Every method here is best-effort and swallows its own failures. A notification
 is a courtesy - it must never be the reason a message fails to send or a sale
 fails to complete.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 24 September 2026
*/

package za.ac.cput.service.communication;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import za.ac.cput.domain.enums.NotificationType;
import za.ac.cput.factory.communication.NotificationFactory;
import za.ac.cput.repository.communication.NotificationRepository;

@Service
public class NotificationPublisher {

    private static final Logger log = LoggerFactory.getLogger(NotificationPublisher.class);

    private final NotificationRepository repository;

    public NotificationPublisher(NotificationRepository repository) {
        this.repository = repository;
    }

    /**
     * entityId is the CONVERSATION, not the message: the frontend's
     * notificationRoute maps a MESSAGE notification to /messages/{entityId}, and
     * that route takes a conversation id.
     */
    public void messageReceived(long recipientId, String senderName, String preview, long conversationId) {
        publish(recipientId, NotificationType.MESSAGE,
                "New message from " + senderName, preview, "CONVERSATION", conversationId);
    }

    public void purchaseMade(long sellerId, String listingTitle, BigDecimal amount, long transactionId) {
        publish(sellerId, NotificationType.TRANSACTION,
                "Someone bought " + listingTitle,
                "R" + amount + " is being held until the buyer confirms they received it.",
                "TRANSACTION", transactionId);
    }

    public void fundsReleased(long sellerId, BigDecimal amount, long transactionId) {
        publish(sellerId, NotificationType.TRANSACTION,
                "You have been paid",
                "R" + amount + " has been added to your wallet.",
                "TRANSACTION", transactionId);
    }

    public void purchaseCancelled(long recipientId, BigDecimal amount, long transactionId) {
        publish(recipientId, NotificationType.TRANSACTION,
                "A purchase was cancelled",
                "R" + amount + " has been refunded.",
                "TRANSACTION", transactionId);
    }

    public void reviewReceived(long revieweeId, int rating, long transactionId) {
        publish(revieweeId, NotificationType.TRANSACTION,
                "You received a " + rating + "-star review",
                "Someone you traded with has rated the deal.",
                "TRANSACTION", transactionId);
    }

    public void badgeEarned(long userId) {
        publish(userId, NotificationType.SYSTEM,
                "You are now a Trusted Seller",
                "Five different buyers have rated your sales highly. Your badge now shows on your listings.",
                "USER", userId);
    }

    private void publish(long userId, NotificationType type, String title, String content,
                         String entityType, Long entityId) {
        try {
            this.repository.save(NotificationFactory.createNotification(
                    userId, type, title, content, entityType, entityId));
        } catch (RuntimeException e) {
            // Deliberately swallowed. Losing a notification is a far better
            // outcome than rolling back the message or sale that caused it.
            log.warn("Could not publish {} notification for user {}: {}", type, userId, e.getMessage());
        }
    }

}
