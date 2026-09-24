package com.sleekydz86.searchai.global.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "llm_usage", indexes = {
	@Index(name = "idx_llm_usage_created", columnList = "createdAt"),
	@Index(name = "idx_llm_usage_user_created", columnList = "username,createdAt"),
	@Index(name = "idx_llm_usage_model_created", columnList = "model,createdAt")
})
public class LlmUsageJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 64)
	private String requestId;

	@Column(nullable = false, length = 80)
	private String username;

	@Column(length = 20)
	private String routingType;

	@Column(nullable = false, length = 80)
	private String model;

	@Column(length = 80)
	private String selectedModel;

	@Column(length = 80)
	private String actualModel;

	@Column(nullable = false, length = 20)
	private String tier;

	@Column(nullable = false)
	private boolean fallbackOccurred;

	@Column(length = 80)
	private String fallbackModel;

	@Column(nullable = false, length = 20)
	private String requestType;

	@Column(nullable = false)
	private long promptTokens;

	@Column(nullable = false)
	private long completionTokens;

	@Column(nullable = false)
	private long totalTokens;

	@Column(nullable = false)
	private double estimatedCost;

	@Column(nullable = false)
	private long latencyMs;

	@Column(nullable = false)
	private boolean success;

	@Column(length = 80)
	private String errorType;

	@Column
	private double routingConfidence;

	@Column(nullable = false)
	private boolean cached;

	@Column(nullable = false)
	private Instant createdAt;

	public Long getId() {
		return id;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getRoutingType() {
		return routingType;
	}

	public void setRoutingType(String routingType) {
		this.routingType = routingType;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getSelectedModel() {
		return selectedModel;
	}

	public void setSelectedModel(String selectedModel) {
		this.selectedModel = selectedModel;
	}

	public String getActualModel() {
		return actualModel;
	}

	public void setActualModel(String actualModel) {
		this.actualModel = actualModel;
	}

	public String getTier() {
		return tier;
	}

	public void setTier(String tier) {
		this.tier = tier;
	}

	public boolean isFallbackOccurred() {
		return fallbackOccurred;
	}

	public void setFallbackOccurred(boolean fallbackOccurred) {
		this.fallbackOccurred = fallbackOccurred;
	}

	public String getFallbackModel() {
		return fallbackModel;
	}

	public void setFallbackModel(String fallbackModel) {
		this.fallbackModel = fallbackModel;
	}

	public String getRequestType() {
		return requestType;
	}

	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}

	public long getPromptTokens() {
		return promptTokens;
	}

	public void setPromptTokens(long promptTokens) {
		this.promptTokens = promptTokens;
	}

	public long getCompletionTokens() {
		return completionTokens;
	}

	public void setCompletionTokens(long completionTokens) {
		this.completionTokens = completionTokens;
	}

	public long getTotalTokens() {
		return totalTokens;
	}

	public void setTotalTokens(long totalTokens) {
		this.totalTokens = totalTokens;
	}

	public double getEstimatedCost() {
		return estimatedCost;
	}

	public void setEstimatedCost(double estimatedCost) {
		this.estimatedCost = estimatedCost;
	}

	public long getLatencyMs() {
		return latencyMs;
	}

	public void setLatencyMs(long latencyMs) {
		this.latencyMs = latencyMs;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getErrorType() {
		return errorType;
	}

	public void setErrorType(String errorType) {
		this.errorType = errorType;
	}

	public double getRoutingConfidence() {
		return routingConfidence;
	}

	public void setRoutingConfidence(double routingConfidence) {
		this.routingConfidence = routingConfidence;
	}

	public boolean isCached() {
		return cached;
	}

	public void setCached(boolean cached) {
		this.cached = cached;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}
}
