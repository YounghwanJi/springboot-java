package com.boilerplate.springbootjava.infrastructure.external.config;

import com.boilerplate.springbootjava.infrastructure.interceptor.RestClientLoggingInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
public class RestClientConfig {

    private final RestClientLoggingInterceptor loggingInterceptor;

    @Bean
    public RestClient.Builder restClientBuilder() {

        return RestClient.builder()
                .requestInterceptor(loggingInterceptor)
                // SimpleClientHttpRequestFactory를 통해 강제로 HTTP1.1 설정
                .requestFactory(new SimpleClientHttpRequestFactory());
    }
}