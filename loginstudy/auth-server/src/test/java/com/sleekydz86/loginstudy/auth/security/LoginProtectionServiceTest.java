package com.sleekydz86.loginstudy.auth.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
	@DisplayName("최대 실패 횟수 이후 계정을 잠근다")
	void locksAccountAfterMaxFailures() {
		// given
		String username = "user";

		// when
		service.onFailure(username);
		service.onFailure(username);
		service.onFailure(username);

		// then
		assertThat(redisTemplate.hasKey(LoginProtectionService.lockedKey(username))).isTrue();
		assertThatThrownBy(() -> service.assertNotLocked(username))
				.isInstanceOf(LockedException.class)
				.hasMessageContaining("계정이 일시적으로 잠겼습니다");
	}

	@Test
	@DisplayName("로그인 성공 시 실패 카운터를 초기화한다")
	void successClearsFailureCounters() {
		// given
		String username = "admin";
		service.onFailure(username);

		// when
		service.onSuccess(username);

		// then
		assertThat(redisTemplate.hasKey(LoginProtectionService.failureKey(username))).isFalse();
		assertThat(redisTemplate.hasKey(LoginProtectionService.lockedKey(username))).isFalse();
	}

	@Test
	@DisplayName("해시 태그 키는 동일한 사용자 슬롯 접두사를 공유한다")
	void hashTaggedKeysShareSameUserSlotPrefix() {
		// given
		String username = "demo";

		// when / then
		assertThat(LoginProtectionService.failureKey(username)).isEqualTo("login:{demo}:failure");
		assertThat(LoginProtectionService.lockedKey(username)).isEqualTo("login:{demo}:locked");
	}
}
