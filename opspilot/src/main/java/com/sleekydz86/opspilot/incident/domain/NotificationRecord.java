package com.sleekydz86.opspilot.incident.domain;

import java.time.Instant;
import java.util.UUID;

public record NotificationRecord(
	UUID id,
	IncidentId incidentId,
	String channel,
	String payload,
	Instant sentAt
) {

	public NotificationRecord {
		if (id == null || incidentId == null || channel == null || payload == null || sentAt == null) {
			throw new IllegalArgumentException("알림 기록이 불완전합니다");
		}
	}
}
