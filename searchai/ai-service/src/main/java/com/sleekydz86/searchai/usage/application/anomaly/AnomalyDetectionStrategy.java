package com.sleekydz86.searchai.usage.application.anomaly;

public interface AnomalyDetectionStrategy {

	AnomalyResult detect(String username, long todayProjectedTokens, double recentAverage, double stdDev);

	record AnomalyResult(boolean anomalous, String reason, double ratio) {
		public static AnomalyResult ok() {
			return new AnomalyResult(false, "", 0);
		}
	}
}
