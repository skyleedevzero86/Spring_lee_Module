package com.sleekydz86.payment2v2.global.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheInitializer {

    private final CacheManager cacheManager;

    @EventListener(ApplicationReadyEvent.class)
    @Order(1)
    public void clearCacheOnStartup() {
        try {
            log.info("Redis 캐시 초기화 시작...");

            if (cacheManager != null) {
                cacheManager.getCacheNames().forEach(cacheName -> {
                    var cache = cacheManager.getCache(cacheName);
                    if (cache != null) {
                        cache.clear();
                        log.info("캐시 클리어 완료: {}", cacheName);
                    }
                });
            }

            log.info("Redis 캐시 초기화 완료");
        } catch (Exception e) {
            log.warn("캐시 초기화 중 오류 발생 (무시 가능): {}", e.getMessage());
        }
    }
}

