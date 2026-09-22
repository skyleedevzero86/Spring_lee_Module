package com.sleekydz86.concurrentlab.customer.domain;

public record TimedResult<T>(T value, long elapsedMillis) {

	public TimedResult {
		if (value == null) {
			throw new IllegalArgumentException("결과가 필요합니다");
		}
		if (elapsedMillis < 0) {
			throw new IllegalArgumentException("소요 시간은 0 이상이어야 합니다");
		}
	}
}
