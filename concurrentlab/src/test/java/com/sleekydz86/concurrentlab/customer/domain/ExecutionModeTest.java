package com.sleekydz86.concurrentlab.customer.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ExecutionModeTest {

	@Test
	void parsesKnownModes() {
		assertThat(ExecutionMode.from("sequential")).isEqualTo(ExecutionMode.SEQUENTIAL);
		assertThat(ExecutionMode.from("STRUCTURED").label()).isEqualTo("구조화 동시성");
	}

	@Test
	void rejectsUnknownMode() {
		assertThatThrownBy(() -> ExecutionMode.from("parallel"))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("지원하지 않는 실행 방식");
	}
}
