package com.sleekydz86.jvmperf.global.util;

import java.util.List;

public final class GarbageCollectorName {

	private GarbageCollectorName() {
	}

	public static String from(List<String> names) {
		if (names == null || names.isEmpty()) {
			return "알 수 없음";
		}
		String joined = String.join(" ", names);
		if (joined.contains("G1")) {
			return "G1 GC";
		}
		if (joined.contains("ZGC")) {
			return "ZGC";
		}
		if (joined.contains("Shenandoah")) {
			return "Shenandoah";
		}
		return names.getFirst();
	}
}
