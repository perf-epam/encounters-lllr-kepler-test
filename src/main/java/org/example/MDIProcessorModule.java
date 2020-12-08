package org.example;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.example.constants.ApplicationPropertyKeys.CLUSTER_ID;
import static org.example.constants.ApplicationPropertyKeys.HADOOP_ECOSYSTEM_URL;
import static org.example.constants.ApplicationPropertyKeys.OAUTH_KEY;
import static org.example.constants.ApplicationPropertyKeys.OAUTH_SECRET;
import static org.example.constants.ApplicationPropertyKeys.OAUTH_URL;
import static org.example.constants.ConfigDefaults.MAX_OKHTTP_CALL_RETRIES;

import com.cerner.healtheintent.hiokhttp.oauth1a.OAuth1aInterceptor;
import com.cerner.healtheintent.hiokhttp.oauth1a.OAuthConfiguration;
import com.cerner.healtheintent.hiokhttp.retries.RetriesInterceptor;
import com.cerner.healtheintent.hiokhttp.retries.RetryExecutionAttributes;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import java.util.Properties;
import okhttp3.OkHttpClient;
import org.example.model.config.HadoopEcosystemConfig;

public class MDIProcessorModule extends AbstractModule {
  private static final String PROPERTY_MISSING_MESSAGE =
      "Environment property is missing or blank: ";

  @Override
  protected void configure() {
    install(new KeplerModule());
  }

  /**
   * Provides generic application properties
   *
   * @return application properties
   */
  @Provides
  @Singleton
  @Named("applicationProperties")
  Properties provideApplicationProperties() {
    Properties properties = new Properties();
    System.getenv()
        .forEach(properties::setProperty);

    return properties;
  }

  /**
   * Provides HadoopEcosystemConfig entity which contains oauth info
   * and hadoop-ecosystem resource urls
   *
   * @param properties application properties to take configuration from
   * @return {@link HadoopEcosystemConfig}
   */
  @Provides
  @Singleton
  HadoopEcosystemConfig provideHadoopEcosystemConfig(
      @Named("applicationProperties") Properties properties) {
    String hadoopEcosystemUrl = properties.getProperty(HADOOP_ECOSYSTEM_URL);
    String clusterId = properties.getProperty(CLUSTER_ID);
    String accessTokenUrl = properties.getProperty(OAUTH_URL);
    String consumerKey = properties.getProperty(OAUTH_KEY);
    String consumerSecret = properties.getProperty(OAUTH_SECRET);

    checkPropertyIsNotBlank(HADOOP_ECOSYSTEM_URL, hadoopEcosystemUrl);
    checkPropertyIsNotBlank(CLUSTER_ID, clusterId);
    checkPropertyIsNotBlank(OAUTH_URL, accessTokenUrl);
    checkPropertyIsNotBlank(OAUTH_KEY, consumerKey);
    checkPropertyIsNotBlank(OAUTH_SECRET, consumerSecret);

    OAuthConfiguration oAuthConfig = OAuthConfiguration.builder()
        .accessTokenUrl(accessTokenUrl)
        .consumerKey(consumerKey)
        .consumerSecret(consumerSecret)
        .build();

    return HadoopEcosystemConfig.builder()
        .oauthConfig(oAuthConfig)
        .kafkaResourceUrl(hadoopEcosystemUrl + "/kafka-clusters/" + clusterId)
        .build();
  }

  /**
   * Provides configured hadoop-ecosystem okHttpClient
   *
   * @param hadoopEcosystemConfig {@link HadoopEcosystemConfig} configuration for hi-okhttp client
   * @return OkHttpClient
   */
  @Provides
  @Singleton
  @Named("hadoopEcosystemClient")
  OkHttpClient provideOkHttpClient(HadoopEcosystemConfig hadoopEcosystemConfig) {
    final RetryExecutionAttributes retryAttributes = RetryExecutionAttributes.builderWithDefaults()
        .maxRetries(MAX_OKHTTP_CALL_RETRIES)
        .build();

    return new OkHttpClient.Builder()
        .addInterceptor(new OAuth1aInterceptor(hadoopEcosystemConfig.getOauthConfig()))
        .addInterceptor(new RetriesInterceptor(retryAttributes))
        .build();
  }

  /**
   * Provides configured Jackson's ObjectMapper
   * @return ObjectMapper
   */
  @Provides
  @Singleton
  ObjectMapper provideObjectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
    return mapper;
  }

  private void checkPropertyIsNotBlank(String key, String value) {
    Preconditions.checkState(isNotBlank(value), PROPERTY_MISSING_MESSAGE + key);
  }
}
