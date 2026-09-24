package com.sleekydz86.searchai.usage.domain;

public record RuntimePolicy(
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
}
