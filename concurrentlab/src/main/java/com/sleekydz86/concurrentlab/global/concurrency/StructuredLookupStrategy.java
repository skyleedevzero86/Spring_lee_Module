package com.sleekydz86.concurrentlab.global.concurrency;

import java.util.List;
import java.util.concurrent.StructuredTaskScope;

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
import com.sleekydz86.concurrentlab.global.exception.LookupFailedException;
import com.sleekydz86.concurrentlab.global.util.Stopwatch;

public final class StructuredLookupStrategy implements CustomerLookupStrategy {

	private final CustomerProfilePort profilePort;
	private final CustomerOrderPort orderPort;
	private final CustomerRecommendationPort recommendationPort;

	public StructuredLookupStrategy(
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
		return ExecutionMode.STRUCTURED;
	}

	@Override
	public CustomerLookupResult execute(CustomerId customerId) {
		Stopwatch total = Stopwatch.start();
		try (var scope = StructuredTaskScope.open()) {
			StructuredTaskScope.Subtask<TimedResult<CustomerProfile>> profileTask =
				scope.fork(() -> profilePort.getProfile(customerId));
			StructuredTaskScope.Subtask<TimedResult<List<OrderSummary>>> ordersTask =
				scope.fork(() -> orderPort.getOrders(customerId));
			StructuredTaskScope.Subtask<TimedResult<List<Recommendation>>> recommendationsTask =
				scope.fork(() -> recommendationPort.getRecommendations(customerId));

			scope.join();

			TimedResult<CustomerProfile> profile = profileTask.get();
			TimedResult<List<OrderSummary>> orders = ordersTask.get();
			TimedResult<List<Recommendation>> recommendations = recommendationsTask.get();

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
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new LookupFailedException("구조화 동시성 조회가 중단되었습니다", ex);
		}
		catch (Exception ex) {
			throw new LookupFailedException("구조화 동시성 조회에 실패했습니다", ex);
		}
	}
}
