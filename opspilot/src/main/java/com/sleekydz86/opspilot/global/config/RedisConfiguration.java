package com.sleekydz86.opspilot.global.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.beans.factory.annotation.Value;

@Configuration
@ConditionalOnProperty(prefix = "opspilot.cache", name = "mode", havingValue = "redis")
public class RedisConfiguration {

	@Bean
	LettuceConnectionFactory redisConnectionFactory(
		@Value("${spring.data.redis.host:localhost}") String host,
		@Value("${spring.data.redis.port:6379}") int port
	) {
		RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(host, port);
		return new LettuceConnectionFactory(configuration);
	}

	@Bean
	StringRedisTemplate stringRedisTemplate(LettuceConnectionFactory redisConnectionFactory) {
		return new StringRedisTemplate(redisConnectionFactory);
	}
}
