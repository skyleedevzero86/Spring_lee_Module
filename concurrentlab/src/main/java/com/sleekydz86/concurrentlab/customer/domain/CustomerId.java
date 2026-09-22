package com.sleekydz86.concurrentlab.customer.domain;

public record CustomerId(long value) {

	public CustomerId {
		if (value <= 0) {
			throw new IllegalArgumentException("고객 번호는 1 이상이어야 합니다");
		}
	}

	public static CustomerId of(long value) {
		return new CustomerId(value);
	}

	@Override
	public String toString() {
		return Long.toString(value);
	}
}
