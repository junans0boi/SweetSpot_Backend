package com.hollywood.sweetspot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

        @Bean
        public WebClient naverMapsWebClient(
                        @Value("${naver.maps.base-url}") String baseUrl,
                        @Value("${naver.maps.key-id}") String keyId,
                        @Value("${naver.maps.key}") String key) {
                return WebClient.builder()
                                .baseUrl(baseUrl)
                                .defaultHeader("x-ncp-apigw-api-key-id", keyId)
                                .defaultHeader("x-ncp-apigw-api-key", key)
                                .defaultHeader(HttpHeaders.ACCEPT, "application/json")
                                .exchangeStrategies(ExchangeStrategies.builder()
                                                .codecs(c -> c.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                                                .build())
                                .build();
        }
}