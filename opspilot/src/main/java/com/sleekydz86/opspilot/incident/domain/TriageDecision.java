package com.sleekydz86.opspilot.incident.domain;

public record TriageDecision(
	boolean urgent,
	double urgencyProbability,
	IncidentType category,
	Severity severity,
	String team,
	String summary,
	String recommendedAction,
	AnalysisSource analysisSource,
	String model,
	long inputTokens,
	long outputTokens,
	boolean cacheHit
) {

	public TriageDecision {
		if (category == null || severity == null || analysisSource == null) {
			throw new IllegalArgumentException("분석 결정이 불완전합니다");
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

	public TriageDecision asCacheHit() {
		return new TriageDecision(
			urgent,
			urgencyProbability,
			category,
			severity,
			team,
			summary,
			recommendedAction,
			AnalysisSource.CACHE,
			model,
			0L,
			0L,
			true
		);
	}
}
