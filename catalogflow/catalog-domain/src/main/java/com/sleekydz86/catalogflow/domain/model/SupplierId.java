package com.sleekydz86.catalogflow.domain.model;

import java.util.Objects;
import java.util.UUID;

public record SupplierId(UUID value) {

	public SupplierId {
		Objects.requireNonNull(value, "supplierId");
	}

	public static SupplierId of(String value) {
		return new SupplierId(UUID.fromString(value));
	}

	@Override
	public String toString() {
		return value.toString();
	}
}
