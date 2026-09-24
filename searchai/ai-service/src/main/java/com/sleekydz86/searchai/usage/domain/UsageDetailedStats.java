package com.sleekydz86.searchai.usage.domain;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record UsageDetailedStats(
	Instant from,
	Instant to,
	StatsGranularity granularity,
	long totalCalls,
	long totalTokens,
	double totalCost,
	long cacheHits,
	double cacheHitRatio,
	double avgLatencyMs,
	double successRate,
	Map<String, Metric> byTier,
	Map<String, Metric> byRequestType,
	Map<String, Metric> byDayOfWeek,
	Map<String, Metric> byHour,
	Map<String, Metric> byUsername,
	Map<String, Metric> byRoutingType,
	List<TimelineBucket> timeline,
	List<ModelMetric> models
) {

	public record Metric(long count, long tokens, double cost) {
	}

	public record TimelineBucket(
		String bucket,
		long calls,
		long tokens,
		double cost,
		long cacheHits,
		double avgLatencyMs
	) {
	}

	public record ModelMetric(
		String tier,
		String model,
		long calls,
		long successCount,
		long failureCount,
		double successRate,
		double avgLatencyMs,
		double p95LatencyMs,
		double avgPromptTokens,
		double avgCompletionTokens,
		long totalTokens,
		double estimatedCost,
		long fallbackCount,
		double cacheHitRatio
	) {
	}
}
