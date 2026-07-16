package com.sleekydz86.catalogflow.global.cache;

import java.util.UUID;

public final class CacheKeys {

	private CacheKeys() {
	}

	public static String product(UUID productId) {
		return "catalog:product:" + productId;
	}

	public static String categoryPage(UUID categoryId, String status, String cursor, int size) {
		String normalizedStatus = status == null || status.isBlank() ? "ALL" : status;
		String normalizedCursor = cursor == null || cursor.isBlank() ? "FIRST" : cursor;
		return "catalog:category:" + categoryId + ":page:" + normalizedCursor
				+ ":status:" + normalizedStatus + ":size:" + size;
	}

	public static String categoryPrefix(UUID categoryId) {
		return "catalog:category:" + categoryId + ":";
	}

	public static String popular(int size) {
		return "catalog:popular:" + size;
	}

	public static String popularPrefix() {
		return "catalog:popular:";
	}
}
