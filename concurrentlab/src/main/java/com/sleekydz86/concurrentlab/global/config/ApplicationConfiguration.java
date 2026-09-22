package com.sleekydz86.concurrentlab.global.config;

import java.util.List;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sleekydz86.concurrentlab.customer.application.port.in.LookupCustomerUseCase;
import com.sleekydz86.concurrentlab.customer.application.port.out.CustomerLookupStrategy;
import com.sleekydz86.concurrentlab.customer.application.port.out.CustomerOrderPort;
import com.sleekydz86.concurrentlab.customer.application.port.out.CustomerProfilePort;
import com.sleekydz86.concurrentlab.customer.application.port.out.CustomerRecommendationPort;
import com.sleekydz86.concurrentlab.customer.application.service.LookupCustomerService;
import com.sleekydz86.concurrentlab.global.concurrency.CustomerLookupStrategyFactory;
import com.sleekydz86.concurrentlab.global.concurrency.SequentialLookupStrategy;
import com.sleekydz86.concurrentlab.global.concurrency.StructuredLookupStrategy;

@Configuration
@EnableConfigurationProperties(LatencyProperties.class)
public class ApplicationConfiguration {

	@Bean
	CustomerLookupStrategy sequentialLookupStrategy(
		CustomerProfilePort profilePort,
		CustomerOrderPort orderPort,
		CustomerRecommendationPort recommendationPort
	) {
		return new SequentialLookupStrategy(profilePort, orderPort, recommendationPort);
	}

	@Bean
	CustomerLookupStrategy structuredLookupStrategy(
		CustomerProfilePort profilePort,
		CustomerOrderPort orderPort,
		CustomerRecommendationPort recommendationPort
	) {
		return new StructuredLookupStrategy(profilePort, orderPort, recommendationPort);
	}

	@Bean
	CustomerLookupStrategyFactory customerLookupStrategyFactory(List<CustomerLookupStrategy> strategies) {
		return new CustomerLookupStrategyFactory(strategies);
	}

	@Bean
	LookupCustomerUseCase lookupCustomerUseCase(CustomerLookupStrategyFactory strategyFactory) {
		return new LookupCustomerService(strategyFactory);
	}
}
