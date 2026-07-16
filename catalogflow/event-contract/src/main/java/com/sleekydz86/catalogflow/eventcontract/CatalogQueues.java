package com.sleekydz86.catalogflow.eventcontract;

public final class CatalogQueues {

	public static final String QUERY_PRODUCT_EVENTS = "catalog-query.product-events";
	public static final String QUERY_PRODUCT_EVENTS_RETRY = "catalog-query.product-events.retry";
	public static final String AI_ENRICHMENT_REQUESTS = "ai-worker.enrichment-requests";
	public static final String COMMAND_ENRICHMENT_RESULTS = "catalog-command.enrichment-results";
	public static final String BATCH_RETRY = "catalog-batch.retry";
	public static final String DEAD_LETTER = "catalog.dead-letter";

	private CatalogQueues() {
	}
}
