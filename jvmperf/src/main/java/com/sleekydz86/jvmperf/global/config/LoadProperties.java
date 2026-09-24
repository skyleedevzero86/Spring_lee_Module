package com.sleekydz86.jvmperf.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jvmperf.load")
public record LoadProperties(int chunkMegabytes) {

	public LoadProperties {
		if (chunkMegabytes <= 0) {
			throw new IllegalArgumentException("부하 청크 크기는 1MB 이상이어야 합니다");
		}
	}
}
