package com.sleekydz86.opspilot.incident.application.port.in;

import com.sleekydz86.opspilot.incident.domain.IncidentType;
import com.sleekydz86.opspilot.incident.domain.Severity;

public record TriageIncidentCommand(
	String title,
	String description,
	IncidentType type,
	Severity severity,
	String source
) {

	public TriageIncidentCommand {
		if (title == null || title.isBlank()) {
			throw new IllegalArgumentException("제목이 필요합니다");
		}
		if (description == null || description.isBlank()) {
			throw new IllegalArgumentException("설명이 필요합니다");
		}
		if (type == null) {
			type = IncidentType.UNKNOWN;
		}
		if (severity == null) {
			severity = Severity.MEDIUM;
		}
		if (source == null || source.isBlank()) {
			source = "api";
		}
	}
}
