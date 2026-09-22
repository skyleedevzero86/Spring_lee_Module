package com.sleekydz86.failurelab.scenario.application.port.in;

import com.sleekydz86.failurelab.scenario.domain.SimulationReport;
import com.sleekydz86.failurelab.scenario.domain.SimulationRequest;

public interface RunFaultSimulationUseCase {

	SimulationReport run(SimulationRequest request);
}
