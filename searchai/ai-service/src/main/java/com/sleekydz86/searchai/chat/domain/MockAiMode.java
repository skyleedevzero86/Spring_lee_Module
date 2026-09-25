package com.sleekydz86.searchai.chat.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum MockAiMode {
	NORMAL,
	SLOW,
	HTTP_429,
	HTTP_500,
	HTTP_503,
	TIMEOUT,
	MALFORMED_RESPONSE;

	private static final Logger log = LoggerFactory.getLogger(MockAiMode.class);

	public static MockAiMode from(String value) {
		if (value == null || value.isBlank()) {
			return NORMAL;
		}
		try {
			return MockAiMode.valueOf(value.trim().toUpperCase());
		} catch (Exception ex) {
			log.warn("알 수 없는 Mock AI 모드 '{}', NORMAL로 대체합니다", value);
			return NORMAL;
		}
	}
}
