package org.example.kepler.notification;

import com.google.inject.Inject;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import lombok.extern.slf4j.Slf4j;
import org.example.model.config.ScheduleConfig;

/**
 * NotificationScannerScheduler is used to schedule provided Runnable tasks using provided config
 */
@Slf4j
public class NotificationScannerScheduler {
  private final ScheduledExecutorService scheduledExecutorService;
  private final List<Runnable> tasks;
  private final ScheduleConfig config;

  /**
   * Creates a new instance of NotificationScannerScheduler
   *
   * @param scheduledExecutorService {@link ScheduledExecutorService} to schedule executions
   * @param notificationScannerTasks list of runnable tasks to schedule
   * @param config {@link ScheduleConfig}
   */
  @Inject
  public NotificationScannerScheduler(
      ScheduledExecutorService scheduledExecutorService,
      List<Runnable> notificationScannerTasks,
      ScheduleConfig config) {
    this.scheduledExecutorService = scheduledExecutorService;
    this.tasks = notificationScannerTasks;
    this.config = config;
  }

  /**
   * Schedules Runnable tasks executions
   */
  public void schedule() {
    log.info("Scheduling {} repeatable tasks. Initial delay: {}, period: {}, time unit: {}",
        tasks.size(), config.getInitialDelay(), config.getPeriod(), config.getTimeUnit());

    tasks.forEach(task -> scheduledExecutorService.scheduleAtFixedRate(
        task, config.getInitialDelay(), config.getPeriod(), config.getTimeUnit()));
  }
}
