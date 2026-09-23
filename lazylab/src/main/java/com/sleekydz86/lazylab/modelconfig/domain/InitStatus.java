package com.sleekydz86.lazylab.modelconfig.domain;

public enum InitStatus {

	NOT_INITIALIZED,
	INITIALIZED;

	public String label() {
		return switch (this) {
			case NOT_INITIALIZED -> "초기화되지 않음";
			case INITIALIZED -> "초기화 완료";
		};
	}
}
