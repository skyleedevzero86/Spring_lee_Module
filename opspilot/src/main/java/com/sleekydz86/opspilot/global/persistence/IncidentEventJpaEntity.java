package com.sleekydz86.opspilot.global.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "incident_event")
public class IncidentEventJpaEntity {

	@Id
	private UUID id;

	@Column(nullable = false)
	private UUID incidentId;

	@Column(nullable = false)
	private String eventType;

	@Column(nullable = false, length = 4000)
	private String payload;

	@Column(nullable = false)
	private Instant occurredAt;

	protected IncidentEventJpaEntity() {
	}

	public IncidentEventJpaEntity(UUID id, UUID incidentId, String eventType, String payload, Instant occurredAt) {
		this.id = id;
		this.incidentId = incidentId;
		this.eventType = eventType;
		this.payload = payload;
		this.occurredAt = occurredAt;
	}

	public UUID getId() {
		return id;
	}

	public UUID getIncidentId() {
		return incidentId;
	}

	public String getEventType() {
		return eventType;
	}

	public String getPayload() {
		return payload;
	}

	public Instant getOccurredAt() {
		return occurredAt;
	}
}
