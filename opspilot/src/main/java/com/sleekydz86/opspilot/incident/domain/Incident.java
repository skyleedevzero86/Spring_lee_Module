package com.sleekydz86.opspilot.incident.domain;

import java.time.Instant;

public record Incident(
	IncidentId id,
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

	public Incident {
		if (id == null) {
			throw new IllegalArgumentException("장애 ID가 필요합니다");
		}
		if (title == null || title.isBlank()) {
			throw new IllegalArgumentException("제목이 필요합니다");
		}
		if (description == null || description.isBlank()) {
			throw new IllegalArgumentException("설명이 필요합니다");
		}
		if (type == null) {
			throw new IllegalArgumentException("장애 유형이 필요합니다");
		}
		if (severity == null) {
			throw new IllegalArgumentException("심각도가 필요합니다");
		}
		if (status == null) {
			throw new IllegalArgumentException("상태가 필요합니다");
		}
		if (source == null || source.isBlank()) {
			throw new IllegalArgumentException("발생 소스가 필요합니다");
		}
		if (fingerprint == null || fingerprint.isBlank()) {
			throw new IllegalArgumentException("지문이 필요합니다");
		}
		if (createdAt == null) {
			throw new IllegalArgumentException("생성 시각이 필요합니다");
		}
	}

	public static Incident open(
		String title,
		String description,
		IncidentType type,
		Severity severity,
		String source,
		String fingerprint
	) {
		return new Incident(
			IncidentId.newId(),
			title,
			description,
			type,
			severity,
			IncidentStatus.OPEN,
			source,
			fingerprint,
			Instant.now(),
			null
		);
	}

	public Incident withStatus(IncidentStatus next) {
		return new Incident(id, title, description, type, severity, next, source, fingerprint, createdAt, resolvedAt);
	}

	public Incident withSeverity(Severity next) {
		return new Incident(id, title, description, type, next, status, source, fingerprint, createdAt, resolvedAt);
	}

	public Incident withType(IncidentType next) {
		return new Incident(id, title, description, next, severity, status, source, fingerprint, createdAt, resolvedAt);
	}

	public Incident resolve() {
		return new Incident(id, title, description, type, severity, IncidentStatus.RESOLVED, source, fingerprint, createdAt, Instant.now());
	}
}
