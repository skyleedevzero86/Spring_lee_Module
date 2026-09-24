package com.sleekydz86.searchai.usage.application.anomaly;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public final class MultiplierAnomalyStrategy implements AnomalyDetectionStrategy {

	private final double multiplier;

	public MultiplierAnomalyStrategy(@Value("${app.usage.anomaly-multiplier:3.0}") double multiplier) {
		this.multiplier = multiplier <= 0 ? 3.0 : multiplier;
	}

	@Override
	public AnomalyResult detect(String username, long todayProjectedTokens, double recentAverage, double stdDev) {
		if (recentAverage <= 0) {
			return AnomalyResult.ok();
		}
		double ratio = todayProjectedTokens / recentAverage;
		if (ratio >= multiplier) {
			return new AnomalyResult(true,
				String.format("평균 대비 %.1f배 증가 (임계 %.1fx)", ratio, multiplier),
				ratio);
		}
		return AnomalyResult.ok();
	}
}
