package com.sleekydz86.jvmperf.monitor.domain;

public record HeapMetric(long usedBytes, long maxBytes) {

	public HeapMetric {
		if (usedBytes < 0) {
			throw new IllegalArgumentException("힙 사용량은 0 이상이어야 합니다");
		}
	}

	public int usagePercent() {
		if (maxBytes <= 0) {
			return 0;
		}
		return (int) Math.min(100, Math.round((usedBytes * 100.0) / maxBytes));
	}
}
