package com.sleekydz86.catalogflow.domain.event;

import com.sleekydz86.catalogflow.domain.model.ProductId;

import java.time.Instant;
import java.util.UUID;

public record ProductDiscontinued(
		UUID eventId,
		ProductId aggregateId,
		long aggregateVersion,
		Instant occurredAt,
		String correlationId,
		String causationId,
		int schemaVersion,
		String reason) implements DomainEvent {

	public static final String EVENT_TYPE = "ProductDiscontinued";
	public static final int CURRENT_SCHEMA_VERSION = 1;

	public ProductDiscontinued {
		if (eventId == null) {
			eventId = UUID.randomUUID();
		}
		if (correlationId == null) {
			correlationId = "";
		}
		if (causationId == null) {
			causationId = "";
		}
		if (reason == null) {
			reason = "";
		}
	}

	@Override
	public String eventType() {
		return EVENT_TYPE;
	}
}
