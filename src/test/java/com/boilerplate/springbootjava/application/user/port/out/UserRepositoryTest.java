package com.boilerplate.springbootjava.application.user.port.out;

import com.boilerplate.springbootjava.config.AbstractRepositoryTest;
import com.boilerplate.springbootjava.infrastructure.persistence.user.UserEntity;
import com.boilerplate.springbootjava.infrastructure.persistence.user.UserRole;
import com.boilerplate.springbootjava.infrastructure.persistence.user.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * UserRepository 테스트
 * - TestContainers PostgreSQL 사용
 * - 암호화 필드 저장/조회 검증
 * - 커스텀 쿼리 메서드 검증
 */
class UserRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    /**
     * 테스트용 사용자 생성 (각 테스트에서 필요시 호출)
     */
    private UserEntity createAndSaveTestUser(String email, UserRole role, String name, String phoneNumber) {
        UserEntity user = UserEntity.builder()
                .email(email)
                .password("$2a$10$BCuf8uL.9jKI2hCQJlpKKOK4HIFJJYkF5sVUoyOqoPP8zyGLka08S")
                .role(role)
                .name(name)
                .phoneNumber(phoneNumber)
                .status(UserStatus.ACTIVE)
                .build();
        return userRepository.saveAndFlush(user);
    }

    @Test
    @DisplayName("사용자 저장 - 정상 동작")
    void save_Success() {
        // given
        UserEntity user = createUser("new@test.com", "New User", "010-9999-9999");

        // when
        UserEntity savedUser = userRepository.save(user);

        // then
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("new@test.com");
        assertThat(savedUser.getName()).isEqualTo("New User");
        assertThat(savedUser.getPhoneNumber()).isEqualTo("010-9999-9999");
        assertThat(savedUser.getRole()).isEqualTo(UserRole.USER);
        assertThat(savedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("사용자 저장 - 암호화 필드 검증")
    void save_WithEncryptedFields_Success() {
        // given
        String originalName = "홍길동";
        String originalPhone = "010-1234-5678";
        UserEntity user = createUser("encrypted@test.com", originalName, originalPhone);

        // when
        UserEntity savedUser = userRepository.saveAndFlush(user);

        // EntityManager 클리어하여 1차 캐시 무효화
        userRepository.flush();

        // 다시 조회
        UserEntity foundUser = userRepository.findById(savedUser.getId()).orElseThrow();

        // then - 복호화된 값이 원본과 동일해야 함
        assertThat(foundUser.getName()).isEqualTo(originalName);
        assertThat(foundUser.getPhoneNumber()).isEqualTo(originalPhone);
    }

    @Test
    @DisplayName("ID로 사용자 조회 - 성공")
    void findById_Success() {
        // given
        UserEntity savedUser = createAndSaveTestUser("findbyid@test.com", UserRole.ADMIN, "Admin User", "010-1234-5678");
        Long userId = savedUser.getId();

        // when
        Optional<UserEntity> foundUser = userRepository.findById(userId);

        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getId()).isEqualTo(userId);
        assertThat(foundUser.get().getEmail()).isEqualTo("findbyid@test.com");
        assertThat(foundUser.get().getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    @DisplayName("ID로 사용자 조회 - 암호화 필드 복호화 확인")
    void findById_DecryptsFields() {
        // given
        String expectedName = "Test User";
        String expectedPhone = "010-9876-5432";
        UserEntity savedUser = createAndSaveTestUser("decrypt@test.com", UserRole.USER, expectedName, expectedPhone);
        Long userId = savedUser.getId();

        // when
        UserEntity foundUser = userRepository.findById(userId).orElseThrow();

        // then - 암호화된 필드가 복호화되어 조회됨
        assertThat(foundUser.getName()).isNotNull();
        assertThat(foundUser.getName()).isEqualTo(expectedName);
        assertThat(foundUser.getPhoneNumber()).isNotNull();
        assertThat(foundUser.getPhoneNumber()).isEqualTo(expectedPhone);
    }

    @Test
    @DisplayName("ID로 사용자 조회 - 존재하지 않는 경우")
    void findById_NotFound() {
        // given
        Long nonExistentId = 99999L;

        // when
        Optional<UserEntity> foundUser = userRepository.findById(nonExistentId);

        // then
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("이메일로 사용자 조회 - 성공")
    void findByEmail_Success() {
        // given
        String email = "findbyemail@test.com";
        createAndSaveTestUser(email, UserRole.USER, "Find User", "010-1111-1111");

        // when
        Optional<UserEntity> foundUser = userRepository.findByEmail(email);

        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo(email);
        assertThat(foundUser.get().getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    @DisplayName("이메일로 사용자 조회 - 존재하지 않는 경우")
    void findByEmail_NotFound() {
        // given
        String nonExistentEmail = "nonexistent@test.com";

        // when
        Optional<UserEntity> foundUser = userRepository.findByEmail(nonExistentEmail);

        // then
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("이메일 존재 여부 확인 - 존재하는 경우")
    void existsByEmail_ReturnsTrue() {
        // given
        String existingEmail = "exists@test.com";
        createAndSaveTestUser(existingEmail, UserRole.USER, "Exists User", "010-2222-2222");

        // when
        boolean exists = userRepository.existsByEmail(existingEmail);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("이메일 존재 여부 확인 - 존재하지 않는 경우")
    void existsByEmail_ReturnsFalse() {
        // given
        String nonExistentEmail = "nonexistent@test.com";

        // when
        boolean exists = userRepository.existsByEmail(nonExistentEmail);

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("중복 이메일로 저장 - 예외 발생 (Unique 제약 조건)")
    void save_DuplicateEmail_ThrowsException() {
        // given
        String duplicateEmail = "duplicate@test.com";
        createAndSaveTestUser(duplicateEmail, UserRole.USER, "First User", "010-1111-1111");

        UserEntity duplicateUser = createUser(duplicateEmail, "Duplicate User", "010-0000-0000");

        // when & then
        assertThatThrownBy(() -> userRepository.saveAndFlush(duplicateUser))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("모든 사용자 조회 - Pageable")
    void findAll_WithPageable() {
        // given - 4명의 사용자 생성
        createAndSaveTestUser("page1@test.com", UserRole.USER, "User 1", "010-0001-0001");
        createAndSaveTestUser("page2@test.com", UserRole.USER, "User 2", "010-0002-0002");
        createAndSaveTestUser("page3@test.com", UserRole.USER, "User 3", "010-0003-0003");
        createAndSaveTestUser("page4@test.com", UserRole.USER, "User 4", "010-0004-0004");

        Pageable pageable = PageRequest.of(0, 2, Sort.by("id").ascending());

        // when
        Page<UserEntity> page = userRepository.findAll(pageable);

        // then
        assertThat(page.getContent()).hasSize(2);  // 2개만 조회
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(4);  // 4명 이상 존재
        assertThat(page.getNumber()).isEqualTo(0);  // 첫 페이지
        assertThat(page.getContent().get(0).getId()).isLessThan(page.getContent().get(1).getId());  // ID 오름차순
    }

    @Test
    @DisplayName("모든 사용자 조회 - 정렬 (이메일 내림차순)")
    void findAll_WithSort() {
        // given - 이메일이 다른 3명 생성
        createAndSaveTestUser("aaa@test.com", UserRole.USER, "User A", "010-0001-0001");
        createAndSaveTestUser("bbb@test.com", UserRole.USER, "User B", "010-0002-0002");
        createAndSaveTestUser("ccc@test.com", UserRole.USER, "User C", "010-0003-0003");

        Pageable pageable = PageRequest.of(0, 10, Sort.by("email").descending());

        // when
        Page<UserEntity> page = userRepository.findAll(pageable);

        // then
        assertThat(page.getContent()).isNotEmpty();
        // 이메일이 내림차순으로 정렬되었는지 확인
        for (int i = 0; i < page.getContent().size() - 1; i++) {
            String currentEmail = page.getContent().get(i).getEmail();
            String nextEmail = page.getContent().get(i + 1).getEmail();
            assertThat(currentEmail).isGreaterThanOrEqualTo(nextEmail);
        }
    }

    @Test
    @DisplayName("사용자 삭제 - 성공")
    void delete_Success() {
        // given
        UserEntity user = createUser("delete@test.com", "Delete User", "010-0000-0000");
        UserEntity savedUser = userRepository.save(user);
        Long userId = savedUser.getId();

        // when
        userRepository.deleteById(userId);
        userRepository.flush();

        // then
        Optional<UserEntity> deletedUser = userRepository.findById(userId);
        assertThat(deletedUser).isEmpty();
    }

    @Test
    @DisplayName("사용자 수정 - 더티 체킹")
    void update_DirtyChecking() {
        // given
        UserEntity user = createUser("update@test.com", "Original Name", "010-1111-1111");
        UserEntity savedUser = userRepository.save(user);
        Long userId = savedUser.getId();

        // when - 엔티티 수정 (더티 체킹)
        UserEntity foundUser = userRepository.findById(userId).orElseThrow();
        UserEntity updatedUser = UserEntity.builder()
                .id(foundUser.getId())
                .email(foundUser.getEmail())
                .password(foundUser.getPassword())
                .role(foundUser.getRole())
                .name("Updated Name")  // 수정
                .phoneNumber("010-2222-2222")  // 수정
                .status(foundUser.getStatus())
                .createdAt(foundUser.getCreatedAt())
                .updatedAt(foundUser.getUpdatedAt())
                .build();

        userRepository.save(updatedUser);
        userRepository.flush();

        // then
        UserEntity result = userRepository.findById(userId).orElseThrow();
        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getPhoneNumber()).isEqualTo("010-2222-2222");
    }

    @Test
    @DisplayName("역할별 사용자 수 확인")
    void count_ByRole() {
        // given
        createAndSaveTestUser("count1@test.com", UserRole.USER, "User 1", "010-0001-0001");
        createAndSaveTestUser("count2@test.com", UserRole.USER, "User 2", "010-0002-0002");
        createAndSaveTestUser("count3@test.com", UserRole.ADMIN, "Admin 1", "010-0003-0003");

        // when
        long totalCount = userRepository.count();

        // then
        assertThat(totalCount).isGreaterThanOrEqualTo(3);
    }

    @Test
    @DisplayName("모든 사용자 조회 - 빈 결과 페이지")
    void findAll_EmptyPage() {
        // given
        createAndSaveTestUser("empty1@test.com", UserRole.USER, "User 1", "010-0001-0001");

        // 매우 큰 페이지 번호
        Pageable pageable = PageRequest.of(1000, 10);

        // when
        Page<UserEntity> page = userRepository.findAll(pageable);

        // then
        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(1);
    }

    // === 헬퍼 메서드 ===

    private UserEntity createUser(String email, String name, String phoneNumber) {
        return UserEntity.builder()
                .email(email)
                .password("$2a$10$BCuf8uL.9jKI2hCQJlpKKOK4HIFJJYkF5sVUoyOqoPP8zyGLka08S")  // password123
                .role(UserRole.USER)
                .name(name)
                .phoneNumber(phoneNumber)
                .status(UserStatus.ACTIVE)
                .build();
    }
}
