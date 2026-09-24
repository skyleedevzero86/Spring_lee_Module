package com.sleekydz86.searchai.global.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "runtime_policy")
public class RuntimePolicyJpaEntity {

	@Id
	private Long id = 1L;

	@Column(nullable = false)
	private long maxOutputTokens = 300;

	@Column(nullable = false)
	private long perRequestLimit;

	@Column(nullable = false)
	private long dailyLimit;

	@Column(nullable = false)
	private long weeklyLimit = 25_000;

	@Column(nullable = false)
	private long monthlyLimit;

	@Column(nullable = false)
	private double dailyCostLimit = 2.0;

	@Column(nullable = false)
	private double monthlyCostLimit = 10.0;

	@Column(nullable = false)
	private double softRatio;

	@Column(nullable = false)
	private double warnRatio;

	@Column(nullable = false)
	private double anomalyMultiplier;

	@Column(nullable = false)
	private int rateLimitPerMinute;

	@Column(nullable = false, length = 20)
	private String routerMode;

	@Column(nullable = false)
	private double confidenceThreshold;

	@Column(nullable = false, length = 20)
	private String maxTierCap;

	@Column(nullable = false)
	private boolean cacheEnabled;

	@Column(nullable = false, length = 20)
	private String defaultPlan = "PRO";

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public long getMaxOutputTokens() {
		return maxOutputTokens;
	}

	public void setMaxOutputTokens(long maxOutputTokens) {
		this.maxOutputTokens = maxOutputTokens;
	}

	public long getPerRequestLimit() {
		return perRequestLimit;
	}

	public void setPerRequestLimit(long perRequestLimit) {
		this.perRequestLimit = perRequestLimit;
	}

	public long getDailyLimit() {
		return dailyLimit;
	}

	public void setDailyLimit(long dailyLimit) {
		this.dailyLimit = dailyLimit;
	}

	public long getWeeklyLimit() {
		return weeklyLimit;
	}

	public void setWeeklyLimit(long weeklyLimit) {
		this.weeklyLimit = weeklyLimit;
	}

	public long getMonthlyLimit() {
		return monthlyLimit;
	}

	public void setMonthlyLimit(long monthlyLimit) {
		this.monthlyLimit = monthlyLimit;
	}

	public double getDailyCostLimit() {
		return dailyCostLimit;
	}

	public void setDailyCostLimit(double dailyCostLimit) {
		this.dailyCostLimit = dailyCostLimit;
	}

	public double getMonthlyCostLimit() {
		return monthlyCostLimit;
	}

	public void setMonthlyCostLimit(double monthlyCostLimit) {
		this.monthlyCostLimit = monthlyCostLimit;
	}

	public double getSoftRatio() {
		return softRatio;
	}

	public void setSoftRatio(double softRatio) {
		this.softRatio = softRatio;
	}

	public double getWarnRatio() {
		return warnRatio;
	}

	public void setWarnRatio(double warnRatio) {
		this.warnRatio = warnRatio;
	}

	public double getAnomalyMultiplier() {
		return anomalyMultiplier;
	}

	public void setAnomalyMultiplier(double anomalyMultiplier) {
		this.anomalyMultiplier = anomalyMultiplier;
	}

	public int getRateLimitPerMinute() {
		return rateLimitPerMinute;
	}

	public void setRateLimitPerMinute(int rateLimitPerMinute) {
		this.rateLimitPerMinute = rateLimitPerMinute;
	}

	public String getRouterMode() {
		return routerMode;
	}

	public void setRouterMode(String routerMode) {
		this.routerMode = routerMode;
	}

	public double getConfidenceThreshold() {
		return confidenceThreshold;
	}

	public void setConfidenceThreshold(double confidenceThreshold) {
		this.confidenceThreshold = confidenceThreshold;
	}

	public String getMaxTierCap() {
		return maxTierCap;
	}

	public void setMaxTierCap(String maxTierCap) {
		this.maxTierCap = maxTierCap;
	}

	public boolean isCacheEnabled() {
		return cacheEnabled;
	}

	public void setCacheEnabled(boolean cacheEnabled) {
		this.cacheEnabled = cacheEnabled;
	}

	public String getDefaultPlan() {
		return defaultPlan;
	}

	public void setDefaultPlan(String defaultPlan) {
		this.defaultPlan = defaultPlan;
	}
}
