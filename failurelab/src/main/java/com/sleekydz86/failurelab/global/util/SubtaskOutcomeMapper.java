package com.sleekydz86.failurelab.global.util;

import java.util.concurrent.StructuredTaskScope;

import com.sleekydz86.failurelab.scenario.domain.ServiceCallResult;
import com.sleekydz86.failurelab.scenario.domain.ServiceKind;
import com.sleekydz86.failurelab.scenario.domain.ServiceSpec;
import com.sleekydz86.failurelab.scenario.domain.TaskOutcome;
import com.sleekydz86.failurelab.scenario.domain.TaskResult;

public final class SubtaskOutcomeMapper {

	private SubtaskOutcomeMapper() {
	}

	public static TaskResult map(
		ServiceKind kind,
		ServiceSpec spec,
		StructuredTaskScope.Subtask<ServiceCallResult> subtask,
		long scopeElapsedMillis
	) {
		return switch (subtask.state()) {
			case SUCCESS -> TaskResult.success(kind, subtask.get().elapsedMillis());
			case FAILED -> TaskResult.failed(kind, spec.delayMillis(), rootFailureName(subtask.exception()));
			case UNAVAILABLE -> TaskResult.cancelled(kind, Math.min(scopeElapsedMillis, spec.delayMillis()));
		};
	}

	public static String rootFailureName(Throwable throwable) {
		Throwable current = throwable;
		while (current.getCause() != null && current.getCause() != current) {
			current = current.getCause();
		}
		return current.getClass().getSimpleName();
	}

	public static boolean isFailure(TaskResult result) {
		return result.outcome() == TaskOutcome.FAILED;
	}
}
