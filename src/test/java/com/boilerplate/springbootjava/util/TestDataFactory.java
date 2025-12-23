package com.boilerplate.springbootjava.util;

import com.boilerplate.springbootjava.adapter.in.web.v1.auth.dto.LoginRequestDto;
import com.boilerplate.springbootjava.adapter.in.web.v1.user.dto.UserCreateRequestDto;
import com.boilerplate.springbootjava.adapter.in.web.v1.user.dto.UserUpdateRequestDto;
import com.boilerplate.springbootjava.infrastructure.persistence.user.UserEntity;
import com.boilerplate.springbootjava.infrastructure.persistence.user.UserRole;
import com.boilerplate.springbootjava.infrastructure.persistence.user.UserStatus;
import org.springframework.stereotype.Component;

/**
 * 테스트 데이터 생성 팩토리
 * - 테스트에서 사용할 Entity, DTO 객체를 간편하게 생성
 */
@Component
public class TestDataFactory {

    /**
     * 기본 UserEntity 생성
     * - 비밀번호는 BCrypt 인코딩된 "password123"
     */
    public UserEntity createUser(String email, UserRole role) {
        return UserEntity.builder()
                .email(email)
                .role(role)
                .password("$2a$10$BCuf8uL.9jKI2hCQJlpKKOK4HIFJJYkF5sVUoyOqoPP8zyGLka08S")  // password123
                .name("Test User")
                .phoneNumber("010-1234-5678")
                .status(UserStatus.ACTIVE)
                .build();
    }

    /**
     * 커스텀 필드를 가진 UserEntity 생성
     */
    public UserEntity createUser(
            String email,
            UserRole role,
            String name,
            String phoneNumber,
            UserStatus status
    ) {
        return UserEntity.builder()
                .email(email)
                .role(role)
                .password("$2a$10$BCuf8uL.9jKI2hCQJlpKKOK4HIFJJYkF5sVUoyOqoPP8zyGLka08S")  // password123
                .name(name)
                .phoneNumber(phoneNumber)
                .status(status)
                .build();
    }

    /**
     * 기본 UserCreateRequestDto 생성
     */
    public UserCreateRequestDto createUserRequest() {
        return new UserCreateRequestDto(
                "newuser@test.com",
                "password123",
                "New User",
                "010-9999-8888"
        );
    }

    /**
     * 커스텀 필드를 가진 UserCreateRequestDto 생성
     */
    public UserCreateRequestDto createUserRequest(
            String email,
            String password,
            String name,
            String phoneNumber
    ) {
        return new UserCreateRequestDto(email, password, name, phoneNumber);
    }

    /**
     * 기본 UserUpdateRequestDto 생성
     */
    public UserUpdateRequestDto createUserUpdateRequest() {
        return new UserUpdateRequestDto(
                "Updated Name",
                "010-8888-9999"
        );
    }

    /**
     * 커스텀 필드를 가진 UserUpdateRequestDto 생성
     */
    public UserUpdateRequestDto createUserUpdateRequest(String name, String phoneNumber) {
        return new UserUpdateRequestDto(name, phoneNumber);
    }

    /**
     * 기본 LoginRequestDto 생성
     */
    public LoginRequestDto createLoginRequest() {
        return new LoginRequestDto("user@test.com", "password123");
    }

    /**
     * 커스텀 필드를 가진 LoginRequestDto 생성
     */
    public LoginRequestDto createLoginRequest(String email, String password) {
        return new LoginRequestDto(email, password);
    }
}
