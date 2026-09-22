package com.sleekydz86.failurelab.scenario.adapter.in.web;

import java.util.List;

import com.sleekydz86.failurelab.scenario.domain.ServiceSpec;
import com.sleekydz86.failurelab.scenario.domain.SimulationReport;
import com.sleekydz86.failurelab.scenario.domain.SimulationRequest;
import com.sleekydz86.failurelab.scenario.domain.TaskResult;

public final class SimulationWebModels {

	private SimulationWebModels() {
	}

	public record ServiceSpecRequest(long delayMillis, boolean failure) {

		ServiceSpec toDomain() {
			return new ServiceSpec(delayMillis, failure);
		}
	}

	public record RunSimulationRequest(
		ServiceSpecRequest profile,
		ServiceSpecRequest orders,
		ServiceSpecRequest recommendation,
		long timeoutMillis
	) {

		public SimulationRequest toDomain() {
			if (profile == null || orders == null || recommendation == null) {
				throw new IllegalArgumentException("서비스 설정이 필요합니다");
			}
			return new SimulationRequest(
				profile.toDomain(),
				orders.toDomain(),
				recommendation.toDomain(),
				timeoutMillis
			);
		}
	}

	public record TaskResultView(
		String service,
		String displayName,
		String outcome,
		String outcomeLabel,
		long elapsedMillis,
		String failureReason
	) {

		static TaskResultView from(TaskResult result) {
			return new TaskResultView(
				result.service().name(),
				result.service().displayName(),
				result.outcome().name(),
				result.outcome().label(),
				result.elapsedMillis(),
				result.failureReason().orElse(null)
			);
		}
	}

	public record SimulationReportResponse(
		List<TaskResultView> tasks,
		long totalElapsedMillis,
		String reason
	) {

		public static SimulationReportResponse from(SimulationReport report) {
			return new SimulationReportResponse(
				report.tasks().stream().map(TaskResultView::from).toList(),
				report.totalElapsedMillis(),
				report.reason().orElse(null)
			);
		}
	}
}
