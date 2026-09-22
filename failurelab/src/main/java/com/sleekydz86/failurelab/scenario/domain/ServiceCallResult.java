package com.sleekydz86.failurelab.scenario.domain;

public record ServiceCallResult(ServiceKind service, String payload, long elapsedMillis) {

	public ServiceCallResult {
		if (service == null) {
			throw new IllegalArgumentException("서비스 종류가 필요합니다");
		}
		if (payload == null || payload.isBlank()) {
			throw new IllegalArgumentException("응답 내용이 필요합니다");
		}
		if (elapsedMillis < 0) {
			throw new IllegalArgumentException("소요 시간은 0 이상이어야 합니다");
		}
	}
}
