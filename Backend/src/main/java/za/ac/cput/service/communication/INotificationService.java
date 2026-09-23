/*
 INotificationService.java

 Service contract for Notification.

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/

package za.ac.cput.service.communication;

import java.util.List;

import za.ac.cput.domain.communication.Notification;
import za.ac.cput.service.IService;

public interface INotificationService extends IService<Notification, Long> {

    List<Notification> findByUserId(long userId);

    List<Notification> findUnreadForUser(long userId);

    Notification markRead(Long notificationId);

}
