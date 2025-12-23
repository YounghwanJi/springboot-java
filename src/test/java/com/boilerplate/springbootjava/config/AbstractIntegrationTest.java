package com.boilerplate.springbootjava.config;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * 통합 테스트를 위한 베이스 클래스
 * - @SpringBootTest를 사용하여 전체 ApplicationContext 로드
 * - TestContainers로 PostgreSQL 및 Redis 컨테이너 실행
 * - 각 테스트 전 캐시 초기화
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    /**
     * PostgreSQL 컨테이너 (싱글톤으로 재사용)
     * - postgres:16-alpine 이미지 사용
     * - withReuse(true)로 컨테이너 재사용 (성능 향상)
     */
    @Container
    static PostgreSQLContainer<?> postgresContainer =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test")
                    .withReuse(true);

    /**
     * Redis 컨테이너 (싱글톤으로 재사용)
     * - redis:7-alpine 이미지 사용
     * - withReuse(true)로 컨테이너 재사용
     */
    @Container
    static GenericContainer<?> redisContainer =
            new GenericContainer<>("redis:7-alpine")
                    .withExposedPorts(6379)
                    .withReuse(true);

    /**
     * 동적 프로퍼티 설정
     * - 컨테이너 실행 후 JDBC URL 및 Redis 호스트/포트를 동적으로 설정
     */
    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        // PostgreSQL 설정
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);

        // Redis 설정
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port",
                () -> redisContainer.getMappedPort(6379).toString());
    }

    @Autowired
    protected CacheManager cacheManager;

    /**
     * 각 테스트 전 캐시 초기화
     * - 테스트 간 캐시 격리 보장
     */
    @BeforeEach
    void clearCaches() {
        if (cacheManager != null) {
            cacheManager.getCacheNames().forEach(cacheName -> {
                Cache cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    cache.clear();
                }
            });
        }
    }
}
