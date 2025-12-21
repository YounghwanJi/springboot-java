package com.boilerplate.springbootjava.infrastructure.config.security;

import com.boilerplate.springbootjava.infrastructure.persistence.user.UserRole;
import com.boilerplate.springbootjava.infrastructure.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    public String createAccessToken(String userEmail, UserRole role) {
        log.info("createAccessToken - userEmail: {}, secret: {}",  userEmail, jwtProperties.getAccessToken().getSecret());
        return createToken(userEmail, role, jwtProperties.getAccessToken().getSecret(), jwtProperties.getAccessToken().getExpiration());
    }

    public String createRefreshToken(String userEmail) {
        return Jwts.builder()
                .setSubject(userEmail)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getRefreshToken().getExpiration()))
                .signWith(Keys.hmacShaKeyFor(jwtProperties.getRefreshToken().getSecret().getBytes()))
                .compact();
    }

    private String createToken(
            String userEmail,
            UserRole role,
            String secret,
            long expiration
    ) {
        return Jwts.builder()
                .setSubject(userEmail)
                .claim("role", role.name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .compact();
    }

    public Claims parseAccessToken(String token) {
        return parse(token, jwtProperties.getAccessToken().getSecret());
    }

    public Claims parseRefreshToken(String token) {
        return parse(token, jwtProperties.getRefreshToken().getSecret());
    }

    private Claims parse(String token, String secret) {
        return Jwts.parserBuilder()
                .setSigningKey(secret.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
