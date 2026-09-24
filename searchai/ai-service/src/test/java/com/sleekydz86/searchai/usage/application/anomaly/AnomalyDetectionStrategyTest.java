package com.sleekydz86.searchai.usage.application.anomaly;

import com.sleekydz86.searchai.usage.application.anomaly.AnomalyDetectionStrategy.AnomalyResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AnomalyDetectionStrategyTest {

	@Test
	void multiplierDetectsSpike() {
		MultiplierAnomalyStrategy strategy = new MultiplierAnomalyStrategy(3.0);
		AnomalyResult result = strategy.detect("u1", 15_000, 3_000, 500);
		assertThat(result.anomalous()).isTrue();
		assertThat(result.ratio()).isGreaterThanOrEqualTo(5.0);
	}

	@Test
	void stdDevDetectsOutlier() {
		StdDevAnomalyStrategy strategy = new StdDevAnomalyStrategy();
		AnomalyResult result = strategy.detect("u1", 20_000, 3_000, 500);
		assertThat(result.anomalous()).isTrue();
	}
}
