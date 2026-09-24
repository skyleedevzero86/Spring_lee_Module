package com.sleekydz86.searchai.gateway.usage.adapter.in.web;

import com.sleekydz86.searchai.gateway.usage.adapter.out.grpc.GrpcUsageAdapter;
import com.sleekydz86.searchai.proto.AbTestResponse;
import com.sleekydz86.searchai.proto.AlertsResponse;
import com.sleekydz86.searchai.proto.DetailedStatsResponse;
import com.sleekydz86.searchai.proto.ModelCost;
import com.sleekydz86.searchai.proto.NamedCount;
import com.sleekydz86.searchai.proto.RuntimePolicy;
import com.sleekydz86.searchai.proto.TimelineBucket;
import com.sleekydz86.searchai.proto.UsageAlert;
import com.sleekydz86.searchai.proto.UsageSummaryResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/admin")
public final class UsageAdminController {

	private final GrpcUsageAdapter grpcUsageAdapter;

	public UsageAdminController(GrpcUsageAdapter grpcUsageAdapter) {
		this.grpcUsageAdapter = grpcUsageAdapter;
	}

	@GetMapping("/usage/summary")
	@PreAuthorize("hasRole('ADMIN')")
	public Mono<SummaryView> summary(@RequestParam(required = false) String username) {
		return grpcUsageAdapter.summary(username).map(SummaryView::from);
	}

	@GetMapping("/usage/alerts")
	@PreAuthorize("hasRole('ADMIN')")
	public Mono<List<AlertView>> alerts() {
		return grpcUsageAdapter.alerts().map(AlertsResponse::getAlertsList)
			.map(list -> list.stream().map(AlertView::from).toList());
	}

	@GetMapping("/stats")
	@PreAuthorize("hasRole('ADMIN')")
	public Mono<DetailedStatsView> stats(
		@RequestParam(required = false) String from,
		@RequestParam(required = false) String to,
		@RequestParam(defaultValue = "DAY") String granularity,
		@RequestParam(required = false) String username
	) {
		return grpcUsageAdapter.detailedStats(from, to, granularity, username).map(DetailedStatsView::from);
	}

	@GetMapping("/policy")
	@PreAuthorize("hasRole('ADMIN')")
	public Mono<PolicyView> getPolicy() {
		return grpcUsageAdapter.getPolicy().map(PolicyView::from);
	}

	@PutMapping("/policy")
	@PreAuthorize("hasRole('ADMIN')")
	public Mono<PolicyView> updatePolicy(@RequestBody PolicyView body) {
		return grpcUsageAdapter.updatePolicy(body.toProto()).map(PolicyView::from);
	}

	@PostMapping("/abtest")
	@PreAuthorize("hasRole('ADMIN')")
	public Mono<AbTestView> abTest(@RequestBody AbTestRequestBody body) {
		return grpcUsageAdapter.runAbTest(body.prompt(), body.tiers()).map(AbTestView::from);
	}

	public record AbTestRequestBody(String prompt, List<String> tiers) {
	}

	public record AbTestVariantView(
		String tier,
		String model,
		String response,
		long latencyMs,
		long promptTokens,
		long completionTokens,
		long totalTokens,
		double estimatedCost,
		boolean success,
		String error
	) {
	}

	public record AbTestView(String prompt, List<AbTestVariantView> variants) {
		static AbTestView from(AbTestResponse response) {
			return new AbTestView(
				response.getPrompt(),
				response.getVariantsList().stream()
					.map(v -> new AbTestVariantView(
						v.getTier(),
						v.getModel(),
						v.getResponse(),
						v.getLatencyMs(),
						v.getPromptTokens(),
						v.getCompletionTokens(),
						v.getTotalTokens(),
						v.getEstimatedCost(),
						v.getSuccess(),
						v.getError()
					))
					.toList()
			);
		}
	}

	public record SummaryView(
		long todayTokens,
		long monthTokens,
		double todayCost,
		double monthCost,
		long dailyLimit,
		long monthlyLimit,
		long perRequestLimit,
		double softRatio,
		String budgetState,
		List<ModelView> models
	) {
		static SummaryView from(UsageSummaryResponse response) {
			return new SummaryView(
				response.getTodayTokens(),
				response.getMonthTokens(),
				response.getTodayCost(),
				response.getMonthCost(),
				response.getDailyLimit(),
				response.getMonthlyLimit(),
				response.getPerRequestLimit(),
				response.getSoftRatio(),
				response.getBudgetState(),
				response.getModelsList().stream().map(ModelView::from).toList()
			);
		}
	}

	public record ModelView(
		String tier,
		String model,
		long calls,
		long totalTokens,
		double estimatedCost,
		double avgLatencyMs,
		double successRate,
		long successCount,
		long failureCount,
		double p95LatencyMs,
		double avgPromptTokens,
		double avgCompletionTokens,
		long fallbackCount,
		double cacheHitRatio
	) {
		static ModelView from(ModelCost cost) {
			return new ModelView(
				cost.getTier(),
				cost.getModel(),
				cost.getCalls(),
				cost.getTotalTokens(),
				cost.getEstimatedCost(),
				cost.getAvgLatencyMs(),
				cost.getSuccessRate(),
				cost.getSuccessCount(),
				cost.getFailureCount(),
				cost.getP95LatencyMs(),
				cost.getAvgPromptTokens(),
				cost.getAvgCompletionTokens(),
				cost.getFallbackCount(),
				cost.getCacheHitRatio()
			);
		}
	}

