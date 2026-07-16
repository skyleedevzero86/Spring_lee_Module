package com.sleekydz86.catalogflow.domain.model;

import com.sleekydz86.catalogflow.domain.exception.InvalidProductKeywordException;

public record ProductKeyword(String value) {

	private static final int MAX_LENGTH = 100;

	public ProductKeyword {
		if (value == null || value.isBlank()) {
			throw new InvalidProductKeywordException("키워드는 비어 있을 수 없습니다");
		}
		String trimmed = value.trim();
		if (trimmed.length() > MAX_LENGTH) {
			throw new InvalidProductKeywordException("키워드는 " + MAX_LENGTH + "자를 초과할 수 없습니다");
		}
		value = trimmed.toLowerCase();
	}
}
