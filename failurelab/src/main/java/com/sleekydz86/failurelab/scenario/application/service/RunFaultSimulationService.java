package com.sleekydz86.failurelab.scenario.application.service;

import com.sleekydz86.failurelab.scenario.application.port.in.RunFaultSimulationUseCase;
import com.sleekydz86.failurelab.scenario.application.port.out.FaultSimulationExecutor;
import com.sleekydz86.failurelab.scenario.domain.SimulationReport;
import com.sleekydz86.failurelab.scenario.domain.SimulationRequest;

public final class RunFaultSimulationService implements RunFaultSimulationUseCase {

	private final FaultSimulationExecutor executor;

	public RunFaultSimulationService(FaultSimulationExecutor executor) {
		this.executor = executor;
	}

	@Override
	public SimulationReport run(SimulationRequest request) {
		return executor.execute(request);
	}
}
