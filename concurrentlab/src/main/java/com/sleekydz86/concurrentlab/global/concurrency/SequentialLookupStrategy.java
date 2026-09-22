package com.sleekydz86.concurrentlab.global.concurrency;

import com.sleekydz86.concurrentlab.customer.application.port.out.CustomerLookupStrategy;
import com.sleekydz86.concurrentlab.customer.application.port.out.CustomerOrderPort;
import com.sleekydz86.concurrentlab.customer.application.port.out.CustomerProfilePort;
import com.sleekydz86.concurrentlab.customer.application.port.out.CustomerRecommendationPort;
import com.sleekydz86.concurrentlab.customer.domain.ApiTimings;
import com.sleekydz86.concurrentlab.customer.domain.CustomerId;
import com.sleekydz86.concurrentlab.customer.domain.CustomerLookupResult;
import com.sleekydz86.concurrentlab.customer.domain.CustomerProfile;
import com.sleekydz86.concurrentlab.customer.domain.ExecutionMode;
import com.sleekydz86.concurrentlab.customer.domain.OrderSummary;
import com.sleekydz86.concurrentlab.customer.domain.Recommendation;
import com.sleekydz86.concurrentlab.customer.domain.TimedResult;
import com.sleekydz86.concurrentlab.global.util.Stopwatch;

import java.util.List;

public final class SequentialLookupStrategy implements CustomerLookupStrategy {

	private final CustomerProfilePort profilePort;
	private final CustomerOrderPort orderPort;
	private final CustomerRecommendationPort recommendationPort;

	public SequentialLookupStrategy(
		CustomerProfilePort profilePort,
		CustomerOrderPort orderPort,
		CustomerRecommendationPort recommendationPort
	) {
		this.profilePort = profilePort;
		this.orderPort = orderPort;
		this.recommendationPort = recommendationPort;
	}

	@Override
	public ExecutionMode mode() {
		return ExecutionMode.SEQUENTIAL;
	}

	@Override
	public CustomerLookupResult execute(CustomerId customerId) {
		Stopwatch total = Stopwatch.start();

		TimedResult<CustomerProfile> profile = profilePort.getProfile(customerId);
		TimedResult<List<OrderSummary>> orders = orderPort.getOrders(customerId);
		TimedResult<List<Recommendation>> recommendations = recommendationPort.getRecommendations(customerId);

		return new CustomerLookupResult(
			customerId,
			mode(),
			profile.value(),
			orders.value(),
			recommendations.value(),
			new ApiTimings(profile.elapsedMillis(), orders.elapsedMillis(), recommendations.elapsedMillis()),
			total.elapsedMillis()
		);
	}
}
