package com.boilerplate.springbootjava.application.user.service;

import com.boilerplate.springbootjava.adapter.in.web.v1.user.dto.UserCreateRequestDto;
import com.boilerplate.springbootjava.adapter.in.web.v1.user.dto.UserResponseDto;
import com.boilerplate.springbootjava.adapter.in.web.v1.user.dto.UserUpdateRequestDto;
import com.boilerplate.springbootjava.application.user.port.in.UserUseCase;
import com.boilerplate.springbootjava.application.user.port.out.UserRepository;
import com.boilerplate.springbootjava.common.dto.PageResponseDto;
import com.boilerplate.springbootjava.common.exception.CustomException;
import com.boilerplate.springbootjava.common.exception.errorcode.UserErrorCode;
import com.boilerplate.springbootjava.infrastructure.persistence.user.UserEntity;
import com.boilerplate.springbootjava.infrastructure.persistence.user.UserRole;
import com.boilerplate.springbootjava.infrastructure.persistence.user.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService implements UserUseCase {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 사용자 생성
     * 목록에 대한 캐시 삭제
     */
    @Transactional
    @CacheEvict(value = "userList", allEntries = true)
    public UserResponseDto createUser(UserCreateRequestDto request) {
        // 이메일 중복 체크
        if (userRepository.existsByEmail(request.email())) {
            throw new CustomException(UserErrorCode.EMAIL_CONFLICT, ("이미 존재하는 이메일입니다: " + request.email()));
        }

        // 엔티티 생성 (실제로는 비밀번호 암호화 필요)
        UserEntity user = UserEntity.builder()
                .email(request.email())
                .role(UserRole.USER)
                .password(passwordEncoder.encode(request.password()))
                .name(request.name())
                .phoneNumber(request.phoneNumber())
                .status(UserStatus.ACTIVE)
                .build();

        UserEntity savedUser = userRepository.save(user);
        return UserResponseDto.from(savedUser);
    }

    /**
     * 사용자 조회 (단건)
     * 캐시에 저장
     */
    @Cacheable(value = "users", key = "#id", unless = "#result == null")
    public UserResponseDto getUser(Long id) {
        log.info("getUser: {} (Cache Miss)", id);

        // Cache test
//        try {
//            Thread.sleep(3000);
//        } catch (InterruptedException e) {
//            return null;
//        }

        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다: " + id));
        return UserResponseDto.from(user);
    }

    /**
     * 모든 사용자 조회 (페이징)
     * 리스트 조회 캐싱
     */
    @Cacheable(value = "userList",
            key = "#pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort",
            condition = "#pageable.pageNumber < 5" // First 5 pages
    )
    public PageResponseDto<UserResponseDto> getAllUsers(Pageable pageable) {
        Page<UserEntity> page = userRepository.findAll(pageable);

        // Page<UserEntity> -> Page<UserResponseDto>
        Page<UserResponseDto> mapped = page.map(UserResponseDto::from);

        // Page -> PageResponseDto
        return PageResponseDto.from(mapped);
    }

    /**
     * 사용자 정보 수정
     * 캐시 갱신 + 리스트 캐시 무효화
     */
    @Transactional
    @Caching(
            put = @CachePut(value = "users", key = "#id"),
            evict = @CacheEvict(value = "userList", allEntries = true)
    )
    public UserResponseDto updateUser(Long id, UserUpdateRequestDto request) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다: " + id));

        // 변경 감지(Dirty Checking)를 통한 업데이트
        // UserEntity에 업데이트 메서드 추가 필요
        if (request.name() != null) {
            user = UserEntity.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .password(user.getPassword())
                    .role(user.getRole())  // role 필드 추가
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
     * 모든 관련 캐시 무효화
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "users", key = "#id"),
            @CacheEvict(value = "userList", allEntries = true)
    })
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new CustomException(UserErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다: " + id);
        }
        userRepository.deleteById(id);
    }
}