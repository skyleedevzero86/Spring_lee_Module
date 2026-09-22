package com.sleekydz86.failurelab.global.concurrency;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.sleekydz86.failurelab.scenario.adapter.out.simulated.ConfigurableSimulatedServiceAdapter;
import com.sleekydz86.failurelab.scenario.domain.ServiceSpec;
import com.sleekydz86.failurelab.scenario.domain.SimulationReport;
import com.sleekydz86.failurelab.scenario.domain.SimulationRequest;
import com.sleekydz86.failurelab.scenario.domain.TaskOutcome;

class StructuredFaultSimulationExecutorTest {

	private final StructuredFaultSimulationExecutor executor =
		new StructuredFaultSimulationExecutor(new ConfigurableSimulatedServiceAdapter());

	@Test
	void cancelsSlowTaskWhenSiblingFails() {
		SimulationReport report = executor.execute(new SimulationRequest(
			new ServiceSpec(80, false),
			new ServiceSpec(100, true),
			new ServiceSpec(400, false),
			300
		));

		assertThat(report.tasks()).hasSize(3);
		assertThat(find(report, "PROFILE").outcome()).isEqualTo(TaskOutcome.SUCCESS);
		assertThat(find(report, "ORDERS").outcome()).isEqualTo(TaskOutcome.FAILED);
		assertThat(find(report, "ORDERS").failureReason()).contains("OrderServiceException");
		assertThat(find(report, "RECOMMENDATION").outcome()).isEqualTo(TaskOutcome.CANCELLED);
		assertThat(report.reason()).contains("OrderServiceException");
		assertThat(report.totalElapsedMillis()).isLessThan(350);
	}

	@Test
	void cancelsAllUnfinishedTasksOnTimeout() {
		SimulationReport report = executor.execute(new SimulationRequest(
			new ServiceSpec(50, false),
			new ServiceSpec(50, false),
			new ServiceSpec(400, false),
			120
		));

		assertThat(find(report, "PROFILE").outcome()).isEqualTo(TaskOutcome.SUCCESS);
		assertThat(find(report, "ORDERS").outcome()).isEqualTo(TaskOutcome.SUCCESS);
		assertThat(find(report, "RECOMMENDATION").outcome()).isEqualTo(TaskOutcome.CANCELLED);
		assertThat(report.totalElapsedMillis()).isLessThan(250);
	}

	private static com.sleekydz86.failurelab.scenario.domain.TaskResult find(
		SimulationReport report,
		String service
	) {
		return report.tasks().stream()
			.filter(task -> task.service().name().equals(service))
			.findFirst()
			.orElseThrow();
	}
}
