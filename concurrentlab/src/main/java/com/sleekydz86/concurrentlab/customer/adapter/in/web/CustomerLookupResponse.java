package com.sleekydz86.concurrentlab.customer.adapter.in.web;

import java.util.List;

import com.sleekydz86.concurrentlab.customer.domain.CustomerLookupResult;
import com.sleekydz86.concurrentlab.customer.domain.OrderSummary;
import com.sleekydz86.concurrentlab.customer.domain.Recommendation;

public record CustomerLookupResponse(
	long customerId,
	String mode,
	String modeLabel,
	ProfileView profile,
	List<OrderView> orders,
	List<RecommendationView> recommendations,
	TimingView timings,
	long totalElapsedMillis
) {

	public static CustomerLookupResponse from(CustomerLookupResult result) {
		return new CustomerLookupResponse(
			result.customerId().value(),
			result.mode().name(),
			result.mode().label(),
			new ProfileView(result.profile().name(), result.profile().grade()),
			result.orders().stream().map(OrderView::from).toList(),
			result.recommendations().stream().map(RecommendationView::from).toList(),
			new TimingView(
				result.apiTimings().profileMillis(),
				result.apiTimings().ordersMillis(),
				result.apiTimings().recommendationsMillis(),
				result.apiTimings().sequentialEstimate()
			),
			result.totalElapsedMillis()
		);
	}

	public record ProfileView(String name, String grade) {
	}

	public record OrderView(String orderId, String itemName, long amount) {

		static OrderView from(OrderSummary order) {
			return new OrderView(order.orderId(), order.itemName(), order.amount());
		}
	}

	public record RecommendationView(String productId, String title, int score) {

		static RecommendationView from(Recommendation recommendation) {
			return new RecommendationView(
				recommendation.productId(),
				recommendation.title(),
				recommendation.score()
			);
		}
	}

	public record TimingView(
		long profileMillis,
		long ordersMillis,
		long recommendationsMillis,
		long sequentialEstimateMillis
	) {
	}
}
