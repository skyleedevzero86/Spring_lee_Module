package com.sleekydz86.opspilot.incident.domain;

import java.util.List;
import java.util.Map;

public record IncidentStats(
	long totalIncidents,
	long urgentCount,
	long cacheHitCount,
	long fallbackCount,
	double cacheHitRatio,
	Map<String, Long> bySeverity,
	Map<String, Long> byCategory,
	Map<String, Long> bySource,
	Map<String, Long> byTeam,
	List<TimeBucket> timeline
) {

	public record TimeBucket(String bucket, long count, long urgentCount, long cacheHits) {
	}
}
