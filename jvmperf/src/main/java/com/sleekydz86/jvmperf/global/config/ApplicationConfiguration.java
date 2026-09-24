package com.sleekydz86.jvmperf.global.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sleekydz86.jvmperf.monitor.application.port.in.AllocateMemoryLoadUseCase;
import com.sleekydz86.jvmperf.monitor.application.port.in.GetJvmSnapshotUseCase;
import com.sleekydz86.jvmperf.monitor.application.port.out.JvmMetricsPort;
import com.sleekydz86.jvmperf.monitor.application.port.out.MemoryLoadPort;
import com.sleekydz86.jvmperf.monitor.application.service.JvmMonitorService;

@Configuration
@EnableConfigurationProperties(LoadProperties.class)
public class ApplicationConfiguration {

	@Bean
	JvmMonitorService jvmMonitorService(
		JvmMetricsPort jvmMetricsPort,
		MemoryLoadPort memoryLoadPort,
		LoadProperties loadProperties
	) {
		return new JvmMonitorService(jvmMetricsPort, memoryLoadPort, loadProperties.chunkMegabytes());
	}

	@Bean
	GetJvmSnapshotUseCase getJvmSnapshotUseCase(JvmMonitorService service) {
		return service;
	}

	@Bean
	AllocateMemoryLoadUseCase allocateMemoryLoadUseCase(JvmMonitorService service) {
		return service;
	}
}
