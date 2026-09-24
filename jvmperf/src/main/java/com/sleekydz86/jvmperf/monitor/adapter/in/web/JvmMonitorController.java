package com.sleekydz86.jvmperf.monitor.adapter.in.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sleekydz86.jvmperf.monitor.adapter.in.web.MonitorWebModels.MemoryLoadResponse;
import com.sleekydz86.jvmperf.monitor.adapter.in.web.MonitorWebModels.SnapshotResponse;
import com.sleekydz86.jvmperf.monitor.application.port.in.AllocateMemoryLoadUseCase;
import com.sleekydz86.jvmperf.monitor.application.port.in.GetJvmSnapshotUseCase;

@RestController
@RequestMapping("/api/jvm")
public class JvmMonitorController {

	private final GetJvmSnapshotUseCase getJvmSnapshotUseCase;
	private final AllocateMemoryLoadUseCase allocateMemoryLoadUseCase;

	public JvmMonitorController(
		GetJvmSnapshotUseCase getJvmSnapshotUseCase,
		AllocateMemoryLoadUseCase allocateMemoryLoadUseCase
	) {
		this.getJvmSnapshotUseCase = getJvmSnapshotUseCase;
		this.allocateMemoryLoadUseCase = allocateMemoryLoadUseCase;
	}

	@GetMapping("/snapshot")
	public SnapshotResponse snapshot() {
		return SnapshotResponse.from(getJvmSnapshotUseCase.get());
	}

	@PostMapping("/load")
	public MemoryLoadResponse load() {
		return MemoryLoadResponse.from(allocateMemoryLoadUseCase.allocate());
	}
}
