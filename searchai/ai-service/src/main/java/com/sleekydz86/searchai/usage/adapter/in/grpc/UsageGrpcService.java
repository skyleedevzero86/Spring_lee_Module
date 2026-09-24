package com.sleekydz86.searchai.usage.adapter.in.grpc;

import com.sleekydz86.searchai.chat.application.service.AbTestService;
import com.sleekydz86.searchai.proto.AbTestRequest;
import com.sleekydz86.searchai.proto.AbTestResponse;
import com.sleekydz86.searchai.proto.AbTestVariant;
import com.sleekydz86.searchai.proto.AlertsResponse;
import com.sleekydz86.searchai.proto.DetailedStatsRequest;
import com.sleekydz86.searchai.proto.DetailedStatsResponse;
import com.sleekydz86.searchai.proto.Empty;
import com.sleekydz86.searchai.proto.ModelCost;
import com.sleekydz86.searchai.proto.NamedCount;
import com.sleekydz86.searchai.proto.RuntimePolicy;
import com.sleekydz86.searchai.proto.TimelineBucket;
import com.sleekydz86.searchai.proto.UsageAlert;
import com.sleekydz86.searchai.proto.UsageServiceGrpc;
import com.sleekydz86.searchai.proto.UsageSummaryRequest;
import com.sleekydz86.searchai.proto.UsageSummaryResponse;
import com.sleekydz86.searchai.usage.application.service.RuntimePolicyService;
import com.sleekydz86.searchai.usage.application.service.UsagePolicyService;
import com.sleekydz86.searchai.usage.application.service.UsageStatsService;
import com.sleekydz86.searchai.usage.domain.StatsGranularity;
import com.sleekydz86.searchai.usage.domain.UsageDetailedStats;
import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;

import java.time.Instant;
import java.util.Map;

@GrpcService
public final class UsageGrpcService extends UsageServiceGrpc.UsageServiceImplBase {

	private final UsagePolicyService usagePolicyService;
	private final UsageStatsService usageStatsService;
	private final RuntimePolicyService runtimePolicyService;
	private final AbTestService abTestService;

	public UsageGrpcService(
		UsagePolicyService usagePolicyService,
		UsageStatsService usageStatsService,
		RuntimePolicyService runtimePolicyService,
		AbTestService abTestService
	) {
		this.usagePolicyService = usagePolicyService;
		this.usageStatsService = usageStatsService;
		this.runtimePolicyService = runtimePolicyService;
		this.abTestService = abTestService;
	}

	@Override
	public void getUsageSummary(UsageSummaryRequest request, StreamObserver<UsageSummaryResponse> responseObserver) {
		UsagePolicyService.UsageSnapshot snapshot = usagePolicyService.snapshot(request.getUsername());
		UsageSummaryResponse.Builder builder = UsageSummaryResponse.newBuilder()
			.setTodayTokens(snapshot.todayTokens())
			.setMonthTokens(snapshot.monthTokens())
			.setTodayCost(snapshot.todayCost())
			.setMonthCost(snapshot.monthCost())
			.setDailyLimit(snapshot.dailyLimit())
			.setMonthlyLimit(snapshot.monthlyLimit())
			.setPerRequestLimit(snapshot.perRequestLimit())
			.setSoftRatio(snapshot.softRatio())
			.setBudgetState(snapshot.budgetState());
		snapshot.models().forEach(model -> builder.addModels(ModelCost.newBuilder()
			.setTier(model.tier())
			.setModel(model.model())
			.setCalls(model.calls())
			.setTotalTokens(model.totalTokens())
			.setEstimatedCost(model.estimatedCost())
			.setAvgLatencyMs(model.avgLatencyMs())
			.setSuccessRate(model.successRate())
			.build()));
		responseObserver.onNext(builder.build());
		responseObserver.onCompleted();
	}

	@Override
	public void getAlerts(Empty request, StreamObserver<AlertsResponse> responseObserver) {
		AlertsResponse.Builder builder = AlertsResponse.newBuilder();
		usagePolicyService.snapshot(null).alerts().forEach(alert -> builder.addAlerts(UsageAlert.newBuilder()
			.setCode(alert.code())
			.setMessage(alert.message())
			.setSeverity(alert.severity())
			.setUsername(alert.username())
			.setCreatedAtEpochMs(alert.createdAt().toEpochMilli())
			.build()));
		responseObserver.onNext(builder.build());
		responseObserver.onCompleted();
	}

