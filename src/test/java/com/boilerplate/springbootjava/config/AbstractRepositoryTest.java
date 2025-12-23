package com.boilerplate.springbootjava.config;

import com.boilerplate.springbootjava.infrastructure.converter.SecureStringConverter;
import com.boilerplate.springbootjava.infrastructure.util.AesGcmEncryptionAdapter;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Repository 레이어 테스트를 위한 베이스 클래스
 * - @DataJpaTest를 사용하여 JPA 관련 빈만 로드 (경량 컨텍스트)
 * - TestContainers로 PostgreSQL 컨테이너 실행
 * - 암호화 컨버터를 위한 AesGcmEncryptionAdapter, SecureStringConverter Import
 * - 각 테스트는 자동으로 @Transactional + 롤백됨
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@ActiveProfiles("test")
@Import({AesGcmEncryptionAdapter.class, SecureStringConverter.class})
public abstract class AbstractRepositoryTest {

    /**
     * PostgreSQL 컨테이너 (싱글톤으로 재사용)
     * - postgres:16-alpine 이미지 사용
     * - withReuse(true)로 컨테이너 재사용 (성능 향상)
     * - withInitScript로 스키마 초기화 (선택적)
     */
    @Container
    static PostgreSQLContainer<?> postgresContainer =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test")
                    .withReuse(true);

    /**
     * 동적 프로퍼티 설정
     * - 컨테이너 실행 후 JDBC URL 동적 설정
     * - 암호화 키 설정
     */
    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        // PostgreSQL 설정
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);

        // 암호화 키 설정 (정확히 32자)
        registry.add("encryption.secret.key",
                () -> "test-encryption-key-123456789012");
    }
}
