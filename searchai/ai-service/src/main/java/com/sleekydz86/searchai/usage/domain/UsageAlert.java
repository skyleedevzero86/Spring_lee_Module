package com.sleekydz86.searchai.usage.domain;

import java.time.Instant;

public record UsageAlert(
	String code,
	String message,
	String severity,
	String username,
	Instant createdAt
) {
}
