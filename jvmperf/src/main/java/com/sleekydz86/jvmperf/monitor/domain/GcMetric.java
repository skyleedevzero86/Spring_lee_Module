package com.sleekydz86.jvmperf.monitor.domain;

public record GcMetric(String name, long collectionCount, long collectionTimeMillis) {

	public GcMetric {
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("GC 이름이 필요합니다");
		}
		if (collectionCount < 0 || collectionTimeMillis < 0) {
			throw new IllegalArgumentException("GC 수치는 0 이상이어야 합니다");
		}
	}
}
