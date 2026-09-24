package com.sleekydz86.jvmperf.monitor.domain;

public record JvmSnapshot(HeapMetric heap, GcMetric gc, ThreadMetric threads) {

	public JvmSnapshot {
		if (heap == null || gc == null || threads == null) {
			throw new IllegalArgumentException("JVM 스냅샷 구성 값이 필요합니다");
		}
	}
}
