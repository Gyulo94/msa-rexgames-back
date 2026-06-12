package com.shop.common.constants;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app")
public class Constants {

  private String appName;
  private String env;
  private String accessTokenSecretKey;
  private String fileUrl;
  private String redisHost;
  private int redisPort;
  private String tossSecretKey;
}
