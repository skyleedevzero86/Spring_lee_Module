package com.sleekydz86.failurelab.scenario.domain;

public enum TaskOutcome {

	SUCCESS,
	FAILED,
	CANCELLED;

	public String label() {
		return switch (this) {
			case SUCCESS -> "성공";
			case FAILED -> "실패";
			case CANCELLED -> "취소됨";
		};
	}
}
