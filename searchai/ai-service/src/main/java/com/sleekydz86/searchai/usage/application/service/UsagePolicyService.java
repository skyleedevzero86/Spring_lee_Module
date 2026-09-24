package com.sleekydz86.searchai.usage.application.service;

import com.sleekydz86.searchai.global.metrics.LlmMetricsService;
import com.sleekydz86.searchai.router.domain.ModelTier;
import com.sleekydz86.searchai.router.domain.RoutingDecision;
import com.sleekydz86.searchai.router.domain.RoutingType;
import com.sleekydz86.searchai.usage.application.anomaly.AnomalyDetectionStrategy;
import com.sleekydz86.searchai.usage.application.port.out.NotificationSender;
import com.sleekydz86.searchai.usage.application.port.out.UsageStorePort;
import com.sleekydz86.searchai.usage.domain.BudgetState;
import com.sleekydz86.searchai.usage.domain.LlmUsageRecord;
import com.sleekydz86.searchai.usage.domain.PolicyDecision;
import com.sleekydz86.searchai.usage.domain.RequestType;
import com.sleekydz86.searchai.usage.domain.RuntimePolicy;
import com.sleekydz86.searchai.usage.domain.TokenCostCalculator;
import com.sleekydz86.searchai.usage.domain.UsageAlert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public final class UsagePolicyService {

	private static final Logger log = LoggerFactory.getLogger(UsagePolicyService.class);
	private static final long ALERT_COOLDOWN_MS = 5 * 60_000L;

	private final UsageStorePort usageStorePort;
	private final RuntimePolicyService runtimePolicyService;
	private final LlmMetricsService llmMetricsService;
	private final List<AnomalyDetectionStrategy> anomalyStrategies;
	private final List<NotificationSender> notificationSenders;
	private final ConcurrentHashMap<String, WindowCounter> rateWindows = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<String, Long> alertCooldown = new ConcurrentHashMap<>();

	public UsagePolicyService(
		UsageStorePort usageStorePort,
		RuntimePolicyService runtimePolicyService,
		LlmMetricsService llmMetricsService,
		List<AnomalyDetectionStrategy> anomalyStrategies,
		List<NotificationSender> notificationSenders
	) {
		this.usageStorePort = usageStorePort;
		this.runtimePolicyService = runtimePolicyService;
		this.llmMetricsService = llmMetricsService;
		this.anomalyStrategies = anomalyStrategies;
		this.notificationSenders = notificationSenders;
	}

	public PolicyDecision authorize(String username, RequestType requestType, long estimatedTokens) {
		RuntimePolicy policy = runtimePolicyService.current();
		if (!tryAcquireRate(username, policy.rateLimitPerMinute())) {
			llmMetricsService.recordBlocked();
			return PolicyDecision.deny("분당 요청 한도를 초과했습니다. 잠시 후 다시 시도해 주세요.");
		}
		if (estimatedTokens > policy.perRequestLimit()) {
			return PolicyDecision.deny("요청당 최대 토큰(" + policy.perRequestLimit() + ")을 초과했습니다.");
		}

		long today = usageStorePort.sumTokensSince(username, startOfDay());
		long week = usageStorePort.sumTokensSince(username, startOfWeek());
		long month = usageStorePort.sumTokensSince(username, startOfMonth());
		double todayCost = sumCost(username, startOfDay());
		double monthCost = sumCost(username, startOfMonth());

		double dayRatio = (double) (today + estimatedTokens) / policy.dailyLimit();
		double weekRatio = (double) (week + estimatedTokens) / policy.weeklyLimit();
		double monthRatio = (double) (month + estimatedTokens) / policy.monthlyLimit();
		double dayCostRatio = (todayCost) / policy.dailyCostLimit();
		double monthCostRatio = (monthCost) / policy.monthlyCostLimit();
		double ratio = Math.max(Math.max(dayRatio, weekRatio), Math.max(monthRatio, Math.max(dayCostRatio, monthCostRatio)));
		ModelTier cap = parseTier(policy.maxTierCap());
		long maxOut = policy.maxOutputTokens();

		detectAnomaly(username, today + estimatedTokens, policy);

		if (ratio >= 1.0) {
			emitAlertOnce("HARD_LIMIT", username, "HIGH",
				"Budget 100% 도달 — user=" + username + ", ratio=" + pct(ratio));
			llmMetricsService.recordBudgetExceeded();
			if (requestType.isInternalOnly()) {
				return PolicyDecision.allowInternal(BudgetState.HARD,
					"외부 LLM 한도 초과. FAQ/DB 등 내부 조회는 계속 이용할 수 있습니다.");
			}
			if (requestType.requiresExternalLlm()) {
				return PolicyDecision.blockExternalLlm(
					BudgetState.HARD,
					ModelTier.LUNA,
					maxOut,
					"현재 AI 사용 한도를 초과했습니다. 내부 데이터 조회 및 LLM이 필요 없는 기능은 계속 사용할 수 있습니다."
				);
			}
			return PolicyDecision.deny("일/주/월 토큰 또는 비용 한도를 초과했습니다.");
		}

		if (ratio >= policy.warnRatio()) {
			emitAlertOnce("WARN_LIMIT", username, "MEDIUM",
				"Budget 90%+ — user=" + username + ", ratio=" + pct(ratio));
			llmMetricsService.recordBudgetWarning();
			llmMetricsService.recordDowngrade();
			return PolicyDecision.allow(BudgetState.WARN, min(ModelTier.TERRA, cap), maxOut);
		}

		if (ratio >= policy.softRatio()) {
			emitAlertOnce("SOFT_LIMIT", username, "LOW",
				"Budget 80%+ Soft — user=" + username + ", ratio=" + pct(ratio));
			llmMetricsService.recordBudgetWarning();
			llmMetricsService.recordDowngrade();
			return PolicyDecision.allow(BudgetState.SOFT, min(ModelTier.SOL, cap), maxOut);
		}

		return PolicyDecision.allow(BudgetState.OK, cap, maxOut);
	}

	public void record(
		String username,
		RoutingDecision decision,
		RequestType requestType,
		long promptTokens,
		long completionTokens,
		long latencyMs,
		boolean success,
		boolean cached,
		String actualModel,
		String errorType
	) {
		String selected = decision.model();
		String actual = actualModel == null || actualModel.isBlank() ? selected : actualModel;
		boolean fallback = !selected.equals(actual) || decision.routingType() == RoutingType.FALLBACK;
		String tier = TokenCostCalculator.resolveTier(actual).displayName();
		double cost = TokenCostCalculator.estimate(actual, promptTokens, completionTokens);
		usageStorePort.save(new LlmUsageRecord(
			decision.requestId(),
			username,
			decision.routingType(),
			selected,
			actual,
			tier,
			fallback,
			requestType,
			promptTokens,
			completionTokens,
			promptTokens + completionTokens,
			cost,
			latencyMs,
			success,
			errorType,
			decision.confidence(),
			cached,
			Instant.now()
		));
		llmMetricsService.recordRouting(decision.routingType());
		llmMetricsService.recordTokens(actual, promptTokens, completionTokens);
		llmMetricsService.recordCost(actual, cost);
		llmMetricsService.recordCache(cached);
		log.info("사용량 기록: user={} selected={} actual={} tokens={} cost=${} cache={}",
			username, selected, actual, promptTokens + completionTokens, cost, cached);
	}

	public UsageSnapshot snapshot(String usernameFilter) {
		RuntimePolicy policy = runtimePolicyService.current();
		Instant monthStart = startOfMonth();
		List<LlmUsageRecord> monthRecords = usageStorePort.findSince(monthStart).stream()
			.filter(r -> usernameFilter == null || usernameFilter.isBlank() || r.username().equals(usernameFilter))
			.toList();
		Instant dayStart = startOfDay();
		long todayTokens = monthRecords.stream().filter(r -> !r.createdAt().isBefore(dayStart)).mapToLong(LlmUsageRecord::totalTokens).sum();
		long monthTokens = monthRecords.stream().mapToLong(LlmUsageRecord::totalTokens).sum();
		double todayCost = monthRecords.stream().filter(r -> !r.createdAt().isBefore(dayStart)).mapToDouble(LlmUsageRecord::estimatedCost).sum();
		double monthCost = monthRecords.stream().mapToDouble(LlmUsageRecord::estimatedCost).sum();

		Map<String, List<LlmUsageRecord>> byModel = monthRecords.stream()
			.collect(java.util.stream.Collectors.groupingBy(LlmUsageRecord::model, LinkedHashMap::new, java.util.stream.Collectors.toList()));

		List<ModelStat> models = new ArrayList<>();
		byModel.forEach((model, list) -> {
			long calls = list.size();
			long tokens = list.stream().mapToLong(LlmUsageRecord::totalTokens).sum();
			double cost = list.stream().mapToDouble(LlmUsageRecord::estimatedCost).sum();
			double avgLatency = list.stream().mapToLong(LlmUsageRecord::latencyMs).average().orElse(0);
			double successRate = list.isEmpty() ? 0 : list.stream().filter(LlmUsageRecord::success).count() * 100.0 / calls;
			models.add(new ModelStat(list.getFirst().tier(), model, calls, tokens, cost, avgLatency, successRate));
		});
		models.sort(Comparator.comparingDouble(ModelStat::estimatedCost).reversed());

		double ratio = Math.max(
			(double) todayTokens / policy.dailyLimit(),
			(double) monthTokens / policy.monthlyLimit()
		);
		BudgetState state = ratio >= 1 ? BudgetState.HARD
			: ratio >= policy.warnRatio() ? BudgetState.WARN
			: ratio >= policy.softRatio() ? BudgetState.SOFT
			: BudgetState.OK;

		return new UsageSnapshot(
			todayTokens,
			monthTokens,
			todayCost,
			monthCost,
			policy.dailyLimit(),
			policy.monthlyLimit(),
			policy.perRequestLimit(),
			policy.softRatio(),
			state.name(),
			models,
			usageStorePort.recentAlerts(20)
		);
	}

	private void detectAnomaly(String username, long todayProjected, RuntimePolicy policy) {
		double avg = usageStorePort.averageDailyTokens(username, 7);
		double std = avg * 0.35;
		for (AnomalyDetectionStrategy strategy : anomalyStrategies) {
			AnomalyDetectionStrategy.AnomalyResult result = strategy.detect(username, todayProjected, avg, std);
			if (result.anomalous()) {
				emitAlertOnce("ANOMALY", username, "HIGH",
					"[LLM 사용량 경고] 사용자=" + username + ", " + result.reason());
				llmMetricsService.recordAnomaly();
				break;
			}
		}
	}

	private void emitAlertOnce(String code, String username, String severity, String message) {
		String key = code + ":" + username;
		long now = System.currentTimeMillis();
		Long last = alertCooldown.get(key);
		if (last != null && now - last < ALERT_COOLDOWN_MS) {
			return;
		}
		alertCooldown.put(key, now);
		usageStorePort.addAlert(new UsageAlert(code, message, severity, username, Instant.now()));
		notificationSenders.forEach(sender -> sender.send(code, message, severity));
	}

	private double sumCost(String username, Instant from) {
		return usageStorePort.findBetween(username, from, Instant.now()).stream()
			.mapToDouble(LlmUsageRecord::estimatedCost)
			.sum();
	}

	private boolean tryAcquireRate(String username, int limit) {
		long now = System.currentTimeMillis();
		WindowCounter counter = rateWindows.computeIfAbsent(username, key -> new WindowCounter());
		synchronized (counter) {
			if (now - counter.windowStart > 60_000L) {
				counter.windowStart = now;
				counter.count.set(0);
			}
			return counter.count.incrementAndGet() <= limit;
		}
	}

	private static String pct(double ratio) {
		return String.format("%.0f%%", ratio * 100);
	}

	private static ModelTier parseTier(String value) {
		try {
			return ModelTier.valueOf(value.trim().toUpperCase());
		} catch (Exception ex) {
			return ModelTier.ASTRA;
		}
	}

	private static ModelTier min(ModelTier a, ModelTier b) {
		return a.ordinal() <= b.ordinal() ? a : b;
	}

	private static Instant startOfDay() {
		return LocalDate.now(ZoneOffset.UTC).atStartOfDay().toInstant(ZoneOffset.UTC);
	}

	private static Instant startOfWeek() {
		return LocalDate.now(ZoneOffset.UTC)
			.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
			.atStartOfDay()
			.toInstant(ZoneOffset.UTC);
	}

	private static Instant startOfMonth() {
		return LocalDate.now(ZoneOffset.UTC).withDayOfMonth(1).atStartOfDay().toInstant(ZoneOffset.UTC);
	}

	private static final class WindowCounter {
		private long windowStart = System.currentTimeMillis();
		private final AtomicInteger count = new AtomicInteger();
	}

	public record ModelStat(
		String tier,
		String model,
		long calls,
		long totalTokens,
		double estimatedCost,
		double avgLatencyMs,
		double successRate
	) {
	}

	public record UsageSnapshot(
		long todayTokens,
		long monthTokens,
		double todayCost,
		double monthCost,
		long dailyLimit,
		long monthlyLimit,
		long perRequestLimit,
		double softRatio,
		String budgetState,
		List<ModelStat> models,
		List<UsageAlert> alerts
	) {
	}
}
