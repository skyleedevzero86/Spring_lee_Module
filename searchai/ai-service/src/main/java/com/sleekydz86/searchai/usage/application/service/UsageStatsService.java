package com.sleekydz86.searchai.usage.application.service;

import com.sleekydz86.searchai.usage.application.port.out.UsageStorePort;
import com.sleekydz86.searchai.usage.domain.LlmUsageRecord;
import com.sleekydz86.searchai.usage.domain.StatsGranularity;
import com.sleekydz86.searchai.usage.domain.UsageDetailedStats;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public final class UsageStatsService {

	private static final DateTimeFormatter HOUR_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00");
	private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final Map<DayOfWeek, String> DOW_KO = Map.of(
		DayOfWeek.MONDAY, "월",
		DayOfWeek.TUESDAY, "화",
		DayOfWeek.WEDNESDAY, "수",
		DayOfWeek.THURSDAY, "목",
		DayOfWeek.FRIDAY, "금",
		DayOfWeek.SATURDAY, "토",
		DayOfWeek.SUNDAY, "일"
	);

	private final UsageStorePort usageStorePort;

	public UsageStatsService(UsageStorePort usageStorePort) {
		this.usageStorePort = usageStorePort;
	}

	public UsageDetailedStats summarize(Instant from, Instant to, StatsGranularity granularity, String username) {
		Instant safeFrom = from == null ? Instant.now().minus(7, ChronoUnit.DAYS) : from;
		Instant safeTo = to == null ? Instant.now() : to;
		List<LlmUsageRecord> records = usageStorePort.findBetween(username, safeFrom, safeTo);

		long totalCalls = records.size();
		long totalTokens = records.stream().mapToLong(LlmUsageRecord::totalTokens).sum();
		double totalCost = records.stream().mapToDouble(LlmUsageRecord::estimatedCost).sum();
		long cacheHits = records.stream().filter(LlmUsageRecord::cached).count();
		double avgLatency = records.stream().mapToLong(LlmUsageRecord::latencyMs).average().orElse(0);
		double successRate = totalCalls == 0 ? 0 : records.stream().filter(LlmUsageRecord::success).count() * 100.0 / totalCalls;

		return new UsageDetailedStats(
			safeFrom,
			safeTo,
			granularity,
			totalCalls,
			totalTokens,
			totalCost,
			cacheHits,
			totalCalls == 0 ? 0 : cacheHits * 100.0 / totalCalls,
			avgLatency,
			successRate,
			groupMetric(records, LlmUsageRecord::tier),
			groupMetric(records, r -> r.requestType().name()),
			groupByDayOfWeek(records),
			groupByHour(records),
			groupMetric(records, LlmUsageRecord::username),
			groupMetric(records, r -> r.routingType() == null ? "UNKNOWN" : r.routingType().name()),
			timeline(records, granularity),
			models(records)
		);
	}

	private static Map<String, UsageDetailedStats.Metric> groupMetric(
		List<LlmUsageRecord> records,
		java.util.function.Function<LlmUsageRecord, String> keyFn
	) {
		return records.stream()
			.collect(Collectors.groupingBy(keyFn, LinkedHashMap::new, Collectors.toList()))
			.entrySet().stream()
			.sorted(Map.Entry.comparingByKey())
			.collect(Collectors.toMap(
				Map.Entry::getKey,
				e -> metricOf(e.getValue()),
				(a, b) -> a,
				LinkedHashMap::new
			));
	}

	private static Map<String, UsageDetailedStats.Metric> groupByDayOfWeek(List<LlmUsageRecord> records) {
		Map<String, UsageDetailedStats.Metric> ordered = new LinkedHashMap<>();
		for (DayOfWeek day : DayOfWeek.values()) {
			ordered.put(DOW_KO.get(day), new UsageDetailedStats.Metric(0, 0, 0));
		}
		Map<String, List<LlmUsageRecord>> grouped = records.stream()
			.collect(Collectors.groupingBy(r -> DOW_KO.get(LocalDateTime.ofInstant(r.createdAt(), ZoneOffset.UTC).getDayOfWeek())));
		grouped.forEach((k, v) -> ordered.put(k, metricOf(v)));
		return ordered;
	}

	private static Map<String, UsageDetailedStats.Metric> groupByHour(List<LlmUsageRecord> records) {
		Map<String, UsageDetailedStats.Metric> ordered = new LinkedHashMap<>();
		for (int h = 0; h < 24; h++) {
			ordered.put(String.format("%02d시", h), new UsageDetailedStats.Metric(0, 0, 0));
		}
		Map<String, List<LlmUsageRecord>> grouped = records.stream()
			.collect(Collectors.groupingBy(r -> String.format("%02d시", LocalDateTime.ofInstant(r.createdAt(), ZoneOffset.UTC).getHour())));
		grouped.forEach((k, v) -> ordered.put(k, metricOf(v)));
		return ordered;
	}

	private static List<UsageDetailedStats.TimelineBucket> timeline(
		List<LlmUsageRecord> records,
		StatsGranularity granularity
	) {
		Map<String, List<LlmUsageRecord>> grouped = records.stream()
			.collect(Collectors.groupingBy(r -> bucketKey(r.createdAt(), granularity), TreeMap::new, Collectors.toList()));
		List<UsageDetailedStats.TimelineBucket> result = new ArrayList<>();
		grouped.forEach((bucket, list) -> {
			UsageDetailedStats.Metric metric = metricOf(list);
			double avgLatency = list.stream().mapToLong(LlmUsageRecord::latencyMs).average().orElse(0);
			long cacheHits = list.stream().filter(LlmUsageRecord::cached).count();
			result.add(new UsageDetailedStats.TimelineBucket(
				bucket,
				metric.count(),
				metric.tokens(),
				metric.cost(),
				cacheHits,
				avgLatency
			));
		});
		return result;
	}

	private static List<UsageDetailedStats.ModelMetric> models(List<LlmUsageRecord> records) {
		return records.stream()
			.collect(Collectors.groupingBy(LlmUsageRecord::model, LinkedHashMap::new, Collectors.toList()))
			.entrySet().stream()
			.map(entry -> {
				List<LlmUsageRecord> list = entry.getValue();
				long calls = list.size();
				long success = list.stream().filter(LlmUsageRecord::success).count();
				long failure = calls - success;
				long cacheHits = list.stream().filter(LlmUsageRecord::cached).count();
				long fallback = list.stream().filter(r -> r.fallbackModel() != null && !r.fallbackModel().isBlank()).count();
				List<Long> latencies = list.stream().map(LlmUsageRecord::latencyMs).sorted().toList();
				return new UsageDetailedStats.ModelMetric(
					list.getFirst().tier(),
					entry.getKey(),
					calls,
					success,
					failure,
					calls == 0 ? 0 : success * 100.0 / calls,
					list.stream().mapToLong(LlmUsageRecord::latencyMs).average().orElse(0),
					percentile(latencies, 0.95),
					list.stream().mapToLong(LlmUsageRecord::promptTokens).average().orElse(0),
					list.stream().mapToLong(LlmUsageRecord::completionTokens).average().orElse(0),
					list.stream().mapToLong(LlmUsageRecord::totalTokens).sum(),
					list.stream().mapToDouble(LlmUsageRecord::estimatedCost).sum(),
					fallback,
					calls == 0 ? 0 : cacheHits * 100.0 / calls
				);
			})
			.sorted(Comparator.comparingDouble(UsageDetailedStats.ModelMetric::estimatedCost).reversed())
			.toList();
	}

	private static double percentile(List<Long> sorted, double p) {
		if (sorted.isEmpty()) {
			return 0;
		}
		int index = (int) Math.ceil(p * sorted.size()) - 1;
		return sorted.get(Math.max(0, Math.min(sorted.size() - 1, index)));
	}

	private static UsageDetailedStats.Metric metricOf(List<LlmUsageRecord> list) {
		return new UsageDetailedStats.Metric(
			list.size(),
			list.stream().mapToLong(LlmUsageRecord::totalTokens).sum(),
			list.stream().mapToDouble(LlmUsageRecord::estimatedCost).sum()
		);
	}

	private static String bucketKey(Instant instant, StatsGranularity granularity) {
		LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
		return switch (granularity) {
			case HOUR -> ldt.format(HOUR_FMT);
			case DAY -> ldt.format(DAY_FMT);
			case WEEK -> {
				WeekFields weekFields = WeekFields.of(Locale.KOREA);
				int week = ldt.get(weekFields.weekOfWeekBasedYear());
				int year = ldt.get(weekFields.weekBasedYear());
				yield year + "-W" + String.format("%02d", week);
			}
		};
	}
}
