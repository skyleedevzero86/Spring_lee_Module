package com.sleekydz86.catalogflow.adapter.out.messaging;

import com.sleekydz86.catalogflow.eventcontract.CatalogRoutingKeys;
import org.springframework.stereotype.Component;

@Component
public class OutboxRoutingKeyResolver {

	public String resolve(String eventType) {
		return CatalogRoutingKeys.resolve(eventType);
	}
}
