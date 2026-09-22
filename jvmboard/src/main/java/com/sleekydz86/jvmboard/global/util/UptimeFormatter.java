package com.sleekydz86.jvmboard.global.util;

import java.time.Duration;

public final class UptimeFormatter {

	private UptimeFormatter() {
	}

	public static String format(Duration uptime) {
		long seconds = Math.max(0, uptime.getSeconds());
		long hours = seconds / 3600;
		long minutes = (seconds % 3600) / 60;
		long remainingSeconds = seconds % 60;
		return "%02d:%02d:%02d".formatted(hours, minutes, remainingSeconds);
	}
}
