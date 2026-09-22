package com.sleekydz86.failurelab.scenario.domain;

import java.util.Optional;

public record TaskResult(
	ServiceKind service,
	TaskOutcome outcome,
	long elapsedMillis,
	Optional<String> failureReason
) {

	public TaskResult {
		if (service == null) {
			throw new IllegalArgumentException("서비스 종류가 필요합니다");
		}
		if (outcome == null) {
			throw new IllegalArgumentException("작업 결과가 필요합니다");
		}
		if (elapsedMillis < 0) {
			throw new IllegalArgumentException("소요 시간은 0 이상이어야 합니다");
		}
		if (failureReason == null) {
			failureReason = Optional.empty();
		}
	}

	public static TaskResult success(ServiceKind service, long elapsedMillis) {
		return new TaskResult(service, TaskOutcome.SUCCESS, elapsedMillis, Optional.empty());
	}

	public static TaskResult failed(ServiceKind service, long elapsedMillis, String reason) {
		return new TaskResult(service, TaskOutcome.FAILED, elapsedMillis, Optional.ofNullable(reason));
	}

	public static TaskResult cancelled(ServiceKind service, long elapsedMillis) {
		return new TaskResult(service, TaskOutcome.CANCELLED, elapsedMillis, Optional.empty());
	}
}
