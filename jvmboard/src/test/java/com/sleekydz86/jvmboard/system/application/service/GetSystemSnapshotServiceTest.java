package com.sleekydz86.jvmboard.system.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sleekydz86.jvmboard.system.application.port.out.SystemProbe;
import com.sleekydz86.jvmboard.system.application.port.out.SystemReading;
import com.sleekydz86.jvmboard.system.domain.SystemSnapshot;

class GetSystemSnapshotServiceTest {

	@Test
	void buildsASnapshotFromTheProbe() {
		SystemProbe probe = () -> new SystemReading(
			27,
			"OpenJDK 64-Bit Server VM",
			8,
			100,
			200,
			List.of("G1 Young Generation", "G1 Old Generation"),
			Duration.ofSeconds(761)
		);

		SystemSnapshot snapshot = new GetSystemSnapshotService(probe).get();

		assertThat(snapshot.jvmName()).isEqualTo("OpenJDK 64-Bit Server VM");
		assertThat(snapshot.garbageCollector()).isEqualTo("G1");
		assertThat(snapshot.uptime()).isEqualTo(Duration.ofSeconds(761));
	}
}
