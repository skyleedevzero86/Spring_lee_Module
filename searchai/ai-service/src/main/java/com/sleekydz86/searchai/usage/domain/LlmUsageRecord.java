package com.sleekydz86.searchai.usage.domain;

import com.sleekydz86.searchai.router.domain.RoutingType;

import java.time.Instant;

public record LlmUsageRecord(
	String requestId,
	String username,
	RoutingType routingType,
	String selectedModel,
	String actualModel,
	String tier,
	boolean fallbackOccurred,
	RequestType requestType,
	long promptTokens,
	long completionTokens,
	long totalTokens,
	double estimatedCost,
	long latencyMs,
	boolean success,
	String errorType,
	double routingConfidence,
	boolean cached,
	Instant createdAt
) {
	public String model() {
		return actualModel == null || actualModel.isBlank() ? selectedModel : actualModel;
	}

	public String fallbackModel() {
		return fallbackOccurred ? actualModel : null;
	}
}
