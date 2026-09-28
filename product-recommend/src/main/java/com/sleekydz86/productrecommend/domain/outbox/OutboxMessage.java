package com.sleekydz86.productrecommend.domain.outbox;

import java.time.Instant;
import java.util.UUID;

public record OutboxMessage(
	String id,
	String aggregateType,
	String aggregateId,
	String eventType,
	String payload,
	OutboxStatus status,
	Instant createdAt,
	Instant publishedAt
) {
	public static OutboxMessage pending(String aggregateType, String aggregateId, String eventType, String payload) {
		return new OutboxMessage(
			UUID.randomUUID().toString(),
			aggregateType,
			aggregateId,
			eventType,
			payload,
			OutboxStatus.PENDING,
			Instant.now(),
			null
		);
	}

	public OutboxMessage markPublished() {
		return new OutboxMessage(id, aggregateType, aggregateId, eventType, payload, OutboxStatus.PUBLISHED, createdAt, Instant.now());
	}

	public OutboxMessage markFailed() {
		return new OutboxMessage(id, aggregateType, aggregateId, eventType, payload, OutboxStatus.FAILED, createdAt, publishedAt);
	}
}
