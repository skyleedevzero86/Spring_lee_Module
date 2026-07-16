package com.sleekydz86.catalogflow.domain.event;

import com.sleekydz86.catalogflow.domain.model.ProductId;

import java.time.Instant;
import java.util.UUID;

public record ProductEnrichmentRequested(
		UUID eventId,
		ProductId aggregateId,
		long aggregateVersion,
		Instant occurredAt,
		String correlationId,
		String causationId,
		int schemaVersion) implements DomainEvent {

	public static final String EVENT_TYPE = "ProductEnrichmentRequested";
	public static final int CURRENT_SCHEMA_VERSION = 1;

	public ProductEnrichmentRequested {
		if (eventId == null) {
			eventId = UUID.randomUUID();
		}
		if (correlationId == null) {
			correlationId = "";
		}
		if (causationId == null) {
			causationId = "";
		}
	}

	@Override
	public String eventType() {
		return EVENT_TYPE;
	}
}
