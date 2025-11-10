package com.sleekydz86.passykey.adapter.outbound.service;

import com.sleekydz86.passykey.domain.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class UserCacheService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private static final String USER_CACHE_PREFIX = "user:";
    private static final long USER_CACHE_TTL_MINUTES = 30;

    public UserCacheService(RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void cacheUser(String username, User user) {
        try {
            String key = getKey(username);
            String value = objectMapper.writeValueAsString(user);
            redisTemplate.opsForValue().set(key, value, USER_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("사용자 캐싱 실패", e);
        }
    }

    public User getUserFromCache(String username) {
        try {
            String key = getKey(username);
            String value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return null;
            }
            return objectMapper.readValue(value, User.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    public void evictUser(String username) {
        String key = getKey(username);
        redisTemplate.delete(key);
    }

    private String getKey(String username) {
        return USER_CACHE_PREFIX + username;
    }
}



