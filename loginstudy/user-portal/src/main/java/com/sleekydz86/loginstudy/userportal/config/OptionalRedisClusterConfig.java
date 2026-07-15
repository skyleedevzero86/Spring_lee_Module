package com.sleekydz86.loginstudy.userportal.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.util.StringUtils;

@Configuration
@ConditionalOnProperty(name = "loginstudy.redis.cluster-enabled", havingValue = "true", matchIfMissing = true)
public class OptionalRedisClusterConfig {

	private static final List<String> DEFAULT_NODES = List.of(
			"localhost:7001",
			"localhost:7002",
			"localhost:7003",
			"localhost:7004",
			"localhost:7005",
			"localhost:7006");

	@Bean
	RedisClusterConfiguration redisClusterConfiguration(
			@Value("${loginstudy.redis.cluster-nodes:}") String clusterNodes) {
		List<String> nodes = Arrays.stream(clusterNodes.split(","))
				.map(String::trim)
				.filter(StringUtils::hasText)
				.toList();
		if (nodes.isEmpty()) {
			nodes = DEFAULT_NODES;
		}
		return new RedisClusterConfiguration(nodes);
	}
}
