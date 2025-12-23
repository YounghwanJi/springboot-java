package com.boilerplate.springbootjava.infrastructure.filter.security;

import com.boilerplate.springbootjava.application.user.port.out.UserRepository;
import com.boilerplate.springbootjava.infrastructure.config.security.JwtTokenProvider;
import com.boilerplate.springbootjava.infrastructure.persistence.user.UserEntity;
import com.boilerplate.springbootjava.infrastructure.persistence.user.UserRole;
import com.boilerplate.springbootjava.infrastructure.persistence.user.UserStatus;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * JwtAuthenticationFilter 단위 테스트
 * - Mock 객체 사용
 * - JWT 토큰 파싱 및 SecurityContext 설정 검증
 * - 예외 상황 처리 검증
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenResolver tokenResolver;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        // 각 테스트 전 SecurityContext 초기화
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        // 각 테스트 후 SecurityContext 초기화
        SecurityContextHolder.clearContext();
    }

    // ========== 헬퍼 메서드 ==========

    private UserEntity createUserEntity(String email, UserRole role) {
        return UserEntity.builder()
                .id(1L)
                .email(email)
                .password("$2a$10$encodedPassword")
                .role(role)
                .name("Test User")
                .phoneNumber("010-1234-5678")
                .status(UserStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private Claims createClaims(String email, String role) {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn(email);
        when(claims.get("role", String.class)).thenReturn(role);
        return claims;
    }

    // ========== 필터 동작 테스트 ==========

    @Test
    @DisplayName("유효한 토큰 - SecurityContext 설정 성공")
    void doFilterInternal_ValidToken_SetSecurityContext() throws ServletException, IOException {
        // given
        String token = "valid.jwt.token";
        String email = "test@test.com";
        String role = "USER";

        UserEntity userEntity = createUserEntity(email, UserRole.USER);
        Claims claims = createClaims(email, role);

        when(tokenResolver.resolve(request)).thenReturn(Optional.of(token));
        when(tokenProvider.parseAccessToken(token)).thenReturn(claims);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userEntity));

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo(email);
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_" + role);

        verify(tokenResolver).resolve(request);
        verify(tokenProvider).parseAccessToken(token);
        verify(userRepository).findByEmail(email);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("토큰 없음 - SecurityContext 설정하지 않음")
    void doFilterInternal_NoToken_DoesNotSetSecurityContext() throws ServletException, IOException {
        // given
        when(tokenResolver.resolve(request)).thenReturn(Optional.empty());

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();

        verify(tokenResolver).resolve(request);
        verify(tokenProvider, never()).parseAccessToken(anyString());
        verify(userRepository, never()).findByEmail(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("잘못된 토큰 - SecurityContext 클리어")
    void doFilterInternal_InvalidToken_ClearSecurityContext() throws ServletException, IOException {
        // given
        String invalidToken = "invalid.jwt.token";

        when(tokenResolver.resolve(request)).thenReturn(Optional.of(invalidToken));
        when(tokenProvider.parseAccessToken(invalidToken))
                .thenThrow(new RuntimeException("Invalid JWT token"));

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();

        verify(tokenResolver).resolve(request);
        verify(tokenProvider).parseAccessToken(invalidToken);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("토큰 파싱 성공하지만 사용자 없음 - SecurityContext 클리어")
    void doFilterInternal_UserNotFound_ClearSecurityContext() throws ServletException, IOException {
        // given
        String token = "valid.jwt.token";
        String email = "nonexistent@test.com";
        String role = "USER";

        Claims claims = createClaims(email, role);

        when(tokenResolver.resolve(request)).thenReturn(Optional.of(token));
        when(tokenProvider.parseAccessToken(token)).thenReturn(claims);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();

        verify(tokenResolver).resolve(request);
        verify(tokenProvider).parseAccessToken(token);
        verify(userRepository).findByEmail(email);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("ADMIN 권한 토큰 - SecurityContext에 ROLE_ADMIN 설정")
    void doFilterInternal_AdminRole_SetCorrectAuthority() throws ServletException, IOException {
        // given
        String token = "admin.jwt.token";
        String email = "admin@test.com";
        String role = "ADMIN";

        UserEntity userEntity = createUserEntity(email, UserRole.ADMIN);
        Claims claims = createClaims(email, role);

        when(tokenResolver.resolve(request)).thenReturn(Optional.of(token));
        when(tokenProvider.parseAccessToken(token)).thenReturn(claims);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userEntity));

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("MANAGER 권한 토큰 - SecurityContext에 ROLE_MANAGER 설정")
    void doFilterInternal_ManagerRole_SetCorrectAuthority() throws ServletException, IOException {
        // given
        String token = "manager.jwt.token";
        String email = "manager@test.com";
        String role = "MANAGER";

        UserEntity userEntity = createUserEntity(email, UserRole.MANAGER);
        Claims claims = createClaims(email, role);

        when(tokenResolver.resolve(request)).thenReturn(Optional.of(token));
        when(tokenProvider.parseAccessToken(token)).thenReturn(claims);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userEntity));

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_MANAGER");

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("필터 체인 항상 호출 - 토큰 유무와 관계없이")
    void doFilterInternal_AlwaysCallsFilterChain() throws ServletException, IOException {
        // given - 토큰 없는 경우
        when(tokenResolver.resolve(request)).thenReturn(Optional.empty());

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);

        // given - 토큰 있는 경우
        reset(filterChain, tokenResolver, tokenProvider, userRepository);

        String token = "valid.jwt.token";
        String email = "test@test.com";
        UserEntity userEntity = createUserEntity(email, UserRole.USER);
        Claims claims = createClaims(email, "USER");

        when(tokenResolver.resolve(request)).thenReturn(Optional.of(token));
        when(tokenProvider.parseAccessToken(token)).thenReturn(claims);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userEntity));

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
    }
}
