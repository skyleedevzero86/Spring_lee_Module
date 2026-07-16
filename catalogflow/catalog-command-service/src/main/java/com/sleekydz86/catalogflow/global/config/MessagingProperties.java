package com.sleekydz86.catalogflow.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.messaging")
public class MessagingProperties {

	private String exchangeEvents;
	private String exchangeDeadLetter;
	private String queueEnrichmentResults;
	private String routingKeyPrefix;

	public String getExchangeEvents() {
		return exchangeEvents;
	}

	public void setExchangeEvents(String exchangeEvents) {
		this.exchangeEvents = exchangeEvents;
	}

	public String getExchangeDeadLetter() {
		return exchangeDeadLetter;
	}

	public void setExchangeDeadLetter(String exchangeDeadLetter) {
		this.exchangeDeadLetter = exchangeDeadLetter;
	}

	public String getQueueEnrichmentResults() {
		return queueEnrichmentResults;
	}

	public void setQueueEnrichmentResults(String queueEnrichmentResults) {
		this.queueEnrichmentResults = queueEnrichmentResults;
	}

	public String getRoutingKeyPrefix() {
		return routingKeyPrefix;
	}

	public void setRoutingKeyPrefix(String routingKeyPrefix) {
		this.routingKeyPrefix = routingKeyPrefix;
	}
}
