package com.sleekydz86.opspilot.global.persistence;

import com.sleekydz86.opspilot.incident.domain.AnalysisSource;
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
@Table(name = "incident_analysis")
public class IncidentAnalysisJpaEntity {

	@Id
	private UUID id;

	@Column(nullable = false)
	private UUID incidentId;

	@Column(nullable = false)
	private boolean urgent;

	@Column(nullable = false)
	private double urgencyProbability;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private IncidentType category;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Severity severity;

	@Column(nullable = false)
	private String team;

	@Column(nullable = false, length = 2000)
	private String summary;

	@Column(nullable = false, length = 2000)
	private String recommendedAction;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private AnalysisSource analysisSource;

	@Column(nullable = false)
	private boolean cacheHit;

	@Column(nullable = false)
	private String model;

	@Column(nullable = false)
	private long inputTokens;

	@Column(nullable = false)
	private long outputTokens;

	@Column(nullable = false)
	private Instant analyzedAt;

	protected IncidentAnalysisJpaEntity() {
	}

	public IncidentAnalysisJpaEntity(
		UUID id,
		UUID incidentId,
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
		this.id = id;
		this.incidentId = incidentId;
		this.urgent = urgent;
		this.urgencyProbability = urgencyProbability;
		this.category = category;
		this.severity = severity;
		this.team = team;
		this.summary = summary;
		this.recommendedAction = recommendedAction;
		this.analysisSource = analysisSource;
		this.cacheHit = cacheHit;
		this.model = model;
		this.inputTokens = inputTokens;
		this.outputTokens = outputTokens;
		this.analyzedAt = analyzedAt;
	}

	public UUID getId() {
		return id;
	}

	public UUID getIncidentId() {
		return incidentId;
	}

	public boolean isUrgent() {
		return urgent;
	}

	public double getUrgencyProbability() {
		return urgencyProbability;
	}

	public IncidentType getCategory() {
		return category;
	}

	public Severity getSeverity() {
		return severity;
	}

	public String getTeam() {
		return team;
	}

	public String getSummary() {
		return summary;
	}

	public String getRecommendedAction() {
		return recommendedAction;
	}

	public AnalysisSource getAnalysisSource() {
		return analysisSource;
	}

	public boolean isCacheHit() {
		return cacheHit;
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

	public Instant getAnalyzedAt() {
		return analyzedAt;
	}
}
