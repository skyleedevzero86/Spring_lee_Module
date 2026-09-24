package com.sleekydz86.searchai.chat.domain;

public enum MockAiMode {
	NORMAL,
	SLOW,
	HTTP_429,
	HTTP_500,
	HTTP_503,
	TIMEOUT,
	MALFORMED_RESPONSE;

	public static MockAiMode from(String value) {
		if (value == null || value.isBlank()) {
			return NORMAL;
		}
		try {
			return MockAiMode.valueOf(value.trim().toUpperCase());
		} catch (Exception ex) {
			return NORMAL;
		}
	}
}
