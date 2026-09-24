package com.sleekydz86.searchai.router.application.service;

import com.sleekydz86.searchai.global.config.AppProperties;
import com.sleekydz86.searchai.router.domain.ModelHealth;
import com.sleekydz86.searchai.router.domain.ModelTier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Service
public final class ModelHealthRegistry {

	private static final Logger log = LoggerFactory.getLogger(ModelHealthRegistry.class);

	private final Map<ModelTier, AtomicInteger> failures = new EnumMap<>(ModelTier.class);
	private final Map<ModelTier, AtomicInteger> calls = new EnumMap<>(ModelTier.class);
	private final Map<ModelTier, AtomicLong> openUntil = new EnumMap<>(ModelTier.class);
	private final long openDurationMs;
	private final float failureRateThreshold;
	private final int windowSize;

	public ModelHealthRegistry(AppProperties appProperties) {
		AppProperties.CircuitBreaker cb = appProperties.resilience() == null
			? null
			: appProperties.resilience().circuitBreaker();
		this.openDurationMs = cb == null ? 30_000L : cb.waitDurationInOpenStateMsOrDefault();
		this.failureRateThreshold = cb == null ? 50f : cb.failureRateThresholdOrDefault();
		this.windowSize = cb == null ? 10 : cb.slidingWindowSizeOrDefault();
		for (ModelTier tier : ModelTier.values()) {
			failures.put(tier, new AtomicInteger());
			calls.put(tier, new AtomicInteger());
			openUntil.put(tier, new AtomicLong());
		}
	}

	public ModelHealth health(ModelTier tier) {
		long until = openUntil.get(tier).get();
		if (until > System.currentTimeMillis()) {
			return ModelHealth.UNAVAILABLE;
		}
		int total = calls.get(tier).get();
		int fail = failures.get(tier).get();
		if (total >= 3 && fail * 100.0 / total >= failureRateThreshold * 0.6) {
			return ModelHealth.DEGRADED;
		}
		return ModelHealth.AVAILABLE;
	}

	public boolean isRoutable(ModelTier tier) {
		return health(tier) != ModelHealth.UNAVAILABLE;
	}

	public void onSuccess(ModelTier tier) {
		calls.get(tier).incrementAndGet();
		failures.get(tier).set(0);
		openUntil.get(tier).set(0);
	}

	public void onFailure(ModelTier tier) {
		int total = calls.get(tier).incrementAndGet();
		int fail = failures.get(tier).incrementAndGet();
		if (total >= Math.max(5, windowSize / 2) && fail * 100.0 / total >= failureRateThreshold) {
			openUntil.get(tier).set(System.currentTimeMillis() + openDurationMs);
			log.warn("모델 {} Circuit OPEN (실패율 높음, {}ms 차단)", tier, openDurationMs);
			failures.get(tier).set(0);
			calls.get(tier).set(0);
		}
	}

	public ModelTier nearestAvailable(ModelTier preferred) {
		if (isRoutable(preferred)) {
			return preferred;
		}
		for (int i = preferred.ordinal() - 1; i >= 0; i--) {
			ModelTier candidate = ModelTier.values()[i];
			if (isRoutable(candidate)) {
				return candidate;
			}
		}
		for (int i = preferred.ordinal() + 1; i < ModelTier.values().length; i++) {
			ModelTier candidate = ModelTier.values()[i];
			if (isRoutable(candidate)) {
				return candidate;
			}
		}
		return preferred;
	}
}
