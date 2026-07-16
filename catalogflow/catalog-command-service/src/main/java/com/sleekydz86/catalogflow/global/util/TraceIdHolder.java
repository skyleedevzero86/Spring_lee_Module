package com.sleekydz86.catalogflow.global.util;

import java.util.UUID;

public final class TraceIdHolder {

	private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

	private TraceIdHolder() {
	}

	public static void set(String traceId) {
		CURRENT.set(traceId);
	}

	public static String get() {
		return CURRENT.get();
	}

	public static String getOrGenerate() {
		String traceId = CURRENT.get();
		if (traceId == null || traceId.isBlank()) {
			traceId = UUID.randomUUID().toString();
			CURRENT.set(traceId);
		}
		return traceId;
	}

	public static void clear() {
		CURRENT.remove();
	}
}
