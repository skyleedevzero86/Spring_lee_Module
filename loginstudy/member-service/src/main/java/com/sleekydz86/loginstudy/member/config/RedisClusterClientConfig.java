package com.sleekydz86.loginstudy.member.config;

import io.lettuce.core.internal.HostAndPort;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DnsResolvers;
import io.lettuce.core.resource.MappingSocketAddressResolver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "loginstudy.redis.nat-remap", havingValue = "true")
public class RedisClusterClientConfig {

	@Bean(destroyMethod = "shutdown")
	ClientResources lettuceClientResources() {
		MappingSocketAddressResolver resolver = MappingSocketAddressResolver.create(
				DnsResolvers.UNRESOLVED,
				hostAndPort -> {
					String host = hostAndPort.getHostText();
					if (host != null && host.startsWith("redis-")) {
						return HostAndPort.of("127.0.0.1", hostAndPort.getPort());
					}
					return hostAndPort;
				});
		return ClientResources.builder().socketAddressResolver(resolver).build();
	}
}
