package com.sleekydz86.lazylab.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "lazylab.config")
public record LazyConfigProperties(String resourcePath, long loadDelayMillis) {

	public LazyConfigProperties {
		if (resourcePath == null || resourcePath.isBlank()) {
			throw new IllegalArgumentException("설정 리소스 경로가 필요합니다");
		}
		if (loadDelayMillis < 0) {
			throw new IllegalArgumentException("로드 지연 시간은 0 이상이어야 합니다");
		}
	}
}
