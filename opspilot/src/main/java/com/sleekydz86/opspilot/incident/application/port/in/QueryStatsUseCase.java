package com.sleekydz86.opspilot.incident.application.port.in;

import com.sleekydz86.opspilot.incident.domain.IncidentStats;
import com.sleekydz86.opspilot.incident.domain.StatsGranularity;

import java.time.Instant;

public interface QueryStatsUseCase {

	IncidentStats summarize(Instant from, Instant to, StatsGranularity granularity);
}
