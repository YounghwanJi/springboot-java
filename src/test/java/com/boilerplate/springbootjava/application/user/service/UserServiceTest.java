package com.boilerplate.springbootjava.application.user.service;

import com.boilerplate.springbootjava.adapter.in.web.v1.user.dto.UserCreateRequestDto;
import com.boilerplate.springbootjava.adapter.in.web.v1.user.dto.UserResponseDto;
import com.boilerplate.springbootjava.adapter.in.web.v1.user.dto.UserUpdateRequestDto;
import com.boilerplate.springbootjava.application.user.port.out.UserRepository;
import com.boilerplate.springbootjava.common.dto.PageResponseDto;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UserService 단위 테스트
 * - Repository, PasswordEncoder Mock 사용
 * - 비즈니스 로직만 검증 (캐싱은 통합 테스트에서)
 * - 성공/실패 케이스 분리
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    // ========== 헬퍼 메서드 ==========

    private UserCreateRequestDto createUserRequest() {
        return new UserCreateRequestDto(
                "test@test.com",
                "password123",
                "Test User",
                "010-1234-5678"
        );
    }

    private UserEntity createUserEntity(Long id, String email) {
        return UserEntity.builder()
                .id(id)
                .email(email)
                .password("$2a$10$encodedPassword")
                .role(UserRole.USER)
                .name("Test User")
                .phoneNumber("010-1234-5678")
                .status(UserStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    // ========== 사용자 생성 테스트 ==========

    @Test
    @DisplayName("사용자 생성 - 정상 동작")
    void createUser_Success() {
        // given
        UserCreateRequestDto request = createUserRequest();
        UserEntity savedEntity = createUserEntity(1L, request.email());

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("$2a$10$encodedPassword");
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedEntity);

        // when
        UserResponseDto response = userService.createUser(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo(request.email());
        assertThat(response.name()).isEqualTo(request.name());
        assertThat(response.phoneNumber()).isEqualTo(request.phoneNumber());
        assertThat(response.role()).isEqualTo(UserRole.USER);
        assertThat(response.status()).isEqualTo(UserStatus.ACTIVE);

        verify(userRepository).existsByEmail(request.email());
        verify(passwordEncoder).encode(request.password());
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("사용자 생성 - 중복 이메일 예외")
    void createUser_DuplicateEmail_ThrowsException() {
        // given
        UserCreateRequestDto request = createUserRequest();
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("이미 존재하는 이메일입니다")
                .extracting("errorCode")
                .isEqualTo(UserErrorCode.EMAIL_CONFLICT);

        verify(userRepository).existsByEmail(request.email());
        verify(userRepository, never()).save(any(UserEntity.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    // ========== 사용자 단건 조회 테스트 ==========

    @Test
    @DisplayName("사용자 조회 - 정상 동작")
    void getUser_Success() {
        // given
        Long userId = 1L;
        UserEntity entity = createUserEntity(userId, "test@test.com");
        when(userRepository.findById(userId)).thenReturn(Optional.of(entity));

        // when
        UserResponseDto response = userService.getUser(userId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(userId);
        assertThat(response.email()).isEqualTo("test@test.com");
        assertThat(response.name()).isEqualTo("Test User");
        assertThat(response.phoneNumber()).isEqualTo("010-1234-5678");

        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("사용자 조회 - 존재하지 않는 사용자 예외")
    void getUser_NotFound_ThrowsException() {
        // given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getUser(userId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다")
                .extracting("errorCode")
                .isEqualTo(UserErrorCode.USER_NOT_FOUND);

        verify(userRepository).findById(userId);
    }

    // ========== 사용자 목록 조회 테스트 ==========

    @Test
    @DisplayName("모든 사용자 조회 - 정상 동작")
    void getAllUsers_Success() {
        // given
        List<UserEntity> entities = List.of(
                createUserEntity(1L, "user1@test.com"),
                createUserEntity(2L, "user2@test.com"),
                createUserEntity(3L, "user3@test.com")
        );
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserEntity> page = new PageImpl<>(entities, pageable, entities.size());

        when(userRepository.findAll(pageable)).thenReturn(page);

        // when
        PageResponseDto<UserResponseDto> response = userService.getAllUsers(pageable);

        // then
        assertThat(response).isNotNull();
        assertThat(response.content()).hasSize(3);
        assertThat(response.page()).isEqualTo(0);
        assertThat(response.size()).isEqualTo(10);
        assertThat(response.totalElements()).isEqualTo(3);
        assertThat(response.totalPages()).isEqualTo(1);
        assertThat(response.hasNext()).isFalse();
        assertThat(response.hasPrev()).isFalse();

        verify(userRepository).findAll(pageable);
    }

    @Test
    @DisplayName("모든 사용자 조회 - 페이징 (2페이지)")
    void getAllUsers_WithPagination() {
        // given
        List<UserEntity> entities = List.of(
                createUserEntity(11L, "user11@test.com"),
                createUserEntity(12L, "user12@test.com")
        );
        Pageable pageable = PageRequest.of(1, 10);  // 2번째 페이지
        Page<UserEntity> page = new PageImpl<>(entities, pageable, 22);  // 전체 22개

        when(userRepository.findAll(pageable)).thenReturn(page);

        // when
        PageResponseDto<UserResponseDto> response = userService.getAllUsers(pageable);

        // then
        assertThat(response).isNotNull();
        assertThat(response.content()).hasSize(2);
        assertThat(response.page()).isEqualTo(1);
        assertThat(response.size()).isEqualTo(10);
        assertThat(response.totalElements()).isEqualTo(22);
        assertThat(response.totalPages()).isEqualTo(3);
        assertThat(response.hasNext()).isTrue();   // 3페이지 존재
        assertThat(response.hasPrev()).isTrue();   // 1페이지 존재

        verify(userRepository).findAll(pageable);
    }

    @Test
    @DisplayName("모든 사용자 조회 - 빈 결과")
    void getAllUsers_EmptyResult() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserEntity> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(userRepository.findAll(pageable)).thenReturn(emptyPage);

        // when
        PageResponseDto<UserResponseDto> response = userService.getAllUsers(pageable);

        // then
        assertThat(response).isNotNull();
        assertThat(response.content()).isEmpty();
        assertThat(response.totalElements()).isEqualTo(0);
        assertThat(response.totalPages()).isEqualTo(0);

        verify(userRepository).findAll(pageable);
    }

    // ========== 사용자 수정 테스트 ==========

    @Test
    @DisplayName("사용자 수정 - 정상 동작")
    void updateUser_Success() {
        // given
        Long userId = 1L;
        UserUpdateRequestDto updateRequest = new UserUpdateRequestDto(
                "Updated Name",
                "010-9999-9999"
        );

        UserEntity existingEntity = createUserEntity(userId, "test@test.com");
        UserEntity updatedEntity = UserEntity.builder()
                .id(userId)
                .email(existingEntity.getEmail())
                .password(existingEntity.getPassword())
                .role(existingEntity.getRole())
                .name("Updated Name")
                .phoneNumber("010-9999-9999")
                .status(existingEntity.getStatus())
                .createdAt(existingEntity.getCreatedAt())
                .updatedAt(existingEntity.getUpdatedAt())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingEntity));
        when(userRepository.save(any(UserEntity.class))).thenReturn(updatedEntity);

        // when
        UserResponseDto response = userService.updateUser(userId, updateRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(userId);
        assertThat(response.name()).isEqualTo("Updated Name");
        assertThat(response.phoneNumber()).isEqualTo("010-9999-9999");

        verify(userRepository).findById(userId);
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("사용자 수정 - 이름만 수정")
    void updateUser_NameOnly() {
        // given
        Long userId = 1L;
        UserUpdateRequestDto updateRequest = new UserUpdateRequestDto(
                "Updated Name",
                null  // phoneNumber는 null
        );

        UserEntity existingEntity = createUserEntity(userId, "test@test.com");
        UserEntity updatedEntity = UserEntity.builder()
                .id(userId)
                .email(existingEntity.getEmail())
                .password(existingEntity.getPassword())
                .role(existingEntity.getRole())
                .name("Updated Name")
                .phoneNumber(existingEntity.getPhoneNumber())  // 기존 값 유지
                .status(existingEntity.getStatus())
                .createdAt(existingEntity.getCreatedAt())
                .updatedAt(existingEntity.getUpdatedAt())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingEntity));
        when(userRepository.save(any(UserEntity.class))).thenReturn(updatedEntity);

        // when
        UserResponseDto response = userService.updateUser(userId, updateRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("Updated Name");
        assertThat(response.phoneNumber()).isEqualTo("010-1234-5678");  // 기존 값 유지

        verify(userRepository).findById(userId);
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("사용자 수정 - 존재하지 않는 사용자 예외")
    void updateUser_NotFound_ThrowsException() {
        // given
        Long userId = 999L;
        UserUpdateRequestDto updateRequest = new UserUpdateRequestDto(
                "Updated Name",
                "010-9999-9999"
        );

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.updateUser(userId, updateRequest))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다")
                .extracting("errorCode")
                .isEqualTo(UserErrorCode.USER_NOT_FOUND);

        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    // ========== 사용자 삭제 테스트 ==========

    @Test
    @DisplayName("사용자 삭제 - 정상 동작")
    void deleteUser_Success() {
        // given
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);
        doNothing().when(userRepository).deleteById(userId);

        // when
        userService.deleteUser(userId);

        // then
        verify(userRepository).existsById(userId);
        verify(userRepository).deleteById(userId);
    }

    @Test
    @DisplayName("사용자 삭제 - 존재하지 않는 사용자 예외")
    void deleteUser_NotFound_ThrowsException() {
        // given
        Long userId = 999L;
        when(userRepository.existsById(userId)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다")
                .extracting("errorCode")
                .isEqualTo(UserErrorCode.USER_NOT_FOUND);

        verify(userRepository).existsById(userId);
        verify(userRepository, never()).deleteById(anyLong());
    }
}
