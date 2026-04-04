package com.sleekydz86.monitoring.backend.monitoring.adapter.in.web;

import com.sleekydz86.monitoring.backend.monitoring.application.dto.DashboardPayloads;
import com.sleekydz86.monitoring.backend.monitoring.application.port.in.DashboardQueryUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminDashboardController {

    private final DashboardQueryUseCase dashboardQuery;

    public AdminDashboardController(DashboardQueryUseCase dashboardQuery) {
        this.dashboardQuery = dashboardQuery;
    }

    @GetMapping("/overview")
    public DashboardPayloads.OverviewResponse overview() {
        return this.dashboardQuery.overview();
    }

    @GetMapping("/statistics")
    public DashboardPayloads.StatisticsResponse statistics() {
        return this.dashboardQuery.statistics();
    }

    @GetMapping("/actuator-summary")
    public DashboardPayloads.ActuatorSummaryResponse actuatorSummary() {
        return this.dashboardQuery.actuatorSummary();
    }
}
