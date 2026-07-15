package com.sleekydz86.loginstudy.userportal.config;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;

@Configuration
public class OptionalRedisClusterConfig {

	@Bean
	@ConditionalOnExpression("T(org.springframework.util.StringUtils).hasText('${loginstudy.redis.cluster-nodes:}')")
	RedisClusterConfiguration redisClusterConfiguration(
			@Value("${loginstudy.redis.cluster-nodes}") String clusterNodes) {
		return new RedisClusterConfiguration(Arrays.stream(clusterNodes.split(","))
				.map(String::trim)
				.filter(node -> !node.isEmpty())
				.toList());
	}
}
