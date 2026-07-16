package com.sleekydz86.catalogflow.domain.model;

import com.sleekydz86.catalogflow.domain.exception.InvalidProductNameException;

public record ProductName(String value) {

	private static final int MAX_LENGTH = 200;

	public ProductName {
		if (value == null || value.isBlank()) {
			throw new InvalidProductNameException("상품명은 비어 있을 수 없습니다");
		}
		String trimmed = value.trim();
		if (trimmed.length() > MAX_LENGTH) {
			throw new InvalidProductNameException("상품명은 " + MAX_LENGTH + "자를 초과할 수 없습니다");
		}
		value = trimmed;
	}
}
