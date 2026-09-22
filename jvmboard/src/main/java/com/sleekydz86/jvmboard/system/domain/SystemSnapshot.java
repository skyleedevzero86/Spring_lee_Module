package com.sleekydz86.jvmboard.system.domain;

import java.time.Duration;
import java.util.List;

public record SystemSnapshot(
	int javaFeatureVersion,
	String jvmName,
	int availableProcessors,
	long heapUsedBytes,
	long heapMaxBytes,
	String garbageCollector,
	Duration uptime
) {

	public SystemSnapshot {
		if (javaFeatureVersion <= 0) {
			throw new IllegalArgumentException("Java 기능 버전은 양수여야 합니다");
		}
		if (jvmName == null || jvmName.isBlank()) {
			throw new IllegalArgumentException("JVM 이름이 필요합니다");
		}
		if (availableProcessors <= 0) {
			throw new IllegalArgumentException("프로세서 수는 양수여야 합니다");
		}
		if (heapUsedBytes < 0) {
			throw new IllegalArgumentException("힙 사용량은 0 이상이어야 합니다");
		}
		if (garbageCollector == null || garbageCollector.isBlank()) {
			throw new IllegalArgumentException("가비지 컬렉터 이름이 필요합니다");
		}
		if (uptime == null || uptime.isNegative()) {
			throw new IllegalArgumentException("가동 시간은 0 이상이어야 합니다");
		}
	}

	public static SystemSnapshot capture(
		int javaFeatureVersion,
		String jvmName,
		int availableProcessors,
		long heapUsedBytes,
		long heapMaxBytes,
		List<String> garbageCollectorNames,
		Duration uptime
	) {
		return new SystemSnapshot(
			javaFeatureVersion,
			jvmName,
			availableProcessors,
			heapUsedBytes,
			heapMaxBytes,
			GarbageCollectorName.from(garbageCollectorNames),
			uptime
		);
	}
}
