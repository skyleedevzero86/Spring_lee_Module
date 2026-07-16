package com.sleekydz86.catalogflow.domain.model;

import com.sleekydz86.catalogflow.domain.exception.InvalidProductDescriptionException;

public record ProductDescription(String value) {

	private static final int MAX_LENGTH = 10_000;

	public ProductDescription {
		if (value == null) {
			value = "";
		}
		String trimmed = value.trim();
		if (trimmed.length() > MAX_LENGTH) {
			throw new InvalidProductDescriptionException(
					"상품 설명은 " + MAX_LENGTH + "자를 초과할 수 없습니다");
		}
		value = trimmed;
	}

	public static ProductDescription empty() {
		return new ProductDescription("");
	}
}
