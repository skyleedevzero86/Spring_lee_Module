package com.sleekydz86.concurrentlab.global.concurrency;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.sleekydz86.concurrentlab.customer.application.port.out.CustomerOrderPort;
import com.sleekydz86.concurrentlab.customer.application.port.out.CustomerProfilePort;
import com.sleekydz86.concurrentlab.customer.application.port.out.CustomerRecommendationPort;
import com.sleekydz86.concurrentlab.customer.domain.CustomerId;
import com.sleekydz86.concurrentlab.customer.domain.CustomerLookupResult;
import com.sleekydz86.concurrentlab.customer.domain.CustomerProfile;
import com.sleekydz86.concurrentlab.customer.domain.ExecutionMode;
import com.sleekydz86.concurrentlab.customer.domain.OrderSummary;
import com.sleekydz86.concurrentlab.customer.domain.Recommendation;
import com.sleekydz86.concurrentlab.customer.domain.TimedResult;
import com.sleekydz86.concurrentlab.global.util.LatencySimulator;

class LookupStrategyTest {

	private final CustomerId customerId = CustomerId.of(1001);

	@Test
	void sequentialRunsApisOneByOne() {
		ConcurrencyProbe probe = new ConcurrencyProbe();
		CustomerLookupResult result = new SequentialLookupStrategy(
			probe.profile(40),
			probe.orders(60),
			probe.recommendations(50)
		).execute(customerId);

		assertThat(result.mode()).isEqualTo(ExecutionMode.SEQUENTIAL);
		assertThat(result.totalElapsedMillis()).isGreaterThanOrEqualTo(140);
		assertThat(probe.maxConcurrent()).isEqualTo(1);
	}

	@Test
	void structuredRunsApisInParallel() {
		ConcurrencyProbe probe = new ConcurrencyProbe();
		CustomerLookupResult result = new StructuredLookupStrategy(
			probe.profile(40),
			probe.orders(60),
			probe.recommendations(50)
		).execute(customerId);

		assertThat(result.mode()).isEqualTo(ExecutionMode.STRUCTURED);
		assertThat(result.totalElapsedMillis()).isLessThan(140);
		assertThat(result.totalElapsedMillis()).isGreaterThanOrEqualTo(55);
		assertThat(probe.maxConcurrent()).isGreaterThan(1);
	}

	private static final class ConcurrencyProbe {

		private final AtomicInteger inFlight = new AtomicInteger();
		private final AtomicInteger maxConcurrent = new AtomicInteger();

		CustomerProfilePort profile(long delayMillis) {
			return customerId -> track(delayMillis, () -> new CustomerProfile(customerId, "고객", "GOLD"));
		}

		CustomerOrderPort orders(long delayMillis) {
			return customerId -> track(delayMillis, () -> OrderSummary.sampleFor(customerId));
		}

		CustomerRecommendationPort recommendations(long delayMillis) {
			return customerId -> track(delayMillis, () -> Recommendation.sampleFor(customerId));
		}

		int maxConcurrent() {
			return maxConcurrent.get();
		}

		private <T> TimedResult<T> track(long delayMillis, java.util.concurrent.Callable<T> task) {
			int current = inFlight.incrementAndGet();
			maxConcurrent.accumulateAndGet(current, Math::max);
			try {
				return LatencySimulator.call(delayMillis, task);
			}
			finally {
				inFlight.decrementAndGet();
			}
		}
	}
}
