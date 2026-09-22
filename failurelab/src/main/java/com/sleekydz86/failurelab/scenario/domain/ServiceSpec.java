package com.sleekydz86.failurelab.scenario.domain;

public record ServiceSpec(long delayMillis, boolean failure) {

	public ServiceSpec {
		if (delayMillis < 0) {
			throw new IllegalArgumentException("지연 시간은 0 이상이어야 합니다");
		}
	}
}
