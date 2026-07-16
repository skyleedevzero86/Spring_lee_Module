package com.sleekydz86.catalogflow.global.util;

import java.sql.Timestamp;
import java.time.Instant;

public final class InstantSql {

	private InstantSql() {
	}

	public static Timestamp toTimestamp(Instant instant) {
		return instant == null ? null : Timestamp.from(instant);
	}
}
