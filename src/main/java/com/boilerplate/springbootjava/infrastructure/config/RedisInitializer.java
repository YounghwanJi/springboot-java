package com.boilerplate.springbootjava.infrastructure.config;


import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

/**
 * Redis 시작 시, Redis에서 저장되어 있던 cache들을 삭제하기 위해 추가.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisInitializer {

    private final CacheManager cacheManager;
    /* 특정 패턴의 캐시만 삭제 예시 - PART A */
//    private final RedisTemplate<String, Object> redisTemplate;

    @PostConstruct
    public void clearAllCaches() {
        cacheManager.getCacheNames()
                .forEach(cacheName -> {
                    Cache cache = cacheManager.getCache(cacheName);
                    if (cache != null) {
                        cache.clear();
                        log.info("RedisInitializer - Cache initialization: {}", cacheName);
                    }
                });

        /* 특정 패턴의 캐시만 삭제 예시 - PART B */
//        Set<String> keys = redisTemplate.keys("users::*");
//        if (keys != null && !keys.isEmpty()) {
//            redisTemplate.delete(keys);
//            log.info("RedisInitializer - Cache initialization: delete - {}.", keys.size());
//        }
    }
}