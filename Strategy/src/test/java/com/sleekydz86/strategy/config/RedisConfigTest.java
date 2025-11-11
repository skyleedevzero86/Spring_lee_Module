package com.sleekydz86.strategy.config;

import com.sleekydz86.strategy.global.config.RedisConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RedisConfigTest {

    @Test
    @DisplayName("Redis 설정 클래스 존재 여부 확인")
    void testRedisConfigExists() {
        // given & when
        RedisConfig config = new RedisConfig();
        
        // then
        assertNotNull(config);
    }

    @Test
    @DisplayName("Redis 설정 클래스에 @Configuration 어노테이션 존재 확인")
    void testRedisConfigAnnotation() {
        // given & when & then
        assertTrue(RedisConfig.class.isAnnotationPresent(org.springframework.context.annotation.Configuration.class));
    }

    @Test
    @DisplayName("Redis 설정 클래스에 @EnableCaching 어노테이션 존재 확인")
    void testRedisConfigEnableCaching() {
        // given & when & then
        assertTrue(RedisConfig.class.isAnnotationPresent(org.springframework.cache.annotation.EnableCaching.class));
    }
}

