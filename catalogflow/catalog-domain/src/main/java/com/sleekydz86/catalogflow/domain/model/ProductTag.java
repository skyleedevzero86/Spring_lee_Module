package com.sleekydz86.catalogflow.domain.model;

import com.sleekydz86.catalogflow.domain.exception.InvalidProductTagException;

public record ProductTag(String value) {

	private static final int MAX_LENGTH = 50;

	public ProductTag {
		if (value == null || value.isBlank()) {
			throw new InvalidProductTagException("태그는 비어 있을 수 없습니다");
		}
		String trimmed = value.trim();
		if (trimmed.length() > MAX_LENGTH) {
			throw new InvalidProductTagException("태그는 " + MAX_LENGTH + "자를 초과할 수 없습니다");
		}
		value = trimmed.toLowerCase();
	}
}
