package com.sleekydz86.catalogflow.domain.event;

import com.sleekydz86.catalogflow.domain.model.ProductId;

import java.time.Instant;
import java.util.UUID;

public record ProductEnrichmentCompleted(
		UUID eventId,
		ProductId aggregateId,
		long aggregateVersion,
		Instant occurredAt,
		String correlationId,
		String causationId,
		int schemaVersion,
		String modelName) implements DomainEvent {

	public static final String EVENT_TYPE = "ProductEnrichmentCompleted";
	public static final int CURRENT_SCHEMA_VERSION = 1;

	public ProductEnrichmentCompleted {
		if (eventId == null) {
			eventId = UUID.randomUUID();
		}
		if (correlationId == null) {
			correlationId = "";
		}
		if (causationId == null) {
			causationId = "";
		}
		if (modelName == null) {
			modelName = "";
		}
	}

	@Override
	public String eventType() {
		return EVENT_TYPE;
	}
}
