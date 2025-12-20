package com.sleekydz86.passykey.adapter.outbound.service;

import com.sleekydz86.passykey.domain.model.WebAuthnCredential;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class CredentialCacheService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private static final String CREDENTIAL_CACHE_PREFIX = "credentials:";
    private static final long CREDENTIAL_CACHE_TTL_MINUTES = 15;

    public CredentialCacheService(RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void cacheUserCredentials(Long userId, List<WebAuthnCredential> credentials) {
        try {
            String key = getKey(userId);
            String value = objectMapper.writeValueAsString(credentials);
            redisTemplate.opsForValue().set(key, value, CREDENTIAL_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("인증서 캐싱 실패", e);
        }
    }

    public List<WebAuthnCredential> getUserCredentialsFromCache(Long userId) {
        try {
            String key = getKey(userId);
            String value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return null;
            }
            return objectMapper.readValue(value,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, WebAuthnCredential.class));
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    public void evictUserCredentials(Long userId) {
        String key = getKey(userId);
        redisTemplate.delete(key);
    }

    private String getKey(Long userId) {
        return CREDENTIAL_CACHE_PREFIX + userId;
    }
}





