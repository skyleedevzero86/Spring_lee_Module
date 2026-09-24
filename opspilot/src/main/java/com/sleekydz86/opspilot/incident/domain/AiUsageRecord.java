package com.sleekydz86.opspilot.incident.domain;

import java.time.Instant;
import java.util.UUID;

public record AiUsageRecord(
	UUID id,
	IncidentId incidentId,
	String model,
	long inputTokens,
	long outputTokens,
	boolean cacheHit,
	Instant createdAt
) {

	public AiUsageRecord {
		if (id == null || incidentId == null || model == null || createdAt == null) {
			throw new IllegalArgumentException("AI 사용량 기록이 불완전합니다");
		}
		if (inputTokens < 0 || outputTokens < 0) {
			throw new IllegalArgumentException("토큰 수는 0 이상이어야 합니다");
		}
	}
}
