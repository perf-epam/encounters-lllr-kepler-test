package org.example.kepler.notification;

import com.cerner.kepler.notifications.Notification;
import com.cerner.kepler.notifications.NotificationScanner;
import com.google.inject.Inject;
import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.example.kepler.notification.consumer.NotificationConsumer;

/**
 * Represents NotificationScanner Runnable task to scan Kepler's notifications
 */
@Slf4j
public class NotificationScannerThread implements Runnable {
  private final NotificationScanner scanner;
  private final NotificationConsumer notificationConsumer;
  private final String scannerName;

  /**
   * Creates a new instance of NotificationScannerThread
   *
   * @param scanner {@link NotificationScanner} to scan notifications
   * @param notificationConsumer {@link NotificationConsumer} to consume notifications
   * @param scannerName unique name to be used to trace thread logs
   */
  @Inject
  public NotificationScannerThread(
      NotificationScanner scanner, NotificationConsumer notificationConsumer, String scannerName) {
    this.scanner = scanner;
    this.notificationConsumer = notificationConsumer;
    this.scannerName = scannerName;
  }

  @Override
  public void run() {
    try {
      NotificationScanner.Batch batch = scanner.next();
      if (batch == null) {
        log.info("Scanner {}: No notifications received. Batch is empty", scannerName);
        return;
      }

      List<Notification> notifications = batch.getNotifications();
      log.info(
          "Scanner {}: Received a new batch with {} notifications", scannerName,
          notifications.size());

      notifications.forEach(notificationConsumer::consume);
    } catch (IOException e) {
      log.error("Scanner " + scannerName + ": Error while scanning notifications", e);
    }
  }
}
