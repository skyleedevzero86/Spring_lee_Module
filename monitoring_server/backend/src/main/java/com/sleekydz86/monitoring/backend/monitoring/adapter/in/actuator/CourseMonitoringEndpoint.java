package com.sleekydz86.monitoring.backend.monitoring.adapter.in.actuator;

import com.sleekydz86.monitoring.backend.monitoring.application.port.in.DashboardQueryUseCase;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
@Endpoint(id = "course-monitoring")
public class CourseMonitoringEndpoint {

    private final DashboardQueryUseCase dashboardQuery;

    public CourseMonitoringEndpoint(DashboardQueryUseCase dashboardQuery) {
        this.dashboardQuery = dashboardQuery;
    }

    @ReadOperation
    public Map<String, Object> snapshot() {
        return this.dashboardQuery.customEndpointPayload();
    }

    @WriteOperation
    public Map<String, Object> simulate(String target, int burstSize) {
        return this.dashboardQuery.simulateTraffic(target, burstSize);
    }
}
