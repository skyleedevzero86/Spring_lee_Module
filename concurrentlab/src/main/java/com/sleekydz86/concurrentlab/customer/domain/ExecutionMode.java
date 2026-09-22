package com.sleekydz86.concurrentlab.customer.domain;

public enum ExecutionMode {

	SEQUENTIAL,
	STRUCTURED;

	public static ExecutionMode from(String raw) {
		if (raw == null || raw.isBlank()) {
			throw new IllegalArgumentException("실행 방식이 필요합니다");
		}
		try {
			return ExecutionMode.valueOf(raw.trim().toUpperCase());
		}
		catch (IllegalArgumentException ex) {
			throw new IllegalArgumentException("지원하지 않는 실행 방식입니다: " + raw);
		}
	}

	public String label() {
		return switch (this) {
			case SEQUENTIAL -> "순차 실행";
			case STRUCTURED -> "구조화 동시성";
		};
	}
}
