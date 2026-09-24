package com.sleekydz86.jvmperf.monitor.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.sleekydz86.jvmperf.monitor.domain.GcMetric;
import com.sleekydz86.jvmperf.monitor.domain.HeapMetric;
import com.sleekydz86.jvmperf.monitor.domain.JvmSnapshot;
import com.sleekydz86.jvmperf.monitor.domain.MemoryLoadResult;
import com.sleekydz86.jvmperf.monitor.domain.ThreadMetric;

class JvmMonitorServiceTest {

	@Test
	void allocatesAndComparesSnapshots() {
		RecordingMetrics metrics = new RecordingMetrics();
		RecordingLoad load = new RecordingLoad();
		JvmMonitorService service = new JvmMonitorService(metrics, load, 1);

		MemoryLoadResult result = service.allocate();

		assertThat(result.allocatedMegabytes()).isEqualTo(1);
		assertThat(result.retainedChunks()).isEqualTo(1);
		assertThat(result.after().heap().usedBytes()).isGreaterThan(result.before().heap().usedBytes());
	}

	private static final class RecordingMetrics implements com.sleekydz86.jvmperf.monitor.application.port.out.JvmMetricsPort {

		private long used = 10;

		@Override
		public JvmSnapshot capture() {
			JvmSnapshot snapshot = new JvmSnapshot(
				new HeapMetric(used, 100),
				new GcMetric("G1 GC", 1, 10),
				new ThreadMetric(5, 0)
			);
			used += 5;
			return snapshot;
		}
	}

	private static final class RecordingLoad implements com.sleekydz86.jvmperf.monitor.application.port.out.MemoryLoadPort {

		private int chunks;

		@Override
		public int allocateMegabytes(int megabytes) {
			chunks++;
			return megabytes;
		}

		@Override
		public int retainedChunkCount() {
			return chunks;
		}
	}
}
