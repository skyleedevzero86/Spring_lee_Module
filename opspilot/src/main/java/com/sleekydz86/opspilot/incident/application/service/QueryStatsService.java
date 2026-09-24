package com.sleekydz86.opspilot.incident.application.service;

import com.sleekydz86.opspilot.incident.application.port.in.QueryStatsUseCase;
import com.sleekydz86.opspilot.incident.application.port.out.IncidentAnalysisRepositoryPort;
import com.sleekydz86.opspilot.incident.application.port.out.IncidentRepositoryPort;
import com.sleekydz86.opspilot.incident.domain.AnalysisSource;
import com.sleekydz86.opspilot.incident.domain.Incident;
import com.sleekydz86.opspilot.incident.domain.IncidentAnalysis;
import com.sleekydz86.opspilot.incident.domain.IncidentId;
import com.sleekydz86.opspilot.incident.domain.IncidentStats;
import com.sleekydz86.opspilot.incident.domain.StatsGranularity;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class QueryStatsService implements QueryStatsUseCase {

	private static final DateTimeFormatter HOUR_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00").withZone(ZoneOffset.UTC);
	private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneOffset.UTC);
	private static final DateTimeFormatter WEEK_FMT = DateTimeFormatter.ofPattern("YYYY-'W'ww").withZone(ZoneOffset.UTC);

	private final IncidentRepositoryPort incidentRepository;
	private final IncidentAnalysisRepositoryPort analysisRepository;

	public QueryStatsService(
		IncidentRepositoryPort incidentRepository,
		IncidentAnalysisRepositoryPort analysisRepository
	) {
		this.incidentRepository = incidentRepository;
		this.analysisRepository = analysisRepository;
	}

	@Override
	public IncidentStats summarize(Instant from, Instant to, StatsGranularity granularity) {
		Instant safeFrom = from == null ? Instant.now().minus(7, ChronoUnit.DAYS) : from;
		Instant safeTo = to == null ? Instant.now() : to;
		if (safeFrom.isAfter(safeTo)) {
			throw new IllegalArgumentException("시작 시각이 종료 시각보다 늦을 수 없습니다");
		}
		StatsGranularity grain = granularity == null ? StatsGranularity.DAY : granularity;
		List<Incident> incidents = incidentRepository.findBetween(safeFrom, safeTo);
		List<IncidentId> ids = incidents.stream().map(Incident::id).toList();
		Map<IncidentId, IncidentAnalysis> analyses = analysisRepository.findByIncidentIds(ids).stream()
			.collect(Collectors.toMap(IncidentAnalysis::incidentId, Function.identity(), (a, b) -> a));

		long urgentCount = analyses.values().stream().filter(IncidentAnalysis::urgent).count();
		long cacheHitCount = analyses.values().stream().filter(IncidentAnalysis::cacheHit).count();
		long fallbackCount = analyses.values().stream()
			.filter(a -> a.analysisSource() == AnalysisSource.FALLBACK)
			.count();
		double cacheHitRatio = analyses.isEmpty() ? 0.0 : (double) cacheHitCount / analyses.size();

		Map<String, Long> bySeverity = countBy(analyses.values().stream().map(a -> a.severity().name()).toList());
		Map<String, Long> byCategory = countBy(analyses.values().stream().map(a -> a.category().name()).toList());
		Map<String, Long> bySource = countBy(analyses.values().stream().map(a -> a.analysisSource().name()).toList());
		Map<String, Long> byTeam = countBy(analyses.values().stream().map(IncidentAnalysis::team).toList());

		DateTimeFormatter formatter = switch (grain) {
			case HOUR -> HOUR_FMT;
			case DAY -> DAY_FMT;
			case WEEK -> WEEK_FMT;
		};

		Map<String, List<Incident>> grouped = incidents.stream()
			.collect(Collectors.groupingBy(i -> formatter.format(i.createdAt()), LinkedHashMap::new, Collectors.toList()));

		List<IncidentStats.TimeBucket> timeline = grouped.entrySet().stream()
			.sorted(Map.Entry.comparingByKey())
			.map(entry -> {
				long urgent = entry.getValue().stream()
					.map(Incident::id)
					.filter(analyses::containsKey)
					.map(analyses::get)
					.filter(IncidentAnalysis::urgent)
					.count();
				long hits = entry.getValue().stream()
					.map(Incident::id)
					.filter(analyses::containsKey)
					.map(analyses::get)
					.filter(IncidentAnalysis::cacheHit)
					.count();
				return new IncidentStats.TimeBucket(entry.getKey(), entry.getValue().size(), urgent, hits);
			})
			.sorted(Comparator.comparing(IncidentStats.TimeBucket::bucket))
			.toList();

		return new IncidentStats(
			incidents.size(),
			urgentCount,
			cacheHitCount,
			fallbackCount,
			cacheHitRatio,
			bySeverity,
			byCategory,
			bySource,
			byTeam,
			timeline
		);
	}

	private static Map<String, Long> countBy(List<String> values) {
		return values.stream()
			.collect(Collectors.groupingBy(Function.identity(), LinkedHashMap::new, Collectors.counting()));
	}
}
