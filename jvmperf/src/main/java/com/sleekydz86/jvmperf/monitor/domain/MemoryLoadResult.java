package com.sleekydz86.jvmperf.monitor.domain;

public record MemoryLoadResult(
	JvmSnapshot before,
	JvmSnapshot after,
	int allocatedMegabytes,
	int retainedChunks
) {

	public MemoryLoadResult {
		if (before == null || after == null) {
			throw new IllegalArgumentException("부하 전후 스냅샷이 필요합니다");
		}
		if (allocatedMegabytes <= 0) {
			throw new IllegalArgumentException("할당 크기는 1MB 이상이어야 합니다");
		}
		if (retainedChunks < 0) {
			throw new IllegalArgumentException("유지 청크 수는 0 이상이어야 합니다");
		}
	}
}
