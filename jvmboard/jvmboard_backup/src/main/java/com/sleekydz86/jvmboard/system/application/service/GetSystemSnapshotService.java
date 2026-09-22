package com.sleekydz86.jvmboard.system.application.service;

import com.sleekydz86.jvmboard.system.application.port.in.GetSystemSnapshotUseCase;
import com.sleekydz86.jvmboard.system.application.port.out.SystemProbe;
import com.sleekydz86.jvmboard.system.application.port.out.SystemReading;
import com.sleekydz86.jvmboard.system.domain.SystemSnapshot;

public final class GetSystemSnapshotService implements GetSystemSnapshotUseCase {

	private final SystemProbe probe;

	public GetSystemSnapshotService(SystemProbe probe) {
		this.probe = probe;
	}

	@Override
	public SystemSnapshot get() {
		SystemReading reading = probe.read();
		return SystemSnapshot.capture(
			reading.javaFeatureVersion(),
			reading.jvmName(),
			reading.availableProcessors(),
			reading.heapUsedBytes(),
			reading.heapMaxBytes(),
			reading.garbageCollectorNames(),
			reading.uptime()
		);
	}
}
