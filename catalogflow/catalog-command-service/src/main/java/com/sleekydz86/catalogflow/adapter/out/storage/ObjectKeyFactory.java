package com.sleekydz86.catalogflow.adapter.out.storage;

import java.util.UUID;

public final class ObjectKeyFactory {

	private ObjectKeyFactory() {
	}

	public static String create(UUID productId, String extension, boolean temporary) {
		String objectId = UUID.randomUUID().toString().replace("-", "");
		String prefix = temporary ? "temp/" : "products/";
		return prefix + productId + "/" + objectId + "." + extension;
	}
}
