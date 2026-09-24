package com.sleekydz86.jvmperf.monitor.domain;

public record ThreadMetric(int platformThreads, long virtualThreads) {

	public ThreadMetric {
		if (platformThreads < 0) {
			throw new IllegalArgumentException("플랫폼 스레드 수는 0 이상이어야 합니다");
		}
		if (virtualThreads < 0) {
			throw new IllegalArgumentException("가상 스레드 수는 0 이상이어야 합니다");
		}
	}
}
