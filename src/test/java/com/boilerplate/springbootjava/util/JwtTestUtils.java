package com.boilerplate.springbootjava.util;

import com.boilerplate.springbootjava.infrastructure.config.security.JwtTokenProvider;
import com.boilerplate.springbootjava.infrastructure.persistence.user.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * JWT 토큰 생성 테스트 유틸리티
 * - 테스트에서 JWT 토큰을 쉽게 생성할 수 있도록 지원
 */
@Component
public class JwtTestUtils {

    @Autowired
    private JwtTokenProvider tokenProvider;

    /**
     * Access Token 생성
     */
    public String createAccessToken(String email, UserRole role) {
        return tokenProvider.createAccessToken(email, role);
    }

    /**
     * Refresh Token 생성
     */
    public String createRefreshToken(String email) {
        return tokenProvider.createRefreshToken(email);
    }

    /**
     * Bearer 형식의 Access Token 생성
     * - "Bearer {token}" 형태로 반환
     */
    public String createBearerToken(String email, UserRole role) {
        return "Bearer " + createAccessToken(email, role);
    }

    /**
     * 기본 USER 역할의 Bearer Token 생성
     */
    public String createUserBearerToken() {
        return createBearerToken("user@test.com", UserRole.USER);
    }

    /**
     * ADMIN 역할의 Bearer Token 생성
     */
    public String createAdminBearerToken() {
        return createBearerToken("admin@test.com", UserRole.ADMIN);
    }

    /**
     * MANAGER 역할의 Bearer Token 생성
     */
    public String createManagerBearerToken() {
        return createBearerToken("manager@test.com", UserRole.MANAGER);
    }
}
