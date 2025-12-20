package com.sleekydz86.passykey.adapter.outbound.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class SessionCacheService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String SESSION_PREFIX = "session:";
    private static final String USER_SESSION_PREFIX = "user:session:";
    private static final long SESSION_TTL_HOURS = 24;

    public SessionCacheService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void cacheSession(String sessionId, String username) {
        String key = getKey(sessionId);
        String userSessionKey = getUserSessionKey(username);
        
        String existingSessionId = redisTemplate.opsForValue().get(userSessionKey);
        if (existingSessionId != null && !existingSessionId.equals(sessionId)) {
            String existingKey = getKey(existingSessionId);
            redisTemplate.delete(existingKey);
        }
        
        redisTemplate.opsForValue().set(key, username, SESSION_TTL_HOURS, TimeUnit.HOURS);
        redisTemplate.opsForValue().set(userSessionKey, sessionId, SESSION_TTL_HOURS, TimeUnit.HOURS);
    }

    public String getUsernameFromSession(String sessionId) {
        String key = getKey(sessionId);
        return redisTemplate.opsForValue().get(key);
    }

    public String getActiveSessionId(String username) {
        String userSessionKey = getUserSessionKey(username);
        return redisTemplate.opsForValue().get(userSessionKey);
    }

    public void evictSession(String sessionId) {
        String key = getKey(sessionId);
        String username = redisTemplate.opsForValue().get(key);
        
        if (username != null) {
            String userSessionKey = getUserSessionKey(username);
            redisTemplate.delete(userSessionKey);
        }
        
        redisTemplate.delete(key);
    }

    private String getKey(String sessionId) {
        return SESSION_PREFIX + sessionId;
    }

    private String getUserSessionKey(String username) {
        return USER_SESSION_PREFIX + username;
    }
}





