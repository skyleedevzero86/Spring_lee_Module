package com.sleekydz86.searchai.usage.application.service;

import com.sleekydz86.searchai.global.metrics.LlmMetricsService;
import com.sleekydz86.searchai.usage.application.anomaly.AnomalyDetectionStrategy;
import com.sleekydz86.searchai.usage.application.anomaly.MultiplierAnomalyStrategy;
import com.sleekydz86.searchai.usage.application.port.out.NotificationSender;
import com.sleekydz86.searchai.usage.application.port.out.UsageStorePort;
import com.sleekydz86.searchai.usage.domain.LlmUsageRecord;
import com.sleekydz86.searchai.usage.domain.RequestType;
import com.sleekydz86.searchai.usage.domain.RuntimePolicy;
import com.sleekydz86.searchai.usage.domain.UsageAlert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UsagePolicyServiceTest {

	private UsagePolicyService service;
	private FakeStore store;

	@BeforeEach
	void setUp() {
		store = new FakeStore();
		RuntimePolicyService runtimePolicyService = mock(RuntimePolicyService.class);
		when(runtimePolicyService.current()).thenReturn(new RuntimePolicy(
			300, 300, 5000, 25000, 100000, 2.0, 10.0, 0.8, 0.9, 3.0, 100,
			"heuristic", 0.8, "ASTRA", true, "PRO"
		));
		List<AnomalyDetectionStrategy> strategies = List.of(new MultiplierAnomalyStrategy(3.0));
		List<NotificationSender> senders = List.of((title, body, severity) -> {
		});
		service = new UsagePolicyService(store, runtimePolicyService, mock(LlmMetricsService.class), strategies, senders);
	}

	@Test
	void deniesWhenPerRequestLimitExceeded() {
		assertThat(service.authorize("user", RequestType.GENERAL, 400).allowed()).isFalse();
	}

	@Test
	void allowsUnderBudget() {
		assertThat(service.authorize("user", RequestType.GENERAL, 100).allowed()).isTrue();
	}

	@Test
	void softLimitDowngradesMaxTier() {
		for (int i = 0; i < 20; i++) {
			store.save(record(400));
		}
		var decision = service.authorize("user", RequestType.GENERAL, 100);
		assertThat(decision.allowed()).isTrue();
		assertThat(decision.maxAllowedTier().name()).isIn("LUNA", "TERRA", "SOL");
	}

	@Test
	void hardLimitBlocksGeneralButAllowsDb() {
		for (int i = 0; i < 30; i++) {
			store.save(record(500));
		}
		assertThat(service.authorize("user", RequestType.GENERAL, 100).generalBlocked()).isTrue();
		assertThat(service.authorize("user", RequestType.DB, 10).allowed()).isTrue();
		assertThat(service.authorize("user", RequestType.DB, 10).llmRequired()).isFalse();
	}

	private static LlmUsageRecord record(long tokens) {
		return new LlmUsageRecord(
			"r1", "user", com.sleekydz86.searchai.router.domain.RoutingType.JEV,
			"gpt-5.6-sol", "gpt-5.6-sol", "sol", false, RequestType.GENERAL,
			tokens / 2, tokens / 2, tokens, 0.01, 10, true, null, 0.9, false, Instant.now()
		);
	}

	private static final class FakeStore implements UsageStorePort {
		private final CopyOnWriteArrayList<LlmUsageRecord> records = new CopyOnWriteArrayList<>();
		private final CopyOnWriteArrayList<UsageAlert> alerts = new CopyOnWriteArrayList<>();

		@Override
		public void save(LlmUsageRecord record) {
			records.add(record);
		}

		@Override
		public long sumTokensSince(String username, Instant from) {
			return records.stream().filter(r -> r.username().equals(username) && !r.createdAt().isBefore(from))
				.mapToLong(LlmUsageRecord::totalTokens).sum();
		}

		@Override
		public double averageDailyTokens(String username, int days) {
			return 100;
		}

		@Override
		public List<LlmUsageRecord> findSince(Instant from) {
			return records.stream().filter(r -> !r.createdAt().isBefore(from)).toList();
		}

		@Override
		public List<LlmUsageRecord> findBetween(Instant from, Instant to) {
			return records.stream().filter(r -> !r.createdAt().isBefore(from) && !r.createdAt().isAfter(to)).toList();
		}

		@Override
		public List<LlmUsageRecord> findBetween(String username, Instant from, Instant to) {
			return findBetween(from, to).stream().filter(r -> r.username().equals(username)).toList();
		}

		@Override
		public void addAlert(UsageAlert alert) {
			alerts.add(alert);
		}

		@Override
		public List<UsageAlert> recentAlerts(int limit) {
			return new ArrayList<>(alerts.subList(0, Math.min(limit, alerts.size())));
		}
	}
}
