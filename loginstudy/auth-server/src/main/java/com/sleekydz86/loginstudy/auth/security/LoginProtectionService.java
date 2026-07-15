package com.sleekydz86.loginstudy.auth.security;

import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.LockedException;
import org.springframework.stereotype.Service;

@Service
public class LoginProtectionService {

	private static final Logger log = LoggerFactory.getLogger(LoginProtectionService.class);

	private final StringRedisTemplate redisTemplate;
	private final int maxFailures;
	private final Duration failureWindow;
	private final Duration lockDuration;

	public LoginProtectionService(
			StringRedisTemplate redisTemplate,
			@Value("${auth.login.max-failures:5}") int maxFailures,
			@Value("${auth.login.failure-window:15m}") Duration failureWindow,
			@Value("${auth.login.lock-duration:15m}") Duration lockDuration) {
		this.redisTemplate = redisTemplate;
		this.maxFailures = maxFailures;
		this.failureWindow = failureWindow;
		this.lockDuration = lockDuration;
	}

	public void assertNotLocked(String username) {
		Boolean locked = redisTemplate.hasKey(lockedKey(username));
		if (Boolean.TRUE.equals(locked)) {
			throw new LockedException("계정이 일시적으로 잠겼습니다. 잠시 후 다시 시도하세요: " + username);
		}
	}

	public void onFailure(String username) {
		if (username == null || username.isBlank() || "unknown".equals(username)) {
			return;
		}
		String failuresKey = failureKey(username);
		Long failures = redisTemplate.opsForValue().increment(failuresKey);
		if (failures != null && failures == 1L) {
			redisTemplate.expire(failuresKey, failureWindow);
		}
		if (failures != null && failures >= maxFailures) {
			redisTemplate.opsForValue().set(lockedKey(username), "1", lockDuration);
			redisTemplate.delete(failuresKey);
			log.warn("로그인 실패 임계값을 초과해 계정을 잠갔습니다. username={}, failures={}", username, failures);
		}
	}

	public void onSuccess(String username) {
		if (username == null || username.isBlank()) {
			return;
		}
		redisTemplate.delete(failureKey(username));
		redisTemplate.delete(lockedKey(username));
	}

	public static String failureKey(String username) {
		return "login:{" + username + "}:failure";
	}

	public static String lockedKey(String username) {
		return "login:{" + username + "}:locked";
	}
}
