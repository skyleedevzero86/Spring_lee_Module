package com.sleekydz86.opspilot.incident.domain;

public record IncidentReport(Incident incident, IncidentAnalysis analysis) {

	public IncidentReport {
		if (incident == null || analysis == null) {
			throw new IllegalArgumentException("장애 리포트가 불완전합니다");
		}
	}
}
