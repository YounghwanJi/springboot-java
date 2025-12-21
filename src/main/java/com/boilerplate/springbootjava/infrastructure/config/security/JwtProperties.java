package com.boilerplate.springbootjava.infrastructure.config.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.jwt")
@Getter
@Setter
public class JwtProperties {

    private Token accessToken;
    private Token refreshToken;

    @Getter
    @Setter
    public static class Token {
        private String secret;
        private long expiration;
    }
}