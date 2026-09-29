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
		public boolean isMemory() {
			return mode == null || mode.isBlank() || "memory".equalsIgnoreCase(mode);
		}

		public boolean isPgVector() {
			return "pgvector".equalsIgnoreCase(mode);
		}

		public boolean isElasticsearch() {
			return "elasticsearch".equalsIgnoreCase(mode) || "es".equalsIgnoreCase(mode);
		}
	}

	public record Chat(boolean enabled) {
	}
}
