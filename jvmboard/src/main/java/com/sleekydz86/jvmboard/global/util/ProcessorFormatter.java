package com.sleekydz86.jvmboard.global.util;

public final class ProcessorFormatter {

	private ProcessorFormatter() {
	}

	public static String label(int processors) {
		return processors + "개 프로세서";
	}
}
