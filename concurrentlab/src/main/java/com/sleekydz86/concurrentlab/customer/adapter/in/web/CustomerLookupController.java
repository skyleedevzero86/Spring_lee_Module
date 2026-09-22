package com.sleekydz86.concurrentlab.customer.adapter.in.web;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sleekydz86.concurrentlab.customer.application.port.in.LookupCustomerUseCase;
import com.sleekydz86.concurrentlab.customer.domain.CustomerId;
import com.sleekydz86.concurrentlab.customer.domain.ExecutionMode;

@RestController
@RequestMapping("/api/customers")
public class CustomerLookupController {

	private final LookupCustomerUseCase lookupCustomerUseCase;

	public CustomerLookupController(LookupCustomerUseCase lookupCustomerUseCase) {
		this.lookupCustomerUseCase = lookupCustomerUseCase;
	}

	@PostMapping("/{customerId}/lookup")
	public CustomerLookupResponse lookup(
		@PathVariable long customerId,
		@RequestParam String mode
	) {
		return CustomerLookupResponse.from(
			lookupCustomerUseCase.lookup(CustomerId.of(customerId), ExecutionMode.from(mode))
		);
	}
}
