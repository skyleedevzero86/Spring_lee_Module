package com.sleekydz86.catalogflow.domain.event;

import com.sleekydz86.catalogflow.domain.model.ProductId;

import java.time.Instant;
import java.util.UUID;

public interface DomainEvent {

	UUID eventId();

	String eventType();

	ProductId aggregateId();

	long aggregateVersion();

	Instant occurredAt();

	String correlationId();

	String causationId();

	int schemaVersion();
}
