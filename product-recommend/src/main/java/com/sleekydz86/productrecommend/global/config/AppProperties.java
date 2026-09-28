package com.sleekydz86.productrecommend.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
	Embedding embedding,
	Vector vector,
	Chat chat
) {
	public record Embedding(
		String provider,
		String model,
		boolean backfillEnabled,
		long backfillIntervalMs
	) {
	}

	public record Vector(String mode) {
	}

	public record Chat(boolean enabled) {
	}
}
