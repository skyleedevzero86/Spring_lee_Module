package com.sleekydz86.failurelab.scenario.adapter.in.web;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sleekydz86.failurelab.scenario.adapter.in.web.SimulationWebModels.RunSimulationRequest;
import com.sleekydz86.failurelab.scenario.adapter.in.web.SimulationWebModels.SimulationReportResponse;
import com.sleekydz86.failurelab.scenario.application.port.in.RunFaultSimulationUseCase;

@RestController
@RequestMapping("/api/simulations")
public class FaultSimulationController {

	private final RunFaultSimulationUseCase runFaultSimulationUseCase;

	public FaultSimulationController(RunFaultSimulationUseCase runFaultSimulationUseCase) {
		this.runFaultSimulationUseCase = runFaultSimulationUseCase;
	}

	@PostMapping("/run")
	public SimulationReportResponse run(@RequestBody RunSimulationRequest request) {
		return SimulationReportResponse.from(runFaultSimulationUseCase.run(request.toDomain()));
	}
}
