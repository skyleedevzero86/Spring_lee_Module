package com.sleekydz86.catalogflow.global.util;

import java.util.UUID;

public final class CorrelationIdHolder {

	private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

	private CorrelationIdHolder() {
	}

	public static void set(String correlationId) {
		CURRENT.set(correlationId);
	}

	public static String get() {
		return CURRENT.get();
	}

	public static String getOrGenerate() {
		String correlationId = CURRENT.get();
		if (correlationId == null || correlationId.isBlank()) {
			return UUID.randomUUID().toString();
		}
		return correlationId;
	}

	public static void clear() {
		CURRENT.remove();
	}
}
