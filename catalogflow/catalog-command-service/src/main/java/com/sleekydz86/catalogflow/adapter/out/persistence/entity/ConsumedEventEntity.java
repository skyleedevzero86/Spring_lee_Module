package com.sleekydz86.catalogflow.adapter.out.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "consumed_events")
@IdClass(ConsumedEventEntityId.class)
public class ConsumedEventEntity {

	@Id
	@Column(name = "event_id", nullable = false)
	private UUID eventId;

	@Id
	@Column(name = "consumer_name", nullable = false, length = 100)
	private String consumerName;

	@Column(name = "consumed_at", nullable = false)
	private Instant consumedAt;

	public static ConsumedEventEntity create(UUID eventId, String consumerName, Instant consumedAt) {
		ConsumedEventEntity entity = new ConsumedEventEntity();
		entity.eventId = eventId;
		entity.consumerName = consumerName;
		entity.consumedAt = consumedAt;
		return entity;
	}

	public UUID getEventId() {
		return eventId;
	}

	public String getConsumerName() {
		return consumerName;
	}

	public Instant getConsumedAt() {
		return consumedAt;
	}
}
