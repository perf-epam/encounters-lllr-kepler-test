package org.example.kepler.notification.consumer;

import com.cerner.kepler.notifications.Notification;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoggingNotificationConsumer implements NotificationConsumer {
  @Override
  public void consume(Notification notification) {
    log.info(
        "Listener name: {}, table: {}, key: {}, column: {}, timestamp: {}, version: {}",
        notification.getListenerName(),
        notification.getTable(),
        notification.getKey(),
        notification.getColumn(),
        notification.getTimestamp(),
        notification.getVersion()
    );
  }
}
