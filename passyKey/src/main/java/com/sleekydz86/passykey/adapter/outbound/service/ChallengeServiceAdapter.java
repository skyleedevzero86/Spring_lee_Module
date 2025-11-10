package com.sleekydz86.passykey.adapter.outbound.service;

import com.sleekydz86.passykey.domain.port.outbound.ChallengeServicePort;
import com.sleekydz86.passykey.global.constants.WebAuthnConstants;
import com.webauthn4j.data.client.challenge.Challenge;
import com.webauthn4j.data.client.challenge.DefaultChallenge;
import com.webauthn4j.util.Base64UrlUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class ChallengeServiceAdapter implements ChallengeServicePort {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String CHALLENGE_PREFIX = "challenge:";

    public ChallengeServiceAdapter(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Challenge generateAndStoreChallenge(String sessionId, String type) {
        Challenge challenge = new DefaultChallenge();
        String key = getKey(sessionId, type);
        String challengeValue = Base64UrlUtil.encodeToString(challenge.getValue());
        redisTemplate.opsForValue().set(key, challengeValue, WebAuthnConstants.CHALLENGE_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        return challenge;
    }

    @Override
    public Challenge getChallenge(String sessionId, String type) {
        String key = getKey(sessionId, type);
        String challengeValue = redisTemplate.opsForValue().get(key);
        if (challengeValue == null) {
            return null;
        }
        byte[] challengeBytes = Base64UrlUtil.decode(challengeValue);
        return new DefaultChallenge(challengeBytes);
    }

    @Override
    public void removeChallenge(String sessionId, String type) {
        String key = getKey(sessionId, type);
        redisTemplate.delete(key);
    }

    private String getKey(String sessionId, String type) {
        return CHALLENGE_PREFIX + sessionId + ":" + type;
    }
}
