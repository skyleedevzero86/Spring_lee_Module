package com.sleekydz86.jvmperf.monitor.application.service;

import com.sleekydz86.jvmperf.monitor.application.port.in.AllocateMemoryLoadUseCase;
import com.sleekydz86.jvmperf.monitor.application.port.in.GetJvmSnapshotUseCase;
import com.sleekydz86.jvmperf.monitor.application.port.out.JvmMetricsPort;
import com.sleekydz86.jvmperf.monitor.application.port.out.MemoryLoadPort;
import com.sleekydz86.jvmperf.monitor.domain.JvmSnapshot;
import com.sleekydz86.jvmperf.monitor.domain.MemoryLoadResult;

public final class JvmMonitorService implements GetJvmSnapshotUseCase, AllocateMemoryLoadUseCase {

	private final JvmMetricsPort jvmMetricsPort;
	private final MemoryLoadPort memoryLoadPort;
	private final int chunkMegabytes;

	public JvmMonitorService(
		JvmMetricsPort jvmMetricsPort,
		MemoryLoadPort memoryLoadPort,
		int chunkMegabytes
	) {
		this.jvmMetricsPort = jvmMetricsPort;
		this.memoryLoadPort = memoryLoadPort;
		this.chunkMegabytes = chunkMegabytes;
	}

	@Override
	public JvmSnapshot get() {
		return jvmMetricsPort.capture();
	}

	@Override
	public MemoryLoadResult allocate() {
		JvmSnapshot before = jvmMetricsPort.capture();
		int allocated = memoryLoadPort.allocateMegabytes(chunkMegabytes);
		JvmSnapshot after = jvmMetricsPort.capture();
		return new MemoryLoadResult(before, after, allocated, memoryLoadPort.retainedChunkCount());
	}
}
