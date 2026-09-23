package com.sleekydz86.lazylab.modelconfig.domain;

public record ConfigStatus(InitStatus status) {

	public ConfigStatus {
		if (status == null) {
			throw new IllegalArgumentException("초기화 상태가 필요합니다");
		}
	}
}
