package com.sleekydz86.catalogflow.domain.model;

import java.util.Objects;
import java.util.UUID;

public record ProductId(UUID value) {

	public ProductId {
		Objects.requireNonNull(value, "productId");
	}

	public static ProductId generate() {
		return new ProductId(UUID.randomUUID());
	}

	public static ProductId of(String value) {
		return new ProductId(UUID.fromString(value));
	}

	@Override
	public String toString() {
		return value.toString();
	}
}
