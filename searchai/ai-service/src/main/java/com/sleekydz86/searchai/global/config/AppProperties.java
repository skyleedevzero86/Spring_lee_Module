package com.sleekydz86.searchai.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
	Ai ai,
	Vector vector,
	Internet internet,
	Proxy proxy,
	Router router,
	Usage usage,
	Cache cache,
	Models models,
	Fallback fallback,
	Resilience resilience,
	CostAware costAware,
	MockAi mockAi,
	Notification notification
) {

	public record Ai(String mode) {
		public boolean isMock() {
			return !"openai".equalsIgnoreCase(mode);
		}
	}

	public record Vector(String mode) {
		public boolean isMemory() {
			return mode == null || mode.isBlank() || "memory".equalsIgnoreCase(mode);
		}

		public boolean isPgVector() {
			return "pgvector".equalsIgnoreCase(mode);
		}

		public boolean isRedis() {
			return "redis".equalsIgnoreCase(mode);
		}
	}

	public record Internet(WebSearch websearch) {
	}

	public record WebSearch(String url, int counts) {
	}

	public record Proxy(boolean enabled, String host, int port) {
	}

	public record Router(String mode, double confidenceThreshold) {
		public double confidenceThresholdOrDefault() {
			return confidenceThreshold <= 0 ? 0.8 : confidenceThreshold;
		}
	}

	public record Usage(
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
		Map<String, PlanDef> plans
	) {
		public long maxOutputTokensOrDefault() {
			return maxOutputTokens <= 0 ? 300 : maxOutputTokens;
		}

		public long perRequestLimitOrDefault() {
			return perRequestLimit <= 0 ? 300 : perRequestLimit;
		}

		public long dailyLimitOrDefault() {
			return dailyLimit <= 0 ? 5000 : dailyLimit;
		}

		public long weeklyLimitOrDefault() {
			return weeklyLimit <= 0 ? 25_000 : weeklyLimit;
		}

		public long monthlyLimitOrDefault() {
			return monthlyLimit <= 0 ? 100_000 : monthlyLimit;
		}

		public double dailyCostLimitOrDefault() {
			return dailyCostLimit <= 0 ? 2.0 : dailyCostLimit;
		}

		public double monthlyCostLimitOrDefault() {
			return monthlyCostLimit <= 0 ? 10.0 : monthlyCostLimit;
		}

		public double softRatioOrDefault() {
			return softRatio <= 0 ? 0.8 : softRatio;
		}

		public double warnRatioOrDefault() {
			return warnRatio <= 0 ? 0.9 : warnRatio;
		}

		public double anomalyMultiplierOrDefault() {
			return anomalyMultiplier <= 0 ? 3.0 : anomalyMultiplier;
		}

		public int rateLimitPerMinuteOrDefault() {
			return rateLimitPerMinute <= 0 ? 30 : rateLimitPerMinute;
		}

		public Map<String, PlanDef> plansOrEmpty() {
			return plans == null ? Map.of() : plans;
		}
	}

	public record PlanDef(
		long maxOutputTokens,
		long dailyLimit,
		long weeklyLimit,
		long monthlyLimit,
		double monthlyCostLimit,
		int rateLimitPerMinute,
		String maxTierCap
	) {
	}

	public record Cache(boolean enabled, long ttlSeconds, String mode) {
		public long ttlSecondsOrDefault() {
			return ttlSeconds <= 0 ? 600 : ttlSeconds;
		}

		public boolean useRedis() {
			return "redis".equalsIgnoreCase(mode);
		}
	}

	public record Models(Map<String, ModelDef> tiers) {
		public Map<String, ModelDef> tiersOrEmpty() {
			return tiers == null ? Map.of() : tiers;
		}
	}

	public record ModelDef(
		String modelName,
		int priority,
		double estimatedInputCost,
		double estimatedOutputCost,
		boolean enabled
	) {
	}

	public record Fallback(Map<String, List<String>> routes) {
		public Map<String, List<String>> routesOrEmpty() {
			return routes == null ? Map.of() : routes;
		}
	}

	public record Resilience(
		Retry retry,
		CircuitBreaker circuitBreaker,
		RateLimiter rateLimiter,
		Bulkhead bulkhead
	) {
	}

	public record Retry(int maxAttempts, long waitDurationMs) {
		public int maxAttemptsOrDefault() {
			return maxAttempts <= 0 ? 3 : maxAttempts;
		}

		public long waitDurationMsOrDefault() {
			return waitDurationMs <= 0 ? 200 : waitDurationMs;
		}
	}

	public record CircuitBreaker(
		float failureRateThreshold,
		int slidingWindowSize,
		long waitDurationInOpenStateMs,
		int permittedNumberOfCallsInHalfOpenState
	) {
		public float failureRateThresholdOrDefault() {
			return failureRateThreshold <= 0 ? 50f : failureRateThreshold;
		}

		public int slidingWindowSizeOrDefault() {
			return slidingWindowSize <= 0 ? 10 : slidingWindowSize;
		}

		public long waitDurationInOpenStateMsOrDefault() {
			return waitDurationInOpenStateMs <= 0 ? 30_000L : waitDurationInOpenStateMs;
		}

		public int halfOpenCallsOrDefault() {
			return permittedNumberOfCallsInHalfOpenState <= 0 ? 3 : permittedNumberOfCallsInHalfOpenState;
		}
	}

	public record RateLimiter(int limitForPeriod, long limitRefreshPeriodMs) {
		public int limitForPeriodOrDefault() {
			return limitForPeriod <= 0 ? 40 : limitForPeriod;
		}

		public long limitRefreshPeriodMsOrDefault() {
			return limitRefreshPeriodMs <= 0 ? 1000 : limitRefreshPeriodMs;
		}
	}

	public record Bulkhead(int maxConcurrentCalls, long maxWaitDurationMs) {
		public int maxConcurrentCallsOrDefault() {
			return maxConcurrentCalls <= 0 ? 20 : maxConcurrentCalls;
		}

		public long maxWaitDurationMsOrDefault() {
			return maxWaitDurationMs < 0 ? 0 : maxWaitDurationMs;
		}
	}

	public record CostAware(boolean enabled, double minSuccessRate, String preferCheaperWhen) {
		public boolean enabledOrDefault() {
			return enabled;
		}

		public double minSuccessRateOrDefault() {
			return minSuccessRate <= 0 ? 0.95 : minSuccessRate;
		}
	}

	public record MockAi(String mode) {
		public String modeOrDefault() {
			return mode == null || mode.isBlank() ? "NORMAL" : mode.toUpperCase();
		}
	}

	public record Notification(
		boolean telegramEnabled,
		String telegramBotToken,
		String telegramChatId,
		boolean slackEnabled,
		String slackWebhookUrl,
		boolean relayEnabled,
		String relayUrl,
		boolean emailEnabled,
		String emailTo,
		String emailFrom,
		String emailWebhookUrl
	) {
	}

	public static Map<String, ModelDef> defaultModels() {
		Map<String, ModelDef> map = new LinkedHashMap<>();
		map.put("LUNA", new ModelDef("gpt-5.6-luna", 1, 0.0002, 0.0006, true));
		map.put("TERRA", new ModelDef("gpt-5.6-terra", 2, 0.0005, 0.0015, true));
		map.put("SOL", new ModelDef("gpt-5.6-sol", 3, 0.002, 0.006, true));
		map.put("ASTRA", new ModelDef("gpt-6-astra", 4, 0.008, 0.024, true));
		return map;
	}
}
