package com.sleekydz86.concurrentlab.customer.application.port.in;

import com.sleekydz86.concurrentlab.customer.domain.CustomerId;
import com.sleekydz86.concurrentlab.customer.domain.CustomerLookupResult;
import com.sleekydz86.concurrentlab.customer.domain.ExecutionMode;

public interface LookupCustomerUseCase {

	CustomerLookupResult lookup(CustomerId customerId, ExecutionMode mode);
}
