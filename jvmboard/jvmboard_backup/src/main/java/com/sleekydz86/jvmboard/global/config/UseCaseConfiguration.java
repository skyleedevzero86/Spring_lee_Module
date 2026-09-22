package com.sleekydz86.jvmboard.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sleekydz86.jvmboard.system.application.port.in.GetSystemSnapshotUseCase;
import com.sleekydz86.jvmboard.system.application.port.out.SystemProbe;
import com.sleekydz86.jvmboard.system.application.service.GetSystemSnapshotService;

@Configuration
public class UseCaseConfiguration {

	@Bean
	GetSystemSnapshotUseCase getSystemSnapshotUseCase(SystemProbe probe) {
		return new GetSystemSnapshotService(probe);
	}
}
