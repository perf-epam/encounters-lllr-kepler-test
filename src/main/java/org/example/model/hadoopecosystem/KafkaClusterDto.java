package org.example.model.hadoopecosystem;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KafkaClusterDto implements Propertied {
  @JsonProperty("kafkaProperties")
  private List<PropertyDto> properties;
}
