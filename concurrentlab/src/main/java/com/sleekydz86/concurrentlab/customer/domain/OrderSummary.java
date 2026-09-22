package com.sleekydz86.concurrentlab.customer.domain;

import java.util.List;

public record OrderSummary(String orderId, String itemName, long amount) {

	public OrderSummary {
		if (orderId == null || orderId.isBlank()) {
			throw new IllegalArgumentException("주문 번호가 필요합니다");
		}
		if (itemName == null || itemName.isBlank()) {
			throw new IllegalArgumentException("상품명이 필요합니다");
		}
		if (amount < 0) {
			throw new IllegalArgumentException("주문 금액은 0 이상이어야 합니다");
		}
	}

	public static List<OrderSummary> sampleFor(CustomerId customerId) {
		return List.of(
			new OrderSummary("ORD-" + customerId.value() + "-1", "무선 이어폰", 89_000),
			new OrderSummary("ORD-" + customerId.value() + "-2", "노트북 파우치", 32_000)
		);
	}
}
