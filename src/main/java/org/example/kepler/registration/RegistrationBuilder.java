package org.example.kepler.registration;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

import com.cerner.kepler.notifications.NotificationManager;
import com.cerner.kepler.notifications.NotificationScanner;
import com.cerner.kepler.notifications.Registration;
import com.google.inject.Inject;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.example.exceptions.ListenerRegistrationFailedException;
import org.example.model.kepler.KeplerRegistrationConfig;

/**
 * This class is used to build registrations using provided NotificationManager and
 * {@link KeplerRegistrationConfig} config.
 */
@Slf4j
public class RegistrationBuilder {
  private final NotificationManager notificationManager;

  /**
   * Creates an instance of RegistrationBuilder
   *
   * @param notificationManager Kepler's NotificationManager
   */
  @Inject
  public RegistrationBuilder(NotificationManager notificationManager) {
    this.notificationManager = notificationManager;
  }

  /**
   * Builds registration using parameters provided in {@link KeplerRegistrationConfig}
   *
   * @param config registration config
   * @return NotificationScanner for created registration
   */
  public NotificationScanner buildRegistration(KeplerRegistrationConfig config) {
    String tableName = checkNotNull(config.getTableName(), "Table name cannot be null");
    String columnFamily = checkNotNull(config.getColumnFamily(), "Column Family cannot be null");
    String registrationName = checkNotNull(config.getName(), "Listener name cannot be null");
    log.info("Building a registration '{}' on table '{}' and column family '{}'", registrationName,
        tableName, columnFamily);

    try {
      notificationManager.buildRegistration(tableName, registrationName)
          .setColumnFamily(columnFamily)
          .register();

      log.info(
          "Registration '{}' on table '{}' was built successfully", registrationName, tableName);

      Registration registration = notificationManager
          .getRegistrations(tableName)
          .get(config.getName());

      return notificationManager.getScanner(tableName, registration);
    } catch (IOException e) {
      String message =
          format("Failed to build registration '%s' on table '%s'", registrationName, tableName);
      throw new ListenerRegistrationFailedException(message, e);
    }
  }
}
