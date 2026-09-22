package com.sleekydz86.jvmboard.system.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Test;

class SystemSnapshotTest {

	@Test
	void collapsesG1PoolsIntoOneName() {
		SystemSnapshot snapshot = SystemSnapshot.capture(
			27,
			"OpenJDK 64-Bit Server VM",
			8,
			324L * 1024 * 1024,
			4096L * 1024 * 1024,
			List.of("G1 Young Generation", "G1 Old Generation"),
			Duration.ofMinutes(12).plusSeconds(41)
		);

		assertThat(snapshot.javaFeatureVersion()).isEqualTo(27);
		assertThat(snapshot.garbageCollector()).isEqualTo("G1");
	}

	@Test
	void keepsAnUnrecognisedCollectorName() {
		assertThat(GarbageCollectorName.from(List.of("Epsilon"))).isEqualTo("Epsilon");
	}
}
