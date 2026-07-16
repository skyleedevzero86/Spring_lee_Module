package com.sleekydz86.catalogflow.eventcontract;

public final class MessagingHeaders {

	public static final String EVENT_TYPE = "eventType";
	public static final String AGGREGATE_ID = "aggregateId";
	public static final String CORRELATION_ID = "correlationId";
	public static final String TRACE_ID = "traceId";
	public static final String RETRY_COUNT = "x-retry-count";
	public static final String ORIGINAL_ROUTING_KEY = "x-original-routing-key";

	private MessagingHeaders() {
	}
}
