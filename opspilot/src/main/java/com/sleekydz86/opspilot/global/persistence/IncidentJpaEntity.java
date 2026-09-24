package com.sleekydz86.opspilot.global.persistence;

import com.sleekydz86.opspilot.incident.domain.IncidentStatus;
import com.sleekydz86.opspilot.incident.domain.IncidentType;
import com.sleekydz86.opspilot.incident.domain.Severity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "incident")
public class IncidentJpaEntity {

	@Id
	private UUID id;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false, length = 4000)
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private IncidentType type;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Severity severity;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private IncidentStatus status;

	@Column(nullable = false)
	private String source;

	@Column(nullable = false, length = 64)
	private String fingerprint;

	@Column(nullable = false)
	private Instant createdAt;

	private Instant resolvedAt;

	protected IncidentJpaEntity() {
	}

	public IncidentJpaEntity(
		UUID id,
		String title,
		String description,
		IncidentType type,
		Severity severity,
		IncidentStatus status,
		String source,
		String fingerprint,
		Instant createdAt,
		Instant resolvedAt
	) {
		this.id = id;
		this.title = title;
		this.description = description;
		this.type = type;
		this.severity = severity;
		this.status = status;
		this.source = source;
		this.fingerprint = fingerprint;
		this.createdAt = createdAt;
		this.resolvedAt = resolvedAt;
	}

	public UUID getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public IncidentType getType() {
		return type;
	}

	public Severity getSeverity() {
		return severity;
	}

	public IncidentStatus getStatus() {
		return status;
	}

	public String getSource() {
		return source;
	}

	public String getFingerprint() {
		return fingerprint;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getResolvedAt() {
		return resolvedAt;
	}
}
