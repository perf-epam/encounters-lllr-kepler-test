package org.example;

import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.extern.slf4j.Slf4j;
import org.example.kepler.notification.NotificationScannerScheduler;

@Slf4j
public class Application {

  public static void main(String[] args) {
    log.info("Starting Care Management MDI Processor application");

    Injector injector = Guice.createInjector(new MDIProcessorModule());
    NotificationScannerScheduler scheduler =
        injector.getInstance(NotificationScannerScheduler.class);

    scheduler.schedule();

    log.info("Startup is successful");
  }
}
