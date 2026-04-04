package com.sleekydz86.monitoring.backend.monitoring.application.port.in;

import com.sleekydz86.monitoring.backend.monitoring.application.dto.DashboardPayloads;
import java.util.Map;

public interface DashboardQueryUseCase {
    DashboardPayloads.OverviewResponse overview(String windowKey);
    DashboardPayloads.StatisticsResponse statistics(String windowKey);
    DashboardPayloads.ActuatorSummaryResponse actuatorSummary();
    Map<String, Object> customEndpointPayload();
    Map<String, Object> simulateTraffic(String target, int burstSize);
}
