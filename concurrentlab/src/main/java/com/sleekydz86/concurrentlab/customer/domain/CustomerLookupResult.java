package com.sleekydz86.concurrentlab.customer.domain;

import java.util.List;

public record CustomerLookupResult(
	CustomerId customerId,
	ExecutionMode mode,
	CustomerProfile profile,
	List<OrderSummary> orders,
	List<Recommendation> recommendations,
	ApiTimings apiTimings,
	long totalElapsedMillis
) {

	public CustomerLookupResult {
		if (customerId == null) {
			throw new IllegalArgumentException("고객 번호가 필요합니다");
		}
		if (mode == null) {
			throw new IllegalArgumentException("실행 방식이 필요합니다");
		}
		if (profile == null) {
			throw new IllegalArgumentException("프로필이 필요합니다");
		}
		if (orders == null) {
			throw new IllegalArgumentException("주문 내역이 필요합니다");
		}
		if (recommendations == null) {
			throw new IllegalArgumentException("추천 상품이 필요합니다");
		}
		if (apiTimings == null) {
			throw new IllegalArgumentException("API 시간 정보가 필요합니다");
		}
		if (totalElapsedMillis < 0) {
			throw new IllegalArgumentException("총 처리 시간은 0 이상이어야 합니다");
		}
		orders = List.copyOf(orders);
		recommendations = List.copyOf(recommendations);
	}
}
