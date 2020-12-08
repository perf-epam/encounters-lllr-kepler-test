package org.example.model.config;

import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(staticName = "of")
public class ScheduleConfig {
  private final long initialDelay;
  private final long period;
  private final TimeUnit timeUnit;
}
