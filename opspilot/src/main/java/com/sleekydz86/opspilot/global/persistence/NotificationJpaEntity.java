package com.sleekydz86.opspilot.global.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notification")
public class NotificationJpaEntity {

	@Id
	private UUID id;

	@Column(nullable = false)
	private UUID incidentId;

	@Column(nullable = false)
	private String channel;

	@Column(nullable = false, length = 4000)
	private String payload;

	@Column(nullable = false)
	private Instant sentAt;

	protected NotificationJpaEntity() {
	}

	public NotificationJpaEntity(UUID id, UUID incidentId, String channel, String payload, Instant sentAt) {
		this.id = id;
		this.incidentId = incidentId;
		this.channel = channel;
		this.payload = payload;
		this.sentAt = sentAt;
	}

	public UUID getId() {
		return id;
	}

	public UUID getIncidentId() {
		return incidentId;
	}

	public String getChannel() {
		return channel;
	}

	public String getPayload() {
		return payload;
	}

	public Instant getSentAt() {
		return sentAt;
	}
}