	public record AlertView(String code, String message, String severity, String username, long createdAtEpochMs) {
		static AlertView from(UsageAlert alert) {
			return new AlertView(
				alert.getCode(),
				alert.getMessage(),
				alert.getSeverity(),
				alert.getUsername(),
				alert.getCreatedAtEpochMs()
			);
		}
	}

	public record NamedMetricView(String name, long tokens, double cost) {
		static NamedMetricView from(NamedCount count) {
			return new NamedMetricView(count.getName(), count.getCount(), count.getCost());
		}
	}

	public record TimelineView(
		String bucket,
		long calls,
		long tokens,
		double cost,
		long cacheHits,
		double avgLatencyMs
	) {
		static TimelineView from(TimelineBucket bucket) {
			return new TimelineView(
				bucket.getBucket(),
				bucket.getCalls(),
				bucket.getTokens(),
				bucket.getCost(),
				bucket.getCacheHits(),
				bucket.getAvgLatencyMs()
			);
		}
	}

	public record DetailedStatsView(
		String from,
		String to,
		String granularity,
		long totalCalls,
		long totalTokens,
		double totalCost,
		long cacheHits,
		double cacheHitRatio,
		double avgLatencyMs,
		double successRate,
		List<NamedMetricView> byTier,
		List<NamedMetricView> byRequestType,
		List<NamedMetricView> byDayOfWeek,
		List<NamedMetricView> byHour,
		List<NamedMetricView> byUsername,
		List<NamedMetricView> byRoutingType,
		List<TimelineView> timeline,
		List<ModelView> models
	) {
		static DetailedStatsView from(DetailedStatsResponse response) {
			return new DetailedStatsView(
				response.getFromIso(),
				response.getToIso(),
				response.getGranularity(),
				response.getTotalCalls(),
				response.getTotalTokens(),
				response.getTotalCost(),
				response.getCacheHits(),
				response.getCacheHitRatio(),
				response.getAvgLatencyMs(),
				response.getSuccessRate(),
				response.getByTierList().stream().map(NamedMetricView::from).toList(),
				response.getByRequestTypeList().stream().map(NamedMetricView::from).toList(),
				response.getByDayOfWeekList().stream().map(NamedMetricView::from).toList(),
				response.getByHourList().stream().map(NamedMetricView::from).toList(),
				response.getByUsernameList().stream().map(NamedMetricView::from).toList(),
				response.getByRoutingTypeList().stream().map(NamedMetricView::from).toList(),
				response.getTimelineList().stream().map(TimelineView::from).toList(),
				response.getModelsList().stream().map(ModelView::from).toList()
			);
		}
	}

	public record PolicyView(
		long maxOutputTokens,
		long perRequestLimit,
		long dailyLimit,
		long weeklyLimit,
		long monthlyLimit,
		double dailyCostLimit,
		double monthlyCostLimit,
		double softRatio,
		double warnRatio,
		double anomalyMultiplier,
		int rateLimitPerMinute,
		String routerMode,
		double confidenceThreshold,
		String maxTierCap,
		boolean cacheEnabled,
		String defaultPlan
	) {
		static PolicyView from(RuntimePolicy policy) {
			return new PolicyView(
				policy.getMaxOutputTokens(),
				policy.getPerRequestLimit(),
				policy.getDailyLimit(),
				policy.getWeeklyLimit(),
				policy.getMonthlyLimit(),
				policy.getDailyCostLimit(),
				policy.getMonthlyCostLimit(),
				policy.getSoftRatio(),
				policy.getWarnRatio(),
				policy.getAnomalyMultiplier(),
				policy.getRateLimitPerMinute(),
				policy.getRouterMode(),
				policy.getConfidenceThreshold(),
				policy.getMaxTierCap(),
				policy.getCacheEnabled(),
				policy.getDefaultPlan()
			);
		}

		RuntimePolicy toProto() {
			return RuntimePolicy.newBuilder()
				.setMaxOutputTokens(maxOutputTokens <= 0 ? 300 : maxOutputTokens)
				.setPerRequestLimit(perRequestLimit)
				.setDailyLimit(dailyLimit)
				.setWeeklyLimit(weeklyLimit <= 0 ? 25_000 : weeklyLimit)
				.setMonthlyLimit(monthlyLimit)
				.setDailyCostLimit(dailyCostLimit <= 0 ? 2.0 : dailyCostLimit)
				.setMonthlyCostLimit(monthlyCostLimit <= 0 ? 10.0 : monthlyCostLimit)
				.setSoftRatio(softRatio)
				.setWarnRatio(warnRatio)
				.setAnomalyMultiplier(anomalyMultiplier)
				.setRateLimitPerMinute(rateLimitPerMinute)
				.setRouterMode(routerMode == null ? "heuristic" : routerMode)
				.setConfidenceThreshold(confidenceThreshold)
				.setMaxTierCap(maxTierCap == null ? "ASTRA" : maxTierCap)
				.setCacheEnabled(cacheEnabled)
				.setDefaultPlan(defaultPlan == null || defaultPlan.isBlank() ? "PRO" : defaultPlan)
				.build();
		}
	}
}
