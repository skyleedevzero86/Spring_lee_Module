package com.sleekydz86.catalogflow.eventcontract;

import java.util.Set;

public final class CatalogEventTypes {

	public static final String PRODUCT_CREATED = "ProductCreated";
	public static final String PRODUCT_UPDATED = "ProductUpdated";
	public static final String PRODUCT_PRICE_CHANGED = "ProductPriceChanged";
	public static final String PRODUCT_IMAGE_UPLOADED = "ProductImageUploaded";
	public static final String PRODUCT_ENRICHMENT_REQUESTED = "ProductEnrichmentRequested";
	public static final String PRODUCT_ENRICHMENT_COMPLETED = "ProductEnrichmentCompleted";
	public static final String PRODUCT_ENRICHMENT_FAILED = "ProductEnrichmentFailed";
	public static final String PRODUCT_PUBLISHED = "ProductPublished";
	public static final String PRODUCT_SUSPENDED = "ProductSuspended";
	public static final String PRODUCT_DISCONTINUED = "ProductDiscontinued";

	public static final Set<String> QUERY_PRODUCT_EVENT_TYPES = Set.of(
			PRODUCT_CREATED,
			PRODUCT_UPDATED,
			PRODUCT_PRICE_CHANGED,
			PRODUCT_IMAGE_UPLOADED,
			PRODUCT_ENRICHMENT_COMPLETED,
			PRODUCT_PUBLISHED,
			PRODUCT_SUSPENDED,
			PRODUCT_DISCONTINUED);

	public static final Set<String> AI_ENRICHMENT_EVENT_TYPES = Set.of(PRODUCT_ENRICHMENT_REQUESTED);

	public static final Set<String> COMMAND_ENRICHMENT_RESULT_EVENT_TYPES = Set.of(
			PRODUCT_ENRICHMENT_COMPLETED,
			PRODUCT_ENRICHMENT_FAILED);

	private CatalogEventTypes() {
	}
}
