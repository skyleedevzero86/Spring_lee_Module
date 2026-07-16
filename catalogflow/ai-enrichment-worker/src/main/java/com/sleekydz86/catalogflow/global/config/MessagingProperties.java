package com.sleekydz86.catalogflow.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.messaging")
public class MessagingProperties {

	private String exchangeEvents;
	private String exchangeCommands;
	private String exchangeDeadLetter;
	private String queueAiEnrichmentRequests;
	private String queueDeadLetter;
	private String routingKeyPrefix;
	private int consumerPrefetch = 10;
	private int retryMaxAttempts = 3;
	private long retryInitialDelayMs = 3000;
	private double retryMultiplier = 2.0;
	private long publisherConfirmTimeoutMs = 5000;
	private boolean consumerEnabled = true;

	public String getExchangeEvents() {
		return exchangeEvents;
	}

	public void setExchangeEvents(String exchangeEvents) {
		this.exchangeEvents = exchangeEvents;
	}

	public String getExchangeCommands() {
		return exchangeCommands;
	}

	public void setExchangeCommands(String exchangeCommands) {
		this.exchangeCommands = exchangeCommands;
	}

	public String getExchangeDeadLetter() {
		return exchangeDeadLetter;
	}

	public void setExchangeDeadLetter(String exchangeDeadLetter) {
		this.exchangeDeadLetter = exchangeDeadLetter;
	}

	public String getQueueAiEnrichmentRequests() {
		return queueAiEnrichmentRequests;
	}

	public void setQueueAiEnrichmentRequests(String queueAiEnrichmentRequests) {
		this.queueAiEnrichmentRequests = queueAiEnrichmentRequests;
	}

	public String getQueueDeadLetter() {
		return queueDeadLetter;
	}

	public void setQueueDeadLetter(String queueDeadLetter) {
		this.queueDeadLetter = queueDeadLetter;
	}

	public String getRoutingKeyPrefix() {
		return routingKeyPrefix;
	}

	public void setRoutingKeyPrefix(String routingKeyPrefix) {
		this.routingKeyPrefix = routingKeyPrefix;
	}

	public int getConsumerPrefetch() {
		return consumerPrefetch;
	}

	public void setConsumerPrefetch(int consumerPrefetch) {
		this.consumerPrefetch = consumerPrefetch;
	}

	public int getRetryMaxAttempts() {
		return retryMaxAttempts;
	}

	public void setRetryMaxAttempts(int retryMaxAttempts) {
		this.retryMaxAttempts = retryMaxAttempts;
	}

	public long getRetryInitialDelayMs() {
		return retryInitialDelayMs;
	}

	public void setRetryInitialDelayMs(long retryInitialDelayMs) {
		this.retryInitialDelayMs = retryInitialDelayMs;
	}

	public double getRetryMultiplier() {
		return retryMultiplier;
	}

	public void setRetryMultiplier(double retryMultiplier) {
		this.retryMultiplier = retryMultiplier;
	}

	public long getPublisherConfirmTimeoutMs() {
		return publisherConfirmTimeoutMs;
	}

	public void setPublisherConfirmTimeoutMs(long publisherConfirmTimeoutMs) {
		this.publisherConfirmTimeoutMs = publisherConfirmTimeoutMs;
	}

	public boolean isConsumerEnabled() {
		return consumerEnabled;
	}

	public void setConsumerEnabled(boolean consumerEnabled) {
		this.consumerEnabled = consumerEnabled;
	}
}
