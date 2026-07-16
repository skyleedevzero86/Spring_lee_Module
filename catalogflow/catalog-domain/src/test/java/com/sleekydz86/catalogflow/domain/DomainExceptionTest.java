package com.sleekydz86.catalogflow.domain;

import com.sleekydz86.catalogflow.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DomainExceptionTest {

	@Test
	void shouldCarryMessage() {
		DomainException exception = assertThrows(DomainException.class, () -> {
			throw new DomainException("invalid product") {
			};
		});

		assertEquals("invalid product", exception.getMessage());
	}
}
