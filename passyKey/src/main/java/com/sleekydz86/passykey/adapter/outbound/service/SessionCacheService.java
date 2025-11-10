package com.sleekydz86.passykey.adapter.outbound.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class SessionCacheService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String SESSION_PREFIX = "session:";
    private static final long SESSION_TTL_HOURS = 24;

    public SessionCacheService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void cacheSession(String sessionId, String username) {
        String key = getKey(sessionId);
        redisTemplate.opsForValue().set(key, username, SESSION_TTL_HOURS, TimeUnit.HOURS);
    }

    public String getUsernameFromSession(String sessionId) {
        String key = getKey(sessionId);
        return redisTemplate.opsForValue().get(key);
    }

    public void evictSession(String sessionId) {
        String key = getKey(sessionId);
        redisTemplate.delete(key);
    }

    private String getKey(String sessionId) {
        return SESSION_PREFIX + sessionId;
    }
}



