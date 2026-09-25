package com.sleekydz86.searchai.usage.domain;

public enum StatsGranularity {
	HOUR,
	DAY,
	WEEK,
	MONTH;

	public static StatsGranularity from(String value) {
		if (value == null || value.isBlank()) {
			return DAY;
		}
		return StatsGranularity.valueOf(value.trim().toUpperCase());
	}
}