	@Override
	public void getDetailedStats(DetailedStatsRequest request, StreamObserver<DetailedStatsResponse> responseObserver) {
		Instant from = parseInstant(request.getFromIso(), Instant.now().minusSeconds(7 * 86400L));
		Instant to = parseInstant(request.getToIso(), Instant.now());
		UsageDetailedStats stats = usageStatsService.summarize(
			from,
			to,
			StatsGranularity.from(request.getGranularity()),
			request.getUsername()
		);
		DetailedStatsResponse.Builder builder = DetailedStatsResponse.newBuilder()
			.setFromIso(stats.from().toString())
			.setToIso(stats.to().toString())
			.setGranularity(stats.granularity().name())
			.setTotalCalls(stats.totalCalls())
			.setTotalTokens(stats.totalTokens())
			.setTotalCost(stats.totalCost())
			.setCacheHits(stats.cacheHits())
			.setCacheHitRatio(stats.cacheHitRatio())
			.setAvgLatencyMs(stats.avgLatencyMs())
			.setSuccessRate(stats.successRate());
		addNamed(builder::addByTier, stats.byTier());
		addNamed(builder::addByRequestType, stats.byRequestType());
		addNamed(builder::addByDayOfWeek, stats.byDayOfWeek());
		addNamed(builder::addByHour, stats.byHour());
		addNamed(builder::addByUsername, stats.byUsername());
		addNamed(builder::addByRoutingType, stats.byRoutingType());
		stats.timeline().forEach(bucket -> builder.addTimeline(TimelineBucket.newBuilder()
			.setBucket(bucket.bucket())
			.setCalls(bucket.calls())
			.setTokens(bucket.tokens())
			.setCost(bucket.cost())
			.setCacheHits(bucket.cacheHits())
			.setAvgLatencyMs(bucket.avgLatencyMs())
			.build()));
		stats.models().forEach(model -> builder.addModels(ModelCost.newBuilder()
			.setTier(model.tier())
			.setModel(model.model())
			.setCalls(model.calls())
			.setTotalTokens(model.totalTokens())
			.setEstimatedCost(model.estimatedCost())
			.setAvgLatencyMs(model.avgLatencyMs())
			.setSuccessRate(model.successRate())
			.setSuccessCount(model.successCount())
			.setFailureCount(model.failureCount())
			.setP95LatencyMs(model.p95LatencyMs())
			.setAvgPromptTokens(model.avgPromptTokens())
			.setAvgCompletionTokens(model.avgCompletionTokens())
			.setFallbackCount(model.fallbackCount())
			.setCacheHitRatio(model.cacheHitRatio())
			.build()));
		responseObserver.onNext(builder.build());
		responseObserver.onCompleted();
	}

	@Override
	public void getRuntimePolicy(Empty request, StreamObserver<RuntimePolicy> responseObserver) {
		responseObserver.onNext(toProto(runtimePolicyService.current()));
		responseObserver.onCompleted();
	}

	@Override
	public void updateRuntimePolicy(RuntimePolicy request, StreamObserver<RuntimePolicy> responseObserver) {
		com.sleekydz86.searchai.usage.domain.RuntimePolicy updated = runtimePolicyService.update(
			new com.sleekydz86.searchai.usage.domain.RuntimePolicy(
				request.getMaxOutputTokens() <= 0 ? 300 : request.getMaxOutputTokens(),
				request.getPerRequestLimit(),
				request.getDailyLimit(),
				request.getWeeklyLimit() <= 0 ? 25_000 : request.getWeeklyLimit(),
				request.getMonthlyLimit(),
				request.getDailyCostLimit() <= 0 ? 2.0 : request.getDailyCostLimit(),
				request.getMonthlyCostLimit() <= 0 ? 10.0 : request.getMonthlyCostLimit(),
				request.getSoftRatio(),
				request.getWarnRatio(),
				request.getAnomalyMultiplier(),
				request.getRateLimitPerMinute(),
				request.getRouterMode(),
				request.getConfidenceThreshold(),
				request.getMaxTierCap(),
				request.getCacheEnabled(),
				request.getDefaultPlan().isBlank() ? "PRO" : request.getDefaultPlan()
			)
		);
		responseObserver.onNext(toProto(updated));
		responseObserver.onCompleted();
	}

	@Override
	public void runAbTest(AbTestRequest request, StreamObserver<AbTestResponse> responseObserver) {
		abTestService.compare(request.getPrompt(), request.getTiersList())
			.map(result -> {
				AbTestResponse.Builder builder = AbTestResponse.newBuilder().setPrompt(result.prompt());
				result.variants().forEach(v -> builder.addVariants(AbTestVariant.newBuilder()
					.setTier(v.tier())
					.setModel(v.model())
					.setResponse(v.response() == null ? "" : v.response())
					.setLatencyMs(v.latencyMs())
					.setPromptTokens(v.promptTokens())
					.setCompletionTokens(v.completionTokens())
					.setTotalTokens(v.totalTokens())
					.setEstimatedCost(v.estimatedCost())
					.setSuccess(v.success())
					.setError(v.error() == null ? "" : v.error())
					.build()));
				return builder.build();
			})
			.subscribe(
				responseObserver::onNext,
				responseObserver::onError,
				responseObserver::onCompleted
			);
	}

	private static void addNamed(
		java.util.function.Consumer<NamedCount> consumer,
		Map<String, UsageDetailedStats.Metric> map
	) {
		map.forEach((name, metric) -> consumer.accept(NamedCount.newBuilder()
			.setName(name)
			.setCount(metric.tokens())
			.setCost(metric.cost())
			.build()));
	}

	private static RuntimePolicy toProto(com.sleekydz86.searchai.usage.domain.RuntimePolicy policy) {
		return RuntimePolicy.newBuilder()
			.setPerRequestLimit(policy.perRequestLimit())
			.setDailyLimit(policy.dailyLimit())
			.setMonthlyLimit(policy.monthlyLimit())
			.setSoftRatio(policy.softRatio())
			.setWarnRatio(policy.warnRatio())
			.setAnomalyMultiplier(policy.anomalyMultiplier())
			.setRateLimitPerMinute(policy.rateLimitPerMinute())
			.setRouterMode(policy.routerMode())
			.setConfidenceThreshold(policy.confidenceThreshold())
			.setMaxTierCap(policy.maxTierCap())
			.setCacheEnabled(policy.cacheEnabled())
			.setMaxOutputTokens(policy.maxOutputTokens())
			.setWeeklyLimit(policy.weeklyLimit())
			.setDailyCostLimit(policy.dailyCostLimit())
			.setMonthlyCostLimit(policy.monthlyCostLimit())
			.setDefaultPlan(policy.defaultPlan())
			.build();
	}

	private static Instant parseInstant(String value, Instant fallback) {
		if (value == null || value.isBlank()) {
			return fallback;
		}
		return Instant.parse(value);
	}
}
