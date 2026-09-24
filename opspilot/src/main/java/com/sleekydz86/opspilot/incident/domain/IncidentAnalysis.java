package com.sleekydz86.opspilot.incident.domain;

import java.time.Instant;
import java.util.UUID;

public record IncidentAnalysis(
	UUID id,
	IncidentId incidentId,
	boolean urgent,
	double urgencyProbability,
	IncidentType category,
	Severity severity,
	String team,
	String summary,
	String recommendedAction,
	AnalysisSource analysisSource,
	boolean cacheHit,
	String model,
	long inputTokens,
	long outputTokens,
	Instant analyzedAt
) {

	public IncidentAnalysis {
		if (id == null || incidentId == null || category == null || severity == null || analysisSource == null || analyzedAt == null) {
			throw new IllegalArgumentException("분석 결과가 불완전합니다");
		}
		if (urgencyProbability < 0 || urgencyProbability > 1) {
			throw new IllegalArgumentException("긴급 확률은 0과 1 사이여야 합니다");
		}
		if (team == null || team.isBlank()) {
			throw new IllegalArgumentException("담당 팀이 필요합니다");
		}
		if (summary == null || summary.isBlank()) {
			throw new IllegalArgumentException("요약이 필요합니다");
		}
		if (recommendedAction == null || recommendedAction.isBlank()) {
			throw new IllegalArgumentException("권장 조치가 필요합니다");
		}
		if (model == null || model.isBlank()) {
			throw new IllegalArgumentException("모델 정보가 필요합니다");
		}
	}
}
