package com.boilerplate.springbootjava.application.user.service;

import com.boilerplate.springbootjava.adapter.in.web.v1.user.dto.UserCreateRequestDto;
import com.boilerplate.springbootjava.adapter.in.web.v1.user.dto.UserResponseDto;
import com.boilerplate.springbootjava.adapter.in.web.v1.user.dto.UserUpdateRequestDto;
import com.boilerplate.springbootjava.application.user.port.in.UserUseCase;
import com.boilerplate.springbootjava.application.user.port.out.UserRepository;
import com.boilerplate.springbootjava.common.dto.PageResponseDto;
import com.boilerplate.springbootjava.infrastructure.persistence.user.UserEntity;
import com.boilerplate.springbootjava.infrastructure.persistence.user.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService implements UserUseCase {
    private final UserRepository userRepository;

    /**
     * 사용자 생성
     */
    @Transactional
    public UserResponseDto createUser(UserCreateRequestDto request) {
        // 이메일 중복 체크
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다: " + request.email());
        }

        // 엔티티 생성 (실제로는 비밀번호 암호화 필요)
        UserEntity user = UserEntity.builder()
                .email(request.email())
                .password(request.password()) // TODO: 암호화 필요 (BCrypt 등)
                .name(request.name())
                .phoneNumber(request.phoneNumber())
                .status(UserStatus.ACTIVE)
                .build();

        UserEntity savedUser = userRepository.save(user);
        return UserResponseDto.from(savedUser);
    }

    /**
     * 사용자 조회 (단건)
     */
    public UserResponseDto getUser(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + id));
        return UserResponseDto.from(user);
    }

    /**
     * 모든 사용자 조회 (페이징)
     */
    public PageResponseDto<UserResponseDto> getAllUsers(Pageable pageable) {
        Page<UserEntity> page = userRepository.findAll(pageable);

        // Page<UserEntity> -> Page<UserResponseDto>
        Page<UserResponseDto> mapped = page.map(UserResponseDto::from);

        // Page -> PageResponseDto
        return PageResponseDto.from(mapped);
    }

    /**
     * 사용자 정보 수정
     */
    @Transactional
    public UserResponseDto updateUser(Long id, UserUpdateRequestDto request) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + id));

        // 변경 감지(Dirty Checking)를 통한 업데이트
        // UserEntity에 업데이트 메서드 추가 필요
        if (request.name() != null) {
            user = UserEntity.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .password(user.getPassword())
                    .name(request.name())
                    .phoneNumber(request.phoneNumber() != null ? request.phoneNumber() : user.getPhoneNumber())
                    .status(user.getStatus())
                    .createdAt(user.getCreatedAt())
                    .updatedAt(user.getUpdatedAt())
                    .build();
            user = userRepository.save(user);
        }

        return UserResponseDto.from(user);
    }

    /**
     * 사용자 삭제
     */
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다: " + id);
        }
        userRepository.deleteById(id);
    }
}