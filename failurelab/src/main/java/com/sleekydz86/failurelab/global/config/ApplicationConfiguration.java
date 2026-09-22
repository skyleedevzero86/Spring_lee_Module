package com.sleekydz86.failurelab.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sleekydz86.failurelab.global.concurrency.StructuredFaultSimulationExecutor;
import com.sleekydz86.failurelab.scenario.application.port.in.RunFaultSimulationUseCase;
import com.sleekydz86.failurelab.scenario.application.port.out.FaultSimulationExecutor;
import com.sleekydz86.failurelab.scenario.application.port.out.SimulatedServicePort;
import com.sleekydz86.failurelab.scenario.application.service.RunFaultSimulationService;

@Configuration
public class ApplicationConfiguration {

	@Bean
	FaultSimulationExecutor faultSimulationExecutor(SimulatedServicePort simulatedServicePort) {
		return new StructuredFaultSimulationExecutor(simulatedServicePort);
	}

	@Bean
	RunFaultSimulationUseCase runFaultSimulationUseCase(FaultSimulationExecutor executor) {
		return new RunFaultSimulationService(executor);
	}
}
