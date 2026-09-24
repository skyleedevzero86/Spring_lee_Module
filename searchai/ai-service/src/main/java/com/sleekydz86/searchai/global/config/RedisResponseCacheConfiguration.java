package com.sleekydz86.searchai.global.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.beans.factory.annotation.Value;

@Configuration
@ConditionalOnProperty(prefix = "app.cache", name = "mode", havingValue = "redis")
public class RedisResponseCacheConfiguration {

	@Bean
	LettuceConnectionFactory llmCacheRedisConnectionFactory(
		@Value("${spring.data.redis.host:127.0.0.1}") String host,
		@Value("${spring.data.redis.port:9379}") int port,
		@Value("${spring.data.redis.password:}") String password
	) {
		RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
		if (password != null && !password.isBlank()) {
			config.setPassword(RedisPassword.of(password));
		}
		return new LettuceConnectionFactory(config);
	}

	@Bean
	StringRedisTemplate stringRedisTemplate(LettuceConnectionFactory llmCacheRedisConnectionFactory) {
		return new StringRedisTemplate(llmCacheRedisConnectionFactory);
	}
}
