package com.sleekydz86.searchai.global.metrics;

import com.sleekydz86.searchai.router.domain.ModelHealth;
import com.sleekydz86.searchai.router.domain.ModelTier;
import com.sleekydz86.searchai.router.domain.RoutingType;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public final class LlmMetricsService {

	private final MeterRegistry registry;
	private final Map<ModelTier, AtomicInteger> circuitState = new EnumMap<>(ModelTier.class);

	public LlmMetricsService(MeterRegistry registry) {
		this.registry = registry;
		for (ModelTier tier : ModelTier.values()) {
			AtomicInteger gauge = new AtomicInteger(0);
			circuitState.put(tier, gauge);
			registry.gauge("llm.circuit.state", io.micrometer.core.instrument.Tags.of("tier", tier.name()), gauge);
		}
	}

	public void recordRequest(boolean success, String model, long durationMs) {
		Counter.builder("llm.request.total").tag("model", model).register(registry).increment();
		if (success) {
			Counter.builder("llm.request.success").tag("model", model).register(registry).increment();
		} else {
			Counter.builder("llm.request.failure").tag("model", model).register(registry).increment();
		}
		Timer.builder("llm.request.duration").tag("model", model).register(registry)
			.record(durationMs, TimeUnit.MILLISECONDS);
	}

	public void recordTokens(String model, long input, long output) {
		Counter.builder("llm.tokens.input").tag("model", model).register(registry).increment(input);
		Counter.builder("llm.tokens.output").tag("model", model).register(registry).increment(output);
	}

	public void recordCost(String model, double cost) {
		Counter.builder("llm.cost.total").tag("model", model).register(registry).increment(cost);
	}

	public void recordCache(boolean hit) {
		if (hit) {
			Counter.builder("llm.cache.hit").register(registry).increment();
		} else {
			Counter.builder("llm.cache.miss").register(registry).increment();
		}
	}

	public void recordRouting(RoutingType type) {
		String name = switch (type) {
			case RULE -> "llm.routing.rule";
			case JEV -> "llm.routing.jev";
			case FALLBACK -> "llm.routing.fallback";
			case MANUAL -> "llm.routing.manual";
		};
		Counter.builder(name).register(registry).increment();
	}

	public void updateCircuit(ModelTier tier, ModelHealth health) {
		int value = switch (health) {
			case AVAILABLE -> 0;
			case DEGRADED -> 1;
			case UNAVAILABLE -> 2;
		};
		circuitState.get(tier).set(value);
	}

	public void recordBudgetWarning() {
		Counter.builder("llm.budget.warning").register(registry).increment();
	}

	public void recordBudgetExceeded() {
		Counter.builder("llm.budget.exceeded").register(registry).increment();
	}

	public void recordAnomaly() {
		Counter.builder("llm.anomaly.detected").register(registry).increment();
	}

	public void recordBlocked() {
		Counter.builder("llm.request.blocked").register(registry).increment();
	}

	public void recordDowngrade() {
		Counter.builder("llm.model.downgrade").register(registry).increment();
	}

	public void recordTokensTotal(long total) {
		Counter.builder("llm.tokens.total").register(registry).increment(total);
	}
}
