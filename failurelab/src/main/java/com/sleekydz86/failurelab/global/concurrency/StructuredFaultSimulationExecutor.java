package com.sleekydz86.failurelab.global.concurrency;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.StructuredTaskScope;

import com.sleekydz86.failurelab.global.util.Stopwatch;
import com.sleekydz86.failurelab.global.util.SubtaskOutcomeMapper;
import com.sleekydz86.failurelab.scenario.application.port.out.FaultSimulationExecutor;
import com.sleekydz86.failurelab.scenario.application.port.out.SimulatedServicePort;
import com.sleekydz86.failurelab.scenario.domain.ServiceCallResult;
import com.sleekydz86.failurelab.scenario.domain.ServiceKind;
import com.sleekydz86.failurelab.scenario.domain.SimulationReport;
import com.sleekydz86.failurelab.scenario.domain.SimulationRequest;
import com.sleekydz86.failurelab.scenario.domain.TaskResult;

public final class StructuredFaultSimulationExecutor implements FaultSimulationExecutor {

	private final SimulatedServicePort simulatedServicePort;

	public StructuredFaultSimulationExecutor(SimulatedServicePort simulatedServicePort) {
		this.simulatedServicePort = simulatedServicePort;
	}

	@Override
	public SimulationReport execute(SimulationRequest request) {
		Stopwatch total = Stopwatch.start();
		try (var scope = StructuredTaskScope.open(
			StructuredTaskScope.Joiner.<ServiceCallResult>allSuccessfulOrThrow(),
			config -> config.withTimeout(Duration.ofMillis(request.timeoutMillis()))
		)) {
			var profileTask = scope.fork(() -> invoke(ServiceKind.PROFILE, request));
			var ordersTask = scope.fork(() -> invoke(ServiceKind.ORDERS, request));
			var recommendationTask = scope.fork(() -> invoke(ServiceKind.RECOMMENDATION, request));

			Throwable joinFailure = null;
			try {
				scope.join();
			}
			catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
				joinFailure = ex;
			}
			catch (Exception ex) {
				joinFailure = ex;
			}

			long elapsed = total.elapsedMillis();
			List<TaskResult> tasks = List.of(
				SubtaskOutcomeMapper.map(ServiceKind.PROFILE, request.profile(), profileTask, elapsed),
				SubtaskOutcomeMapper.map(ServiceKind.ORDERS, request.orders(), ordersTask, elapsed),
				SubtaskOutcomeMapper.map(
					ServiceKind.RECOMMENDATION,
					request.recommendation(),
					recommendationTask,
					elapsed
				)
			);

			Optional<String> reason = tasks.stream()
				.filter(SubtaskOutcomeMapper::isFailure)
				.flatMap(task -> task.failureReason().stream())
				.findFirst()
				.or(() -> Optional.ofNullable(joinFailure).map(SubtaskOutcomeMapper::rootFailureName));

			return new SimulationReport(tasks, elapsed, reason);
		}
	}

	private ServiceCallResult invoke(ServiceKind kind, SimulationRequest request) {
		return simulatedServicePort.invoke(kind, request.specOf(kind));
	}
}
