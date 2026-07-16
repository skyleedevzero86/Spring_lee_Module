package com.sleekydz86.catalogflow.eventcontract;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CatalogRoutingKeysTest {

	@Test
	void shouldResolveProductCreatedRoutingKey() {
		assertEquals("catalog.product.created.v1", CatalogRoutingKeys.resolve(CatalogEventTypes.PRODUCT_CREATED));
	}

	@Test
	void shouldResolveProductPriceChangedRoutingKey() {
		assertEquals(
				"catalog.product.price.changed.v1",
				CatalogRoutingKeys.resolve(CatalogEventTypes.PRODUCT_PRICE_CHANGED));
	}

	@Test
	void shouldResolveProductEnrichmentRequestedRoutingKey() {
		assertEquals(
				"catalog.product.enrichment.requested.v1",
				CatalogRoutingKeys.resolve(CatalogEventTypes.PRODUCT_ENRICHMENT_REQUESTED));
	}
}
