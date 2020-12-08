package org.example;

import static java.util.Optional.ofNullable;
import static org.example.constants.ApplicationPropertyKeys.BOOTSTRAP_SERVERS_OPTIONAL;
import static org.example.constants.ApplicationPropertyKeys.COLUMN_FAMILY;
import static org.example.constants.ApplicationPropertyKeys.TABLE_NAME;
import static org.example.constants.ApplicationPropertyKeys.ZOOKEEPER_CONNECT_OPTIONAL;

import com.cerner.kepler.notifications.NotificationManager;
import com.cerner.kepler.notifications.NotificationScanner;
import com.cerner.kepler.notifications.kafka.KafkaNotificationManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import org.example.kepler.config.HadoopEcosystemConfigProvider;
import org.example.kepler.config.NotificationManagerConfigProvider;
import org.example.kepler.notification.NotificationScannerThread;
import org.example.kepler.notification.consumer.LoggingNotificationConsumer;
import org.example.kepler.notification.consumer.NotificationConsumer;
import org.example.kepler.registration.RegistrationBuilder;
import org.example.model.config.HadoopEcosystemConfig;
import org.example.model.config.ScheduleConfig;
import org.example.model.kepler.KeplerRegistrationConfig;

public class KeplerModule extends AbstractModule {
  @Override
  protected void configure() {
  }

  /**
   * Provides NotificationManagerConfigProvider which is used to fetch
   * configuration for NotificationManager creation
   *
   * @param okHttpClient okHttpClient to make HTTP requests
   * @param objectMapper objectMapper object mapper
   * @param config {@link HadoopEcosystemConfig} hadoop ecosystem API config
   * @return {@link NotificationManagerConfigProvider}
   */
  @Provides
  @Singleton
  NotificationManagerConfigProvider provideNotificationManagerConfigProvider(
      @Named("hadoopEcosystemClient") OkHttpClient okHttpClient,
      ObjectMapper objectMapper, HadoopEcosystemConfig config) {
    return new HadoopEcosystemConfigProvider(okHttpClient, objectMapper, config);
  }

  /**
   * Provides properties for NotificationManager creation
   *
   * @param configProvider {@link NotificationManagerConfigProvider}
   * @param appProperties generic application properties
   * @return NotificationManager properties
   */
  @Provides
  @Singleton
  @Named("notificationManagerProperties")
  Properties provideNotificationManagerProperties(
      NotificationManagerConfigProvider configProvider,
      @Named("applicationProperties") Properties appProperties) {
    Properties properties = configProvider.getProperties();

    ofNullable(appProperties.getProperty(ZOOKEEPER_CONNECT_OPTIONAL))
        .ifPresent(value -> properties.setProperty(ZOOKEEPER_CONNECT_OPTIONAL, value));
    ofNullable(appProperties.getProperty(BOOTSTRAP_SERVERS_OPTIONAL))
        .ifPresent(value -> properties.setProperty(BOOTSTRAP_SERVERS_OPTIONAL, value));

    return properties;
  }

  /**
   * Provides Kepler's NotificationManager instance
   *
   * @param properties properties to create NotificationManager
   * @return NotificationManager
   * @throws IOException if an error occurs with the underlying system
   */
  @Provides
  @Singleton
  NotificationManager notificationManager(
      @Named("notificationManagerProperties") Properties properties) throws
      IOException {
    return KafkaNotificationManager.getInstance(properties);
  }

  /**
   * Provides a list of {@link KeplerRegistrationConfig} used to create registrations
   *
   * @return list of {@link KeplerRegistrationConfig}
   */
  @Provides
  @Singleton
  List<KeplerRegistrationConfig> provideKeplerRegistrationConfigs(@Named("applicationProperties") Properties appProperties) {
    KeplerRegistrationConfig config = KeplerRegistrationConfig.builder()
        .tableName(appProperties.getProperty(TABLE_NAME))
        .columnFamily(appProperties.getProperty(COLUMN_FAMILY))
        .name("Listener_" + UUID.randomUUID().toString())
        .build();

    return ImmutableList.of(config);
  }

  /**
   * Provides {@link NotificationConsumer} used to consume Kepler's notifications
   *
   * @return {@link NotificationConsumer}
   */
  @Provides
  @Singleton
  NotificationConsumer notificationConsumer() {
    return new LoggingNotificationConsumer();
  }

  /**
   * Provides ScheduledExecutorService to run notification scanning Runnable tasks
   *
   * @return ScheduledExecutorService
   */
  @Provides
  @Singleton
  public ScheduledExecutorService provideScheduledExecutorService() {
    return Executors.newSingleThreadScheduledExecutor();
  }

  /**
   * Provides List of Runnable notification scanning tasks
   *
   * @param registrationConfigs {@link KeplerRegistrationConfig} configs to build Kepler's registrations
   * @param registrationBuilder {@link RegistrationBuilder} utility builder to build Kepler's registrations
   * @param notificationConsumer {@link NotificationConsumer} notification consumers to be used by scanner
   *                                                         tasks to consume received notifications
   * @return List of Runnable {@link NotificationScannerThread}
   */
  @Provides
  @Singleton
  public List<Runnable> provideNotificationScannerTasks(
      List<KeplerRegistrationConfig> registrationConfigs,
      RegistrationBuilder registrationBuilder,
      NotificationConsumer notificationConsumer) {

    List<Runnable> tasks = new LinkedList<>();
    for (KeplerRegistrationConfig config : registrationConfigs) {
      NotificationScanner scanner = registrationBuilder.buildRegistration(config);
      NotificationScannerThread task = new NotificationScannerThread(scanner, notificationConsumer,
          config.getName());

      tasks.add(task);
    }

    return tasks;
  }

  /**
   * Provides {@link ScheduleConfig} to be used to configure ScheduledExecutorService tasks schedule
   *
   * @return {@link ScheduleConfig}
   */
  @Provides
  @Singleton
  public ScheduleConfig provideScheduleConfig() {
    return ScheduleConfig.of(2, 2, TimeUnit.SECONDS);
  }
}
