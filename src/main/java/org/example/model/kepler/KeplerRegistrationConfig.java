package org.example.model.kepler;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class KeplerRegistrationConfig {
  private final String tableName;
  private final String columnFamily;
  private final String name;
}
