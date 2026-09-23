package com.sleekydz86.pemlab.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pemlab.keygen")
public record KeyGenProperties(String algorithm, int keySize) {

	public KeyGenProperties {
		if (algorithm == null || algorithm.isBlank()) {
			throw new IllegalArgumentException("키 알고리즘이 필요합니다");
		}
		if (keySize <= 0) {
			throw new IllegalArgumentException("키 크기는 1 이상이어야 합니다");
		}
	}
}
