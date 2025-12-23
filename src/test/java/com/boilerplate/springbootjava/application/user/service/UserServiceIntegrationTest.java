package com.boilerplate.springbootjava.application.user.service;

import com.boilerplate.springbootjava.adapter.in.web.v1.user.dto.UserCreateRequestDto;
import com.boilerplate.springbootjava.adapter.in.web.v1.user.dto.UserResponseDto;
import com.boilerplate.springbootjava.adapter.in.web.v1.user.dto.UserUpdateRequestDto;
import com.boilerplate.springbootjava.application.user.port.out.UserRepository;
import com.boilerplate.springbootjava.common.dto.PageResponseDto;
import com.boilerplate.springbootjava.config.AbstractIntegrationTest;
import com.boilerplate.springbootjava.infrastructure.persistence.user.UserEntity;
import com.boilerplate.springbootjava.infrastructure.persistence.user.UserRole;
import com.boilerplate.springbootjava.infrastructure.persistence.user.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

/**
 * UserService 통합 테스트
 * - @SpringBootTest + PostgreSQL + Redis 컨테이너 사용
 * - Redis 캐싱 동작 집중 검증
 * - 실제 Repository, PasswordEncoder 사용
 */
class UserServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    @Transactional
    void setUp() {
        // 각 테스트 전 데이터 초기화
        userRepository.deleteAll();
        userRepository.flush();

        // 캐시 초기화 (AbstractIntegrationTest에서도 수행하지만 명시적으로 재확인)
        clearAllCaches();
    }

    private void clearAllCaches() {
        if (cacheManager != null) {
            cacheManager.getCacheNames().forEach(cacheName -> {
                Cache cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    cache.clear();
                }
            });
        }
    }

    // ========== 헬퍼 메서드 ==========

    private UserCreateRequestDto createUserRequest(String email) {
        return new UserCreateRequestDto(
                email,
                "password123",
                "Test User",
                "010-1234-5678"
        );
    }

    private UserEntity createAndSaveUser(String email, String name) {
        UserEntity user = UserEntity.builder()
                .email(email)
                .password(passwordEncoder.encode("password123"))
                .role(UserRole.USER)
                .name(name)
                .phoneNumber("010-1234-5678")
                .status(UserStatus.ACTIVE)
                .build();
        return userRepository.saveAndFlush(user);
    }

    // ========== 캐싱 검증 테스트 ==========

    @Test
    @DisplayName("사용자 조회 - Cache Miss (첫 조회)")
    void getUser_CacheMiss() {
        // given
        UserEntity savedUser = createAndSaveUser("cache-miss@test.com", "Cache User");
        Long userId = savedUser.getId();

        // when - 첫 조회 (Cache Miss)
        UserResponseDto response1 = userService.getUser(userId);

        // then
        assertThat(response1).isNotNull();
        assertThat(response1.id()).isEqualTo(userId);
        assertThat(response1.email()).isEqualTo("cache-miss@test.com");

        // 캐시에 저장되었는지 확인
        Cache usersCache = cacheManager.getCache("users");
        assertThat(usersCache).isNotNull();
        Cache.ValueWrapper cachedValue = usersCache.get(userId);
        assertThat(cachedValue).isNotNull();
        assertThat(cachedValue.get()).isInstanceOf(UserResponseDto.class);
    }

    @Test
    @DisplayName("사용자 조회 - Cache Hit (두 번째 조회)")
    void getUser_CacheHit() {
        // given
        UserEntity savedUser = createAndSaveUser("cache-hit@test.com", "Cache User");
        Long userId = savedUser.getId();

        // when - 첫 조회 (Cache Miss)
        UserResponseDto response1 = userService.getUser(userId);

        // DB에서 삭제 (캐시에서만 존재하도록)
        userRepository.deleteById(userId);
        userRepository.flush();

        // 두 번째 조회 (Cache Hit - DB에 없지만 캐시에서 반환)
        UserResponseDto response2 = userService.getUser(userId);

        // then - 캐시에서 반환되어 데이터가 동일
        assertThat(response2).isNotNull();
        assertThat(response2.id()).isEqualTo(userId);
        assertThat(response2.email()).isEqualTo(response1.email());
        assertThat(response2.name()).isEqualTo(response1.name());
    }

    @Test
    @DisplayName("사용자 생성 - 목록 캐시 무효화 (@CacheEvict)")
    void createUser_EvictListCache() {
        // given - 목록 조회로 캐시 생성
        Pageable pageable = PageRequest.of(0, 10);
        userService.getAllUsers(pageable);

        // 캐시가 생성되었는지 확인
        Cache userListCache = cacheManager.getCache("userList");
        assertThat(userListCache).isNotNull();
        String cacheKey = "0:10:UNSORTED";
        assertThat(userListCache.get(cacheKey)).isNotNull();

        // when - 새 사용자 생성 (목록 캐시 무효화)
        UserCreateRequestDto request = createUserRequest("new@test.com");
        userService.createUser(request);

        // then - 목록 캐시가 무효화됨
        assertThat(userListCache.get(cacheKey)).isNull();
    }

    @Test
    @DisplayName("사용자 수정 - 단건 캐시 갱신 + 목록 캐시 무효화 (@Caching)")
    void updateUser_UpdateCacheAndEvictList() {
        // given
        UserEntity savedUser = createAndSaveUser("update@test.com", "Original Name");
        Long userId = savedUser.getId();

        // 단건 조회로 캐시 생성
        userService.getUser(userId);

        // 목록 조회로 캐시 생성
        Pageable pageable = PageRequest.of(0, 10);
        userService.getAllUsers(pageable);

        Cache usersCache = cacheManager.getCache("users");
        Cache userListCache = cacheManager.getCache("userList");
        String listCacheKey = "0:10:UNSORTED";

        assertThat(usersCache.get(userId)).isNotNull();
        assertThat(userListCache.get(listCacheKey)).isNotNull();

        // when - 사용자 수정
        UserUpdateRequestDto updateRequest = new UserUpdateRequestDto(
                "Updated Name",
                "010-9999-9999"
        );
        UserResponseDto updatedResponse = userService.updateUser(userId, updateRequest);

        // then
        assertThat(updatedResponse.name()).isEqualTo("Updated Name");

        // 단건 캐시는 갱신됨 (@CachePut)
        Cache.ValueWrapper cachedUser = usersCache.get(userId);
        assertThat(cachedUser).isNotNull();
        UserResponseDto cachedDto = (UserResponseDto) cachedUser.get();
        assertThat(cachedDto.name()).isEqualTo("Updated Name");

        // 목록 캐시는 무효화됨 (@CacheEvict)
        assertThat(userListCache.get(listCacheKey)).isNull();
    }

    @Test
    @DisplayName("사용자 삭제 - 모든 캐시 무효화 (@Caching evict)")
    void deleteUser_EvictAllCaches() {
        // given
        UserEntity savedUser = createAndSaveUser("delete@test.com", "Delete User");
        Long userId = savedUser.getId();

        // 단건 조회로 캐시 생성
        userService.getUser(userId);

        // 목록 조회로 캐시 생성
        Pageable pageable = PageRequest.of(0, 10);
        userService.getAllUsers(pageable);

        Cache usersCache = cacheManager.getCache("users");
        Cache userListCache = cacheManager.getCache("userList");
        String listCacheKey = "0:10:UNSORTED";

        assertThat(usersCache.get(userId)).isNotNull();
        assertThat(userListCache.get(listCacheKey)).isNotNull();

        // when - 사용자 삭제
        userService.deleteUser(userId);

        // then - 모든 관련 캐시가 무효화됨
        assertThat(usersCache.get(userId)).isNull();
        assertThat(userListCache.get(listCacheKey)).isNull();
    }

    @Test
    @DisplayName("사용자 목록 조회 - 첫 5페이지만 캐싱 (@Cacheable condition)")
    void getAllUsers_CachesFirstFivePages() {
        // given - 테스트 데이터 생성 (60개)
        for (int i = 1; i <= 60; i++) {
            createAndSaveUser("user" + i + "@test.com", "User " + i);
        }

        Cache userListCache = cacheManager.getCache("userList");

        // when - 1페이지 조회 (캐싱됨)
        Pageable pageable0 = PageRequest.of(0, 10);
        userService.getAllUsers(pageable0);
        String cacheKey0 = "0:10:UNSORTED";
        assertThat(userListCache.get(cacheKey0)).isNotNull();

        // 4페이지 조회 (캐싱됨 - condition: page < 5)
        Pageable pageable4 = PageRequest.of(4, 10);
        userService.getAllUsers(pageable4);
        String cacheKey4 = "4:10:UNSORTED";
        assertThat(userListCache.get(cacheKey4)).isNotNull();

        // 5페이지 조회 (캐싱 안 됨 - condition: page < 5)
        Pageable pageable5 = PageRequest.of(5, 10);
        userService.getAllUsers(pageable5);
        String cacheKey5 = "5:10:UNSORTED";
        assertThat(userListCache.get(cacheKey5)).isNull();

        // 10페이지 조회 (캐싱 안 됨)
        Pageable pageable10 = PageRequest.of(10, 10);
        userService.getAllUsers(pageable10);
        String cacheKey10 = "10:10:UNSORTED";
        assertThat(userListCache.get(cacheKey10)).isNull();
    }

    @Test
    @DisplayName("사용자 목록 조회 - 캐시 키는 page + size + sort 조합")
    void getAllUsers_CacheKeyIncludesPageSizeSort() {
        // given
        createAndSaveUser("user1@test.com", "User 1");
        createAndSaveUser("user2@test.com", "User 2");

        Cache userListCache = cacheManager.getCache("userList");

        // when - 동일 페이지, 다른 크기
        Pageable pageable1 = PageRequest.of(0, 10);
        userService.getAllUsers(pageable1);
        String cacheKey1 = "0:10:UNSORTED";
        assertThat(userListCache.get(cacheKey1)).isNotNull();

        Pageable pageable2 = PageRequest.of(0, 20);
        userService.getAllUsers(pageable2);
        String cacheKey2 = "0:20:UNSORTED";
        assertThat(userListCache.get(cacheKey2)).isNotNull();

        // then - 서로 다른 캐시 키 사용
        assertThat(cacheKey1).isNotEqualTo(cacheKey2);
    }

    @Test
    @DisplayName("통합 시나리오 - 생성 → 조회(캐싱) → 수정(캐시갱신) → 삭제(캐시무효화)")
    void integrationScenario_CreateReadUpdateDelete() {
        // 1. 생성
        UserCreateRequestDto createRequest = createUserRequest("scenario@test.com");
        UserResponseDto created = userService.createUser(createRequest);
        Long userId = created.id();

        assertThat(created.email()).isEqualTo("scenario@test.com");
        assertThat(created.name()).isEqualTo("Test User");

        // 2. 조회 - 캐시 생성
        UserResponseDto read1 = userService.getUser(userId);
        assertThat(read1.id()).isEqualTo(userId);

        Cache usersCache = cacheManager.getCache("users");
        assertThat(usersCache.get(userId)).isNotNull();

        // 3. 수정 - 캐시 갱신
        UserUpdateRequestDto updateRequest = new UserUpdateRequestDto(
                "Updated User",
                "010-9999-9999"
        );
        UserResponseDto updated = userService.updateUser(userId, updateRequest);
        assertThat(updated.name()).isEqualTo("Updated User");

        // 캐시에서 조회 - 갱신된 값
        Cache.ValueWrapper cachedAfterUpdate = usersCache.get(userId);
        assertThat(cachedAfterUpdate).isNotNull();
        UserResponseDto cachedDto = (UserResponseDto) cachedAfterUpdate.get();
        assertThat(cachedDto.name()).isEqualTo("Updated User");

        // 4. 삭제 - 캐시 무효화
        userService.deleteUser(userId);
        assertThat(usersCache.get(userId)).isNull();
    }

    @Test
    @DisplayName("암호화 필드 통합 검증 - 생성 후 조회 시 복호화")
    void encryptedFields_Integration() {
        // given
        String originalName = "홍길동";
        String originalPhone = "010-1234-5678";

        UserCreateRequestDto request = new UserCreateRequestDto(
                "encrypted@test.com",
                "password123",
                originalName,
                originalPhone
        );

        // when
        UserResponseDto created = userService.createUser(request);

        // 캐시 초기화 후 다시 조회 (DB에서 직접 조회)
        clearAllCaches();
        UserResponseDto retrieved = userService.getUser(created.id());

        // then - 암호화된 필드가 정상적으로 복호화됨
        assertThat(retrieved.name()).isEqualTo(originalName);
        assertThat(retrieved.phoneNumber()).isEqualTo(originalPhone);
    }
}
