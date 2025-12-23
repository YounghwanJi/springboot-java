package com.boilerplate.springbootjava.infrastructure.config.security;

import com.boilerplate.springbootjava.infrastructure.persistence.user.UserRole;
import com.boilerplate.springbootjava.infrastructure.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * JwtTokenProvider 단위 테스트
 * - JWT 토큰 생성/파싱 기능 검증
 * - 만료 토큰, 잘못된 서명 처리 검증
 */
class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;

    private static final String ACCESS_SECRET = "test-access-token-secret-key-for-testing-32bytes-minimum";
    private static final String REFRESH_SECRET = "test-refresh-token-secret-key-for-testing-32bytes-min";
    private static final long ACCESS_EXPIRATION = 900000L;  // 15분
    private static final long REFRESH_EXPIRATION = 1209600000L;  // 14일

    @BeforeEach
    void setUp() {
        // JwtProperties 설정
        JwtProperties jwtProperties = new JwtProperties();

        JwtProperties.Token accessToken = new JwtProperties.Token();
        accessToken.setSecret(ACCESS_SECRET);
        accessToken.setExpiration(ACCESS_EXPIRATION);
        jwtProperties.setAccessToken(accessToken);

        JwtProperties.Token refreshToken = new JwtProperties.Token();
        refreshToken.setSecret(REFRESH_SECRET);
        refreshToken.setExpiration(REFRESH_EXPIRATION);
        jwtProperties.setRefreshToken(refreshToken);

        tokenProvider = new JwtTokenProvider(jwtProperties);
    }

    @Test
    @DisplayName("Access Token 생성 - 정상 동작")
    void createAccessToken_Success() {
        // given
        String userEmail = "test@test.com";
        UserRole role = UserRole.USER;

        // when
        String token = tokenProvider.createAccessToken(userEmail, role);

        // then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3);  // JWT는 3개 부분으로 구성 (header.payload.signature)
    }

    @Test
    @DisplayName("Access Token 파싱 - 이메일 추출")
    void parseAccessToken_ExtractsEmail() {
        // given
        String userEmail = "test@test.com";
        String token = tokenProvider.createAccessToken(userEmail, UserRole.USER);

        // when
        Claims claims = tokenProvider.parseAccessToken(token);

        // then
        assertThat(claims.getSubject()).isEqualTo(userEmail);
    }

    @Test
    @DisplayName("Access Token 파싱 - Role 추출")
    void parseAccessToken_ExtractsRole() {
        // given
        String userEmail = "test@test.com";
        UserRole role = UserRole.ADMIN;
        String token = tokenProvider.createAccessToken(userEmail, role);

        // when
        Claims claims = tokenProvider.parseAccessToken(token);

        // then
        assertThat(claims.get("role", String.class)).isEqualTo(role.name());
    }

    @Test
    @DisplayName("Refresh Token 생성 - 정상 동작")
    void createRefreshToken_Success() {
        // given
        String userEmail = "test@test.com";

        // when
        String token = tokenProvider.createRefreshToken(userEmail);

        // then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("Refresh Token 파싱 - 이메일 추출")
    void parseRefreshToken_ExtractsEmail() {
        // given
        String userEmail = "test@test.com";
        String token = tokenProvider.createRefreshToken(userEmail);

        // when
        Claims claims = tokenProvider.parseRefreshToken(token);

        // then
        assertThat(claims.getSubject()).isEqualTo(userEmail);
    }

    @Test
    @DisplayName("Refresh Token에는 Role이 없음")
    void parseRefreshToken_NoRoleClaim() {
        // given
        String userEmail = "test@test.com";
        String token = tokenProvider.createRefreshToken(userEmail);

        // when
        Claims claims = tokenProvider.parseRefreshToken(token);

        // then
        assertThat(claims.get("role")).isNull();
    }

    @Test
    @DisplayName("잘못된 서명의 Access Token - 예외 발생")
    void parseAccessToken_InvalidSignature_ThrowsException() {
        // given
        String userEmail = "test@test.com";
        String token = tokenProvider.createAccessToken(userEmail, UserRole.USER);
        String tamperedToken = token.substring(0, token.length() - 5) + "XXXXX";  // 서명 변조

        // when & then
        assertThatThrownBy(() -> tokenProvider.parseAccessToken(tamperedToken))
                .isInstanceOf(SignatureException.class);
    }

    @Test
    @DisplayName("형식이 잘못된 토큰 - 예외 발생")
    void parseAccessToken_MalformedToken_ThrowsException() {
        // given
        String malformedToken = "invalid.token.format";

        // when & then
        assertThatThrownBy(() -> tokenProvider.parseAccessToken(malformedToken))
                .isInstanceOf(MalformedJwtException.class);
    }

    @Test
    @DisplayName("만료된 Access Token - 예외 발생 (수동 생성)")
    void parseAccessToken_ExpiredToken_ThrowsException() {
        // given - 만료 시간이 매우 짧은 토큰 생성
        JwtProperties shortExpirationProperties = new JwtProperties();

        JwtProperties.Token accessToken = new JwtProperties.Token();
        accessToken.setSecret(ACCESS_SECRET);
        accessToken.setExpiration(1L);  // 1ms
        shortExpirationProperties.setAccessToken(accessToken);

        JwtProperties.Token refreshToken = new JwtProperties.Token();
        refreshToken.setSecret(REFRESH_SECRET);
        refreshToken.setExpiration(REFRESH_EXPIRATION);
        shortExpirationProperties.setRefreshToken(refreshToken);

        JwtTokenProvider shortExpirationProvider = new JwtTokenProvider(shortExpirationProperties);

        String token = shortExpirationProvider.createAccessToken("test@test.com", UserRole.USER);

        // when & then - 약간의 대기 후 파싱 시도
        try {
            Thread.sleep(10);  // 토큰이 만료될 때까지 대기
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertThatThrownBy(() -> tokenProvider.parseAccessToken(token))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    @DisplayName("다양한 역할의 Access Token 생성 및 파싱")
    void createAccessToken_VariousRoles() {
        // given
        String userEmail = "test@test.com";
        UserRole[] roles = {UserRole.USER, UserRole.MANAGER, UserRole.ADMIN};

        // when & then
        for (UserRole role : roles) {
            String token = tokenProvider.createAccessToken(userEmail, role);
            Claims claims = tokenProvider.parseAccessToken(token);

            assertThat(claims.getSubject()).isEqualTo(userEmail);
            assertThat(claims.get("role", String.class)).isEqualTo(role.name());
        }
    }

    @Test
    @DisplayName("Access Token과 Refresh Token은 서로 다른 시크릿 키 사용")
    void accessAndRefreshTokens_UseDifferentSecrets() {
        // given
        String userEmail = "test@test.com";
        String accessToken = tokenProvider.createAccessToken(userEmail, UserRole.USER);
        String refreshToken = tokenProvider.createRefreshToken(userEmail);

        // when & then - 서로 다른 시크릿 키로 서명되었으므로 교차 파싱 불가
        assertThatThrownBy(() -> tokenProvider.parseRefreshToken(accessToken))
                .isInstanceOf(SignatureException.class);

        assertThatThrownBy(() -> tokenProvider.parseAccessToken(refreshToken))
                .isInstanceOf(SignatureException.class);
    }
}
