package com.sleekydz86.lazylab.modelconfig.domain;

public record ModelConfig(
	String modelName,
	String provider,
	double temperature,
	int maxTokens,
	String endpoint
) {

	public ModelConfig {
		if (modelName == null || modelName.isBlank()) {
			throw new IllegalArgumentException("모델 이름이 필요합니다");
		}
		if (provider == null || provider.isBlank()) {
			throw new IllegalArgumentException("모델 제공자가 필요합니다");
		}
		if (temperature < 0 || temperature > 2) {
			throw new IllegalArgumentException("temperature는 0에서 2 사이여야 합니다");
		}
		if (maxTokens <= 0) {
			throw new IllegalArgumentException("maxTokens는 1 이상이어야 합니다");
		}
		if (endpoint == null || endpoint.isBlank()) {
			throw new IllegalArgumentException("엔드포인트가 필요합니다");
		}
	}
}
