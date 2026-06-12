package com.shop.common.config;

import javax.net.ssl.SSLException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import com.shop.common.constants.Constants;

import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.RequiredArgsConstructor;
import reactor.netty.http.client.HttpClient;

@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

  private final Constants constants;

  @Bean
  public WebClient webClient(WebClient.Builder builder) throws SSLException {
    io.netty.handler.ssl.SslContext sslContext = SslContextBuilder.forClient()
        .trustManager(InsecureTrustManagerFactory.INSTANCE)
        .build();

    HttpClient httpClient = HttpClient.create()
        .secure(sslContextSpec -> sslContextSpec
            .sslContext(sslContext));

    return builder
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .baseUrl(constants.getFileUrl())
        .build();
  }
}
