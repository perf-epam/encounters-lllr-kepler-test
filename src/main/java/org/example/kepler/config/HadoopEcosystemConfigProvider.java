package org.example.kepler.config;

import static java.util.Objects.requireNonNull;


import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.example.exceptions.OkHttpRequestFailedException;
import org.example.model.config.HadoopEcosystemConfig;
import org.example.model.hadoopecosystem.KafkaClusterDto;
import org.example.model.hadoopecosystem.Propertied;
import org.example.model.hadoopecosystem.PropertyDto;

/**
 * Hadoop Ecosystem {@link} NotificationManagerConfigProvider which is used hadoop-ecosystem API
 * to fetch configuration
 */
@Slf4j
@RequiredArgsConstructor
public class HadoopEcosystemConfigProvider
    implements NotificationManagerConfigProvider {

  private final OkHttpClient client;
  private final ObjectMapper objectMapper;
  private final HadoopEcosystemConfig config;

  public Properties getProperties() {
    Properties properties = new Properties();
    getKafkaProperties().forEach(
        propertyDto -> properties.setProperty(propertyDto.getName(), propertyDto.getValue())
    );

    log.info("Successfully fetched {} properties form Hadoop Ecosystem service", properties.size());
    return properties;
  }

  private List<PropertyDto> getKafkaProperties() {
    log.info("Getting Kafka properties from hadoop ecosystem service");
    Request request = new Request.Builder()
        .url(config.getKafkaResourceUrl())
        .build();

    try (Response response = client.newCall(request).execute()) {
      if (response.code() != 200) {
        String message =
            "Failed to fetch properties from Hadoop Ecosystem API: " + response.toString();
        throw new OkHttpRequestFailedException(message);
      }

      Propertied responseEntity = objectMapper
          .readValue(requireNonNull(response.body()).byteStream(), KafkaClusterDto.class);

      return responseEntity.getProperties();
    } catch (IOException e) {
      throw new OkHttpRequestFailedException(
          "Failed to fetch properties from Hadoop Ecosystem API", e);
    }
  }
}
