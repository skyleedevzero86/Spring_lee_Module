package com.sleekydz86.jvmperf.monitor.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class HeapMetricTest {

	@Test
	void calculatesUsagePercent() {
		assertThat(new HeapMetric(512L * 1024 * 1024, 1024L * 1024 * 1024).usagePercent()).isEqualTo(50);
	}
}
