package com.boilerplate.springbootjava.infrastructure.config.security;

import com.boilerplate.springbootjava.application.user.port.out.UserRepository;
import com.boilerplate.springbootjava.common.exception.CustomException;
import com.boilerplate.springbootjava.common.exception.errorcode.UserErrorCode;
import com.boilerplate.springbootjava.infrastructure.persistence.user.UserEntity;
import com.boilerplate.springbootjava.infrastructure.persistence.user.UserRole;
import com.boilerplate.springbootjava.infrastructure.persistence.user.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * CustomUserDetailsService 단위 테스트
 * - UserRepository Mock 사용
 * - loadUserByUsername 동작 검증
 * - CustomUserDetails 매핑 검증
 */
@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    // ========== 헬퍼 메서드 ==========

    private UserEntity createUserEntity(String email, UserRole role, UserStatus status) {
        return UserEntity.builder()
                .id(1L)
                .email(email)
                .password("$2a$10$encodedPassword")
                .role(role)
                .name("Test User")
                .phoneNumber("010-1234-5678")
                .status(status)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    // ========== loadUserByUsername 테스트 ==========

    @Test
    @DisplayName("사용자 로드 - 정상 동작")
    void loadUserByUsername_Success() {
        // given
        String email = "test@test.com";
        UserEntity userEntity = createUserEntity(email, UserRole.USER, UserStatus.ACTIVE);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userEntity));

        // when
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        // then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails).isInstanceOf(CustomUserDetails.class);
        assertThat(userDetails.getUsername()).isEqualTo(email);
        assertThat(userDetails.getPassword()).isEqualTo("$2a$10$encodedPassword");
        assertThat(userDetails.isEnabled()).isTrue();

        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("사용자 로드 - 존재하지 않는 사용자 예외")
    void loadUserByUsername_NotFound_ThrowsException() {
        // given
        String email = "nonexistent@test.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(email))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(UserErrorCode.USER_NOT_FOUND);

        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("사용자 로드 - USER 권한 확인")
    void loadUserByUsername_UserRole_ReturnsCorrectAuthorities() {
        // given
        String email = "user@test.com";
        UserEntity userEntity = createUserEntity(email, UserRole.USER, UserStatus.ACTIVE);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userEntity));

        // when
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        // then
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertThat(authorities).hasSize(1);
        assertThat(authorities)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");

        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("사용자 로드 - MANAGER 권한 확인")
    void loadUserByUsername_ManagerRole_ReturnsCorrectAuthorities() {
        // given
        String email = "manager@test.com";
        UserEntity userEntity = createUserEntity(email, UserRole.MANAGER, UserStatus.ACTIVE);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userEntity));

        // when
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        // then
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertThat(authorities).hasSize(1);
        assertThat(authorities)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_MANAGER");

        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("사용자 로드 - ADMIN 권한 확인")
    void loadUserByUsername_AdminRole_ReturnsCorrectAuthorities() {
        // given
        String email = "admin@test.com";
        UserEntity userEntity = createUserEntity(email, UserRole.ADMIN, UserStatus.ACTIVE);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userEntity));

        // when
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        // then
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertThat(authorities).hasSize(1);
        assertThat(authorities)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN");

        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("사용자 로드 - ACTIVE 상태 확인")
    void loadUserByUsername_ActiveStatus_IsEnabled() {
        // given
        String email = "active@test.com";
        UserEntity userEntity = createUserEntity(email, UserRole.USER, UserStatus.ACTIVE);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userEntity));

        // when
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        // then
        assertThat(userDetails.isEnabled()).isTrue();

        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("사용자 로드 - INACTIVE 상태 확인")
    void loadUserByUsername_InactiveStatus_IsNotEnabled() {
        // given
        String email = "inactive@test.com";
        UserEntity userEntity = createUserEntity(email, UserRole.USER, UserStatus.INACTIVE);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userEntity));

        // when
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        // then
        assertThat(userDetails.isEnabled()).isFalse();

        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("사용자 로드 - CustomUserDetails 타입 검증")
    void loadUserByUsername_ReturnsCustomUserDetails() {
        // given
        String email = "test@test.com";
        UserEntity userEntity = createUserEntity(email, UserRole.USER, UserStatus.ACTIVE);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userEntity));

        // when
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        // then
        assertThat(userDetails).isInstanceOf(CustomUserDetails.class);

        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
        assertThat(customUserDetails.getUserId()).isEqualTo(1L);
        assertThat(customUserDetails.getUserRole()).isEqualTo(UserRole.USER);

        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("사용자 로드 - 다양한 이메일 형식")
    void loadUserByUsername_VariousEmailFormats() {
        // given
        String[] testEmails = {
                "user@example.com",
                "test.user@domain.co.kr",
                "admin123@test.org",
                "user+tag@gmail.com"
        };

        for (String email : testEmails) {
            UserEntity userEntity = createUserEntity(email, UserRole.USER, UserStatus.ACTIVE);
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(userEntity));

            // when
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

            // then
            assertThat(userDetails.getUsername()).isEqualTo(email);
        }

        verify(userRepository, times(testEmails.length)).findByEmail(anyString());
    }
}
