package com.sleekydz86.catalogflow.domain.model;

import java.util.Objects;
import java.util.UUID;

public record CategoryId(UUID value) {

	public CategoryId {
		Objects.requireNonNull(value, "categoryId");
	}

	public static CategoryId of(String value) {
		return new CategoryId(UUID.fromString(value));
	}

	@Override
	public String toString() {
		return value.toString();
	}
}
