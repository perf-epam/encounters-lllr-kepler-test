package org.example.kepler.notification.consumer;

import com.cerner.kepler.notifications.Notification;

/**
 * NotificationConsumer represents the logic of Kepler's Notification consuming
 */
public interface NotificationConsumer {
  void consume(Notification notification);
}
