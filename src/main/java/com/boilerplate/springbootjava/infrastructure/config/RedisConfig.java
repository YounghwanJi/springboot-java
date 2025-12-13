package com.boilerplate.springbootjava.infrastructure.config;

import com.boilerplate.springbootjava.adapter.in.web.v1.user.dto.UserResponseDto;
import com.boilerplate.springbootjava.common.dto.PageResponseDto;
import com.boilerplate.springbootjava.common.dto.PageResponseDtoMixIn;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.List;

@Configuration
@EnableCaching  // 캐시 활성화
public class RedisConfig {

    /**
     * API 응답용 ObjectMapper (기본)
     * - @Primary: 일반 API 호출 시 사용
     * - 타입 정보 없음
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // JavaTimeModule 등록 (Instant 지원)
        mapper.registerModule(new JavaTimeModule());
        // Record 지원을 위한 ParameterNamesModule 추가
        mapper.registerModule(new ParameterNamesModule());
        // ISO-8601 출력
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 알 수 없는 속성 무시
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // API 응답용: 타입 정보 없음 (깔끔한 JSON)
        return mapper;
    }

    /**
     * Redis용 ObjectMapper
     * - JavaTimeModule: Instant, LocalDateTime 등 처리
     * - 타입 정보 포함: LinkedHashMap 문제 해결
     */
    @Bean
    public ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // JavaTimeModule 등록 (Instant 지원)
        mapper.registerModule(new JavaTimeModule());
        // Record 지원을 위한 ParameterNamesModule 추가
        mapper.registerModule(new ParameterNamesModule());
        // ISO 8601 출력
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 알 수 없는 속성 무시
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        /* Custom zone */
        // MixIn으로 PageResponseDto에만 타입 정보 추가
        mapper.addMixIn(PageResponseDto.class, PageResponseDtoMixIn.class);

        // 타입 정보 활성화
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator
                .builder()
                .allowIfBaseType(Object.class)
                .allowIfSubType(List.class)
                .build();

        mapper.activateDefaultTyping(
                ptv,
                ObjectMapper.DefaultTyping.NON_CONCRETE_AND_ARRAYS,
                JsonTypeInfo.As.PROPERTY
        );

        return mapper;
    }

    /**
     * GenericJackson2JsonRedisSerializer를 Bean으로 등록
     */
    @Bean
    public GenericJackson2JsonRedisSerializer jsonRedisSerializer(
            @Qualifier("redisObjectMapper") ObjectMapper redisObjectMapper) {
        return new GenericJackson2JsonRedisSerializer(redisObjectMapper);
    }

    /**
     * RedisTemplate 설정
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory,
                                                       GenericJackson2JsonRedisSerializer jsonRedisSerializer) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Serializer 생성
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        // Key는 String, Value는 JSON
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(jsonRedisSerializer);
        template.setHashValueSerializer(jsonRedisSerializer);

        template.setEnableTransactionSupport(true);
        template.afterPropertiesSet();

        return template;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory,
                                     @Qualifier("redisObjectMapper") ObjectMapper redisObjectMapper,
                                     GenericJackson2JsonRedisSerializer jsonRedisSerializer) {

        /* Custom JSON Serializers 생성 */
        // UserResponseDto
        Jackson2JsonRedisSerializer<UserResponseDto> userResponseSerializer =
                new Jackson2JsonRedisSerializer<>(redisObjectMapper, UserResponseDto.class);

        // 기본 캐시 설정
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration
                .defaultCacheConfig()
                // 기본 TTL 10분
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(jsonRedisSerializer)
                );

        /* Custom 캐시 설정 */
        // "users"
        RedisCacheConfiguration userConfig = RedisCacheConfiguration
                .defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(1))
                .disableCachingNullValues()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(userResponseSerializer)
                );

        // "userList"
        RedisCacheConfiguration userListConfig = RedisCacheConfiguration
                .defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(1))
                .disableCachingNullValues()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(jsonRedisSerializer)
                );

        return RedisCacheManager
                .builder(redisConnectionFactory)
                .cacheDefaults(defaultCacheConfig)
                .withCacheConfiguration("users", userConfig)
                .withCacheConfiguration("userList", userListConfig)
                .transactionAware()
                .build();

    }
}