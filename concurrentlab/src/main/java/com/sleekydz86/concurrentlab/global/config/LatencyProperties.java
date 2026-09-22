package com.sleekydz86.concurrentlab.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "concurrentlab.latency")
public record LatencyProperties(
	long profileMillis,
	long ordersMillis,
	long recommendationsMillis
) {

	public LatencyProperties {
		if (profileMillis < 0 || ordersMillis < 0 || recommendationsMillis < 0) {
			throw new IllegalArgumentException("지연 시간은 0 이상이어야 합니다");
		}
	}
}
