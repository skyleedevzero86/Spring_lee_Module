package com.sleekydz86.concurrentlab.global.concurrency;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.sleekydz86.concurrentlab.customer.application.port.out.CustomerLookupStrategy;
import com.sleekydz86.concurrentlab.customer.domain.ExecutionMode;

public final class CustomerLookupStrategyFactory {

	private final Map<ExecutionMode, CustomerLookupStrategy> strategies;

	public CustomerLookupStrategyFactory(List<CustomerLookupStrategy> strategies) {
		this.strategies = strategies.stream()
			.collect(Collectors.toMap(
				CustomerLookupStrategy::mode,
				Function.identity(),
				(left, right) -> {
					throw new IllegalStateException("실행 방식이 중복되었습니다: " + left.mode());
				},
				() -> new EnumMap<>(ExecutionMode.class)
			));
	}

	public CustomerLookupStrategy resolve(ExecutionMode mode) {
		CustomerLookupStrategy strategy = strategies.get(mode);
		if (strategy == null) {
			throw new IllegalArgumentException("등록되지 않은 실행 방식입니다: " + mode);
		}
		return strategy;
	}
}
