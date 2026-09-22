package com.sleekydz86.failurelab.scenario.domain;

public record SimulationRequest(
	ServiceSpec profile,
	ServiceSpec orders,
	ServiceSpec recommendation,
	long timeoutMillis
) {

	public SimulationRequest {
		if (profile == null || orders == null || recommendation == null) {
			throw new IllegalArgumentException("서비스 설정이 필요합니다");
		}
		if (timeoutMillis <= 0) {
			throw new IllegalArgumentException("타임아웃은 1ms 이상이어야 합니다");
		}
	}

	public ServiceSpec specOf(ServiceKind kind) {
		return switch (kind) {
			case PROFILE -> profile;
			case ORDERS -> orders;
			case RECOMMENDATION -> recommendation;
		};
	}
}
