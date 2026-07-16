package com.sleekydz86.catalogflow.eventcontract;

public final class CatalogRoutingKeys {

	public static final String PREFIX = "catalog.product";
	public static final String DEAD_LETTER = "catalog.dead-letter";
	public static final String BATCH_RETRY = "catalog.batch.retry";

	private CatalogRoutingKeys() {
	}

	public static String resolve(String eventType) {
		String normalized = eventType
				.replace("Product", "")
				.replaceAll("([a-z])([A-Z])", "$1.$2")
				.toLowerCase();
		return PREFIX + "." + normalized + ".v1";
	}
}
