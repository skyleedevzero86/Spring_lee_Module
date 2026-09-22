package com.sleekydz86.concurrentlab.customer.application.service;

import com.sleekydz86.concurrentlab.customer.application.port.in.LookupCustomerUseCase;
import com.sleekydz86.concurrentlab.customer.application.port.out.CustomerLookupStrategy;
import com.sleekydz86.concurrentlab.customer.domain.CustomerId;
import com.sleekydz86.concurrentlab.customer.domain.CustomerLookupResult;
import com.sleekydz86.concurrentlab.customer.domain.ExecutionMode;
import com.sleekydz86.concurrentlab.global.concurrency.CustomerLookupStrategyFactory;

public final class LookupCustomerService implements LookupCustomerUseCase {

	private final CustomerLookupStrategyFactory strategyFactory;

	public LookupCustomerService(CustomerLookupStrategyFactory strategyFactory) {
		this.strategyFactory = strategyFactory;
	}

	@Override
	public CustomerLookupResult lookup(CustomerId customerId, ExecutionMode mode) {
		CustomerLookupStrategy strategy = strategyFactory.resolve(mode);
		return strategy.execute(customerId);
	}
}
