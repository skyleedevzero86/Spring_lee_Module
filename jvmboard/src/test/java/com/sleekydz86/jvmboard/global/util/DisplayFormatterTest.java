package com.sleekydz86.jvmboard.global.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.Test;

class DisplayFormatterTest {

	@Test
	void formatsHeapAndUptimeForTheDashboard() {
		assertThat(MemoryFormatter.megabytes(324L * 1024 * 1024)).isEqualTo("324 MB");
		assertThat(MemoryFormatter.megabytes(4096L * 1024 * 1024)).isEqualTo("4096 MB");
		assertThat(MemoryFormatter.megabytes(-1)).isEqualTo("제한 없음");
		assertThat(UptimeFormatter.format(Duration.ofMinutes(12).plusSeconds(41))).isEqualTo("00:12:41");
		assertThat(ProcessorFormatter.label(8)).isEqualTo("8개 프로세서");
		assertThat(ProcessorFormatter.label(1)).isEqualTo("1개 프로세서");
	}
}
