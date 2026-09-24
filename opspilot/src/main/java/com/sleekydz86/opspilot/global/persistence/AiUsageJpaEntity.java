package com.sleekydz86.opspilot.global.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ai_usage")
public class AiUsageJpaEntity {

	@Id
	private UUID id;

	@Column(nullable = false)
	private UUID incidentId;

	@Column(nullable = false)
	private String model;

	@Column(nullable = false)
	private long inputTokens;

	@Column(nullable = false)
	private long outputTokens;

	@Column(nullable = false)
	private boolean cacheHit;

	@Column(nullable = false)
	private Instant createdAt;

	protected AiUsageJpaEntity() {
	}

	public AiUsageJpaEntity(
		UUID id,
		UUID incidentId,
		String model,
		long inputTokens,
		long outputTokens,
		boolean cacheHit,
		Instant createdAt
	) {
		this.id = id;
		this.incidentId = incidentId;
		this.model = model;
		this.inputTokens = inputTokens;
		this.outputTokens = outputTokens;
		this.cacheHit = cacheHit;
		this.createdAt = createdAt;
	}

	public UUID getId() {
		return id;
	}

	public UUID getIncidentId() {
		return incidentId;
	}

	public String getModel() {
		return model;
	}

	public long getInputTokens() {
		return inputTokens;
	}

	public long getOutputTokens() {
		return outputTokens;
	}

	public boolean isCacheHit() {
		return cacheHit;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
