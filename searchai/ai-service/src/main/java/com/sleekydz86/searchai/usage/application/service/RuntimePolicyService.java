package com.sleekydz86.searchai.usage.application.service;

import com.sleekydz86.searchai.global.config.AppProperties;
import com.sleekydz86.searchai.global.persistence.PolicyAuditJpaEntity;
import com.sleekydz86.searchai.global.persistence.PolicyAuditJpaRepository;
import com.sleekydz86.searchai.global.persistence.RuntimePolicyJpaEntity;
import com.sleekydz86.searchai.global.persistence.RuntimePolicyJpaRepository;
import com.sleekydz86.searchai.usage.domain.RuntimePolicy;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public final class RuntimePolicyService {

	private final RuntimePolicyJpaRepository repository;
	private final PolicyAuditJpaRepository auditRepository;
	private final AppProperties appProperties;
	private volatile RuntimePolicy current;

	public RuntimePolicyService(
		RuntimePolicyJpaRepository repository,
		PolicyAuditJpaRepository auditRepository,
		AppProperties appProperties
	) {
		this.repository = repository;
		this.auditRepository = auditRepository;
		this.appProperties = appProperties;
	}

	@PostConstruct
	@Transactional
	void init() {
		current = repository.findById(1L)
			.map(this::toDomain)
			.orElseGet(() -> {
				RuntimePolicy defaults = defaultsFromYaml();
				saveEntity(defaults);
				return defaults;
			});
	}

	public RuntimePolicy current() {
		return current;
	}

	@Transactional
	public RuntimePolicy update(RuntimePolicy policy, String adminId) {
		RuntimePolicy before = current;
		RuntimePolicy normalized = normalize(policy);
		saveEntity(normalized);
		saveAudit(adminId == null ? "system" : adminId, before, normalized);
		current = normalized;
		return current;
	}

	@Transactional
	public RuntimePolicy update(RuntimePolicy policy) {
		return update(policy, "admin");
	}

	private void saveAudit(String adminId, RuntimePolicy before, RuntimePolicy after) {
		PolicyAuditJpaEntity entity = new PolicyAuditJpaEntity();
		entity.setAdminId(adminId);
		entity.setPolicyType("RUNTIME_POLICY");
		entity.setTargetUser("*");
		entity.setBeforeValue(String.valueOf(before));
		entity.setAfterValue(String.valueOf(after));
		entity.setChangedAt(Instant.now());
		auditRepository.save(entity);
	}

	private RuntimePolicy defaultsFromYaml() {
		AppProperties.Usage usage = appProperties.usage();
		AppProperties.Router router = appProperties.router();
		AppProperties.Cache cache = appProperties.cache();
		return new RuntimePolicy(
			usage == null ? 300 : usage.maxOutputTokensOrDefault(),
			usage == null ? 300 : usage.perRequestLimitOrDefault(),
			usage == null ? 5000 : usage.dailyLimitOrDefault(),
			usage == null ? 25_000 : usage.weeklyLimitOrDefault(),
			usage == null ? 100_000 : usage.monthlyLimitOrDefault(),
			usage == null ? 2.0 : usage.dailyCostLimitOrDefault(),
			usage == null ? 10.0 : usage.monthlyCostLimitOrDefault(),
			usage == null ? 0.8 : usage.softRatioOrDefault(),
			usage == null ? 0.9 : usage.warnRatioOrDefault(),
			usage == null ? 3.0 : usage.anomalyMultiplierOrDefault(),
			usage == null ? 30 : usage.rateLimitPerMinuteOrDefault(),
			router == null || router.mode() == null ? "heuristic" : router.mode(),
			router == null ? 0.8 : router.confidenceThresholdOrDefault(),
			"ASTRA",
			cache == null || cache.enabled(),
			"PRO"
		);
	}

	private void saveEntity(RuntimePolicy policy) {
		RuntimePolicyJpaEntity entity = repository.findById(1L).orElseGet(RuntimePolicyJpaEntity::new);
		entity.setId(1L);
		entity.setMaxOutputTokens(policy.maxOutputTokens());
		entity.setPerRequestLimit(policy.perRequestLimit());
		entity.setDailyLimit(policy.dailyLimit());
		entity.setWeeklyLimit(policy.weeklyLimit());
		entity.setMonthlyLimit(policy.monthlyLimit());
		entity.setDailyCostLimit(policy.dailyCostLimit());
		entity.setMonthlyCostLimit(policy.monthlyCostLimit());
		entity.setSoftRatio(policy.softRatio());
		entity.setWarnRatio(policy.warnRatio());
		entity.setAnomalyMultiplier(policy.anomalyMultiplier());
		entity.setRateLimitPerMinute(policy.rateLimitPerMinute());
		entity.setRouterMode(policy.routerMode());
		entity.setConfidenceThreshold(policy.confidenceThreshold());
		entity.setMaxTierCap(policy.maxTierCap());
		entity.setCacheEnabled(policy.cacheEnabled());
		entity.setDefaultPlan(policy.defaultPlan());
		repository.save(entity);
	}

	private RuntimePolicy toDomain(RuntimePolicyJpaEntity entity) {
		return new RuntimePolicy(
			entity.getMaxOutputTokens() <= 0 ? 300 : entity.getMaxOutputTokens(),
			entity.getPerRequestLimit(),
			entity.getDailyLimit(),
			entity.getWeeklyLimit() <= 0 ? 25_000 : entity.getWeeklyLimit(),
			entity.getMonthlyLimit(),
			entity.getDailyCostLimit() <= 0 ? 2.0 : entity.getDailyCostLimit(),
			entity.getMonthlyCostLimit() <= 0 ? 10.0 : entity.getMonthlyCostLimit(),
			entity.getSoftRatio(),
			entity.getWarnRatio(),
			entity.getAnomalyMultiplier(),
			entity.getRateLimitPerMinute(),
			entity.getRouterMode(),
			entity.getConfidenceThreshold(),
			entity.getMaxTierCap(),
			entity.isCacheEnabled(),
			entity.getDefaultPlan() == null ? "PRO" : entity.getDefaultPlan()
		);
	}

	private static RuntimePolicy normalize(RuntimePolicy policy) {
		return new RuntimePolicy(
			Math.max(1, policy.maxOutputTokens()),
			Math.max(1, policy.perRequestLimit()),
			Math.max(1, policy.dailyLimit()),
			Math.max(1, policy.weeklyLimit()),
			Math.max(1, policy.monthlyLimit()),
			Math.max(0.01, policy.dailyCostLimit()),
			Math.max(0.01, policy.monthlyCostLimit()),
			clamp(policy.softRatio(), 0.1, 0.99),
			clamp(policy.warnRatio(), 0.1, 0.99),
			Math.max(1.0, policy.anomalyMultiplier()),
			Math.max(1, policy.rateLimitPerMinute()),
			policy.routerMode() == null || policy.routerMode().isBlank() ? "heuristic" : policy.routerMode(),
			clamp(policy.confidenceThreshold(), 0.1, 1.0),
			policy.maxTierCap() == null || policy.maxTierCap().isBlank() ? "ASTRA" : policy.maxTierCap().toUpperCase(),
			policy.cacheEnabled(),
			policy.defaultPlan() == null || policy.defaultPlan().isBlank() ? "PRO" : policy.defaultPlan().toUpperCase()
		);
	}

	private static double clamp(double value, double min, double max) {
		return Math.max(min, Math.min(max, value));
	}
}
