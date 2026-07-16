package com.sleekydz86.catalogflow.eventcontract;

import java.time.Instant;
import java.util.UUID;

public record IntegrationEventEnvelope(
		UUID eventId,
		String eventType,
		UUID aggregateId,
		long aggregateVersion,
		Instant occurredAt,
		String correlationId,
		String causationId,
		int schemaVersion,
		String payload) {
}
