package com.sleekydz86.jvmboard.global.util;

public final class MemoryFormatter {

	private static final double MEBIBYTE = 1024d * 1024d;

	private MemoryFormatter() {
	}

	public static String megabytes(long bytes) {
		if (bytes < 0) {
			return "제한 없음";
		}
		long megabytes = Math.round(bytes / MEBIBYTE);
		return megabytes + " MB";
	}
}
