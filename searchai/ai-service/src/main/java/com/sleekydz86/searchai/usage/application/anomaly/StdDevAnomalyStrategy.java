package com.sleekydz86.searchai.usage.application.anomaly;

import org.springframework.stereotype.Component;

@Component
public final class StdDevAnomalyStrategy implements AnomalyDetectionStrategy {

	@Override
	public AnomalyResult detect(String username, long todayProjectedTokens, double recentAverage, double stdDev) {
		if (recentAverage <= 0) {
			return AnomalyResult.ok();
		}
		double threshold = recentAverage + 2 * Math.max(stdDev, recentAverage * 0.2);
		if (todayProjectedTokens > threshold) {
			double ratio = todayProjectedTokens / recentAverage;
			return new AnomalyResult(true,
				String.format("평균+2σ 초과 (평균=%.0f, σ=%.0f, 오늘=%d)", recentAverage, stdDev, todayProjectedTokens),
				ratio);
		}
		return AnomalyResult.ok();
	}
}
