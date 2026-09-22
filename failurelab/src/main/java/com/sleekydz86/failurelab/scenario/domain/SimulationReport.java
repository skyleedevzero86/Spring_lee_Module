package com.sleekydz86.failurelab.scenario.domain;

import java.util.List;
import java.util.Optional;

public record SimulationReport(
	List<TaskResult> tasks,
	long totalElapsedMillis,
	Optional<String> reason
) {

	public SimulationReport {
		if (tasks == null || tasks.isEmpty()) {
			throw new IllegalArgumentException("작업 결과가 필요합니다");
		}
		if (totalElapsedMillis < 0) {
			throw new IllegalArgumentException("총 처리 시간은 0 이상이어야 합니다");
		}
		if (reason == null) {
			reason = Optional.empty();
		}
		tasks = List.copyOf(tasks);
	}
}
