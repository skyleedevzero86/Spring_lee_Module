package com.sleekydz86.opspilot.incident.adapter.in.web;

import com.sleekydz86.opspilot.incident.application.port.in.QueryStatsUseCase;
import com.sleekydz86.opspilot.incident.domain.IncidentStats;
import com.sleekydz86.opspilot.incident.domain.StatsGranularity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
public final class StatsController {

	private final QueryStatsUseCase queryStatsUseCase;

	public StatsController(QueryStatsUseCase queryStatsUseCase) {
		this.queryStatsUseCase = queryStatsUseCase;
	}

	@GetMapping
	public StatsResponse stats(
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
		@RequestParam(defaultValue = "DAY") StatsGranularity granularity
	) {
		Instant safeFrom = from == null ? Instant.now().minus(7, ChronoUnit.DAYS) : from;
		Instant safeTo = to == null ? Instant.now() : to;
		IncidentStats stats = queryStatsUseCase.summarize(safeFrom, safeTo, granularity);
		return StatsResponse.from(stats, granularity.name(), safeFrom.toString(), safeTo.toString());
	}

	public record StatsResponse(
		String from,
		String to,
		String granularity,
		long totalIncidents,
		long urgentCount,
		long cacheHitCount,
		long fallbackCount,
		double cacheHitRatio,
		Map<String, Long> bySeverity,
		Map<String, Long> byCategory,
		Map<String, Long> bySource,
		Map<String, Long> byTeam,
		List<BucketResponse> timeline
	) {

		static StatsResponse from(IncidentStats stats, String granularity, String from, String to) {
			return new StatsResponse(
				from,
				to,
				granularity,
				stats.totalIncidents(),
				stats.urgentCount(),
				stats.cacheHitCount(),
				stats.fallbackCount(),
				stats.cacheHitRatio(),
				stats.bySeverity(),
				stats.byCategory(),
				stats.bySource(),
				stats.byTeam(),
				stats.timeline().stream()
					.map(b -> new BucketResponse(b.bucket(), b.count(), b.urgentCount(), b.cacheHits()))
					.toList()
			);
		}
	}

	public record BucketResponse(String bucket, long count, long urgentCount, long cacheHits) {
	}
}
