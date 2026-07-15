package com.sleekydz86.loginstudy.auth.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.LockedException;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers(disabledWithoutDocker = true)
class LoginProtectionServiceTest {

	@Container
	static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7.4-alpine"))
			.withExposedPorts(6379);

	private LoginProtectionService service;
	private StringRedisTemplate redisTemplate;

	@BeforeEach
	void setUp() {
		LettuceConnectionFactory factory = new LettuceConnectionFactory(redis.getHost(), redis.getMappedPort(6379));
		factory.afterPropertiesSet();
		redisTemplate = new StringRedisTemplate(factory);
		redisTemplate.afterPropertiesSet();
		service = new LoginProtectionService(redisTemplate, 3, Duration.ofMinutes(15), Duration.ofMinutes(15));
	}

	@Test
	void locksAccountAfterMaxFailures() {
		service.onFailure("user");
		service.onFailure("user");
		service.onFailure("user");

		assertThat(redisTemplate.hasKey(LoginProtectionService.lockedKey("user"))).isTrue();
		assertThatThrownBy(() -> service.assertNotLocked("user"))
				.isInstanceOf(LockedException.class)
				.hasMessageContaining("계정이 일시적으로 잠겼습니다");
	}

	@Test
	void successClearsFailureCounters() {
		service.onFailure("admin");
		service.onSuccess("admin");

		assertThat(redisTemplate.hasKey(LoginProtectionService.failureKey("admin"))).isFalse();
		assertThat(redisTemplate.hasKey(LoginProtectionService.lockedKey("admin"))).isFalse();
	}

	@Test
	void hashTaggedKeysShareSameUserSlotPrefix() {
		assertThat(LoginProtectionService.failureKey("demo")).isEqualTo("login:{demo}:failure");
		assertThat(LoginProtectionService.lockedKey("demo")).isEqualTo("login:{demo}:locked");
	}
}
