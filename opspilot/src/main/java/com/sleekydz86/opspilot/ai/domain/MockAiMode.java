package com.sleekydz86.opspilot.ai.domain;

public enum MockAiMode {
	NORMAL,
	SLOW,
	HTTP_429,
	HTTP_500,
	HTTP_503,
	TIMEOUT,
	MALFORMED_RESPONSE
}
