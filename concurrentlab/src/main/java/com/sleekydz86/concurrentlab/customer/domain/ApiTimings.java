package com.sleekydz86.concurrentlab.customer.domain;

public record ApiTimings(long profileMillis, long ordersMillis, long recommendationsMillis) {

	public ApiTimings {
		if (profileMillis < 0 || ordersMillis < 0 || recommendationsMillis < 0) {
			throw new IllegalArgumentException("API 소요 시간은 0 이상이어야 합니다");
		}
	}

	public long sequentialEstimate() {
		return profileMillis + ordersMillis + recommendationsMillis;
	}
}
