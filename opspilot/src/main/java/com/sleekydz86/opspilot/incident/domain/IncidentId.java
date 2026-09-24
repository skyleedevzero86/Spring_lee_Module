package com.sleekydz86.opspilot.incident.domain;

import java.util.UUID;

public record IncidentId(UUID value) {

	public IncidentId {
		if (value == null) {
			throw new IllegalArgumentException("장애 ID가 필요합니다");
		}
	}

	public static IncidentId newId() {
		return new IncidentId(UUID.randomUUID());
	}

	public static IncidentId of(UUID value) {
		return new IncidentId(value);
	}

	@Override
	public String toString() {
		return value.toString();
	}
}
