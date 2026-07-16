package com.sleekydz86.catalogflow.domain.event;

import com.sleekydz86.catalogflow.domain.model.Money;
import com.sleekydz86.catalogflow.domain.model.ProductId;

import java.time.Instant;
import java.util.UUID;

public record ProductPriceChanged(
		UUID eventId,
		ProductId aggregateId,
		long aggregateVersion,
		Instant occurredAt,
		String correlationId,
		String causationId,
		int schemaVersion,
		Money previousPrice,
		Money newPrice) implements DomainEvent {

	public static final String EVENT_TYPE = "ProductPriceChanged";
	public static final int CURRENT_SCHEMA_VERSION = 1;

	public ProductPriceChanged {
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
