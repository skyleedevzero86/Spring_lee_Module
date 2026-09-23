package com.sleekydz86.lazylab.modelconfig.domain;

public record ConfigAccessResult(
	InitStatus status,
	boolean firstAccess,
	long loadTimeMillis,
	ModelConfig config
) {

	public ConfigAccessResult {
		if (status == null) {
			throw new IllegalArgumentException("초기화 상태가 필요합니다");
		}
		if (loadTimeMillis < 0) {
			throw new IllegalArgumentException("로드 시간은 0 이상이어야 합니다");
		}
		if (config == null) {
			throw new IllegalArgumentException("모델 설정이 필요합니다");
		}
	}
}
