package org.example.kepler.config;

import java.util.Properties;

/**
 * NotificationManager Configuration Provider interface
 */
public interface NotificationManagerConfigProvider {
  /**
   * Fetches or creates properties which are used by Kepler's
   * NotificationManager to acquire the connection.
   *
   * @return properties to create NotificationManager
   */
  Properties getProperties();
}
