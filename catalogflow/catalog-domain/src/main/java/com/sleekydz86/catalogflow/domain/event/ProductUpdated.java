package com.sleekydz86.catalogflow.domain.event;

import com.sleekydz86.catalogflow.domain.model.CategoryId;
import com.sleekydz86.catalogflow.domain.model.ProductDescription;
import com.sleekydz86.catalogflow.domain.model.ProductId;
import com.sleekydz86.catalogflow.domain.model.ProductName;
import com.sleekydz86.catalogflow.domain.model.ProductStatus;
import com.sleekydz86.catalogflow.domain.model.SupplierId;

import java.time.Instant;
import java.util.UUID;

public record ProductUpdated(
		UUID eventId,
		ProductId aggregateId,
		long aggregateVersion,
		Instant occurredAt,
		String correlationId,
		String causationId,
		int schemaVersion,
		ProductName name,
		ProductDescription description,
		CategoryId categoryId,
		SupplierId supplierId,
		ProductStatus status) implements DomainEvent {

	public static final String EVENT_TYPE = "ProductUpdated";
	public static final int CURRENT_SCHEMA_VERSION = 1;

	public ProductUpdated {
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
