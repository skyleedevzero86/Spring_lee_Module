package com.sleekydz86.catalogflow.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
public class OutboxEventEntity {

	@Id
	private UUID id;

	@Column(name = "aggregate_id", nullable = false)
	private UUID aggregateId;

	@Column(name = "aggregate_type", nullable = false, length = 100)
	private String aggregateType;

	@Column(name = "event_type", nullable = false, length = 100)
	private String eventType;

	@Column(name = "aggregate_version", nullable = false)
	private long aggregateVersion;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(nullable = false, columnDefinition = "jsonb")
	private String payload;

	@Column(name = "correlation_id", length = 100)
	private String correlationId;

	@Column(name = "causation_id", length = 100)
	private String causationId;

	@Column(name = "schema_version", nullable = false)
	private int schemaVersion;

	@Column(nullable = false)
	private boolean published;

	@Column(name = "published_at")
	private Instant publishedAt;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected OutboxEventEntity() {
	}

	public static OutboxEventEntity createEmpty() {
		return new OutboxEventEntity();
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public UUID getAggregateId() {
		return aggregateId;
	}

	public void setAggregateId(UUID aggregateId) {
		this.aggregateId = aggregateId;
	}

	public String getAggregateType() {
		return aggregateType;
	}

	public void setAggregateType(String aggregateType) {
		this.aggregateType = aggregateType;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public long getAggregateVersion() {
		return aggregateVersion;
	}

	public void setAggregateVersion(long aggregateVersion) {
		this.aggregateVersion = aggregateVersion;
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	public String getCorrelationId() {
		return correlationId;
	}

	public void setCorrelationId(String correlationId) {
		this.correlationId = correlationId;
	}

	public String getCausationId() {
		return causationId;
	}

	public void setCausationId(String causationId) {
		this.causationId = causationId;
	}

	public int getSchemaVersion() {
		return schemaVersion;
	}

	public void setSchemaVersion(int schemaVersion) {
		this.schemaVersion = schemaVersion;
	}

	public boolean isPublished() {
		return published;
	}

	public void setPublished(boolean published) {
		this.published = published;
	}

	public Instant getPublishedAt() {
		return publishedAt;
	}

	public void setPublishedAt(Instant publishedAt) {
		this.publishedAt = publishedAt;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}
}
