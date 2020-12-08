package org.example.model.config;

import com.cerner.healtheintent.hiokhttp.oauth1a.OAuthConfiguration;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
public class HadoopEcosystemConfig {
  private final OAuthConfiguration oauthConfig;
  private final String kafkaResourceUrl;
}
