package com.boilerplate.springbootjava.infrastructure.external.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient.Builder restClientBuilder() {

        return RestClient.builder()
                // SimpleClientHttpRequestFactory를 통해 강제로 HTTP1.1 설정
                .requestFactory(new SimpleClientHttpRequestFactory());
    }
}