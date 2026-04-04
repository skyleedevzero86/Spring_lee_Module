package com.sleekydz86.monitoring.backend.monitoring.application.dto;

import java.util.List;
import java.util.Map;

public final class DashboardPayloads {

    private DashboardPayloads() {

    }

    public record ApplicationSummary(
            String name,
            String version,
            String springBootVersion,
            String javaVersion,
            String environment,
            String backendUrl,
            String actuatorBaseUrl,
            String adminUrl,
            String prometheusUrl,
            String grafanaUrl
    ) {
    }

    public record KpiCard(
            String label,
            String value,
            String caption,
            String tone
    ) {
    }

    public record TrendPoint(
            String label,
            double value
    ) {
    }

    public record SectionProgress(
            String title,
            int chapters,
            int durationMinutes,
            double completionRate,
            String focus,
            String accent
    ) {
    }

    public record ComponentStatus(
            String name,
            String status,
            String detail
    ) {
    }

    public record HealthSnapshot(
            String status,
            long uptimeMinutes,
            List<ComponentStatus> components
    ) {
    }

    public record EndpointSummary(
            String id,
            String description,
            String path,
            boolean exposed
    ) {
    }

    public record MetricStat(
            String name,
            String description,
            String statistic,
            double value,
            String unit
    ) {
    }

    public record TagSummary(
            String key,
            List<String> values
    ) {
    }

    public record DistributionItem(
            String label,
            double value,
            String accent
    ) {
    }

    public record MonitoringLink(
            String title,
            String description,
            String url,
            String kind
    ) {
    }

    public record InfrastructureStatus(
            String component,
            String status,
            double availability,
            double latencyMs,
            String lastCheckedAt,
            String detail
    ) {
    }

    public record ServerStatus(
            String hostName,
            String operatingSystemFamily,
            String operatingSystem,
            String architecture,
            String javaRuntime,
            int availableProcessors,
            long totalMemoryBytes,
            long usedMemoryBytes,
            long freeMemoryBytes,
            double memoryUsagePercent,
            long totalDiskBytes,
            long usedDiskBytes,
            long freeDiskBytes,
            double diskUsagePercent,
            String diskPath,
            String capturedAt
    ) {
    }

    public record StoreUsage(
            String component,
            String status,
            String version,
            Long usedBytes,
            Long limitBytes,
            Long freeBytes,
            Double usagePercent,
            long dailyGrowthBytes,
            String capturedAt,
            String detail
    ) {
    }

    public record TimeWindow(
            String label,
            String range,
            String purpose,
            String query
    ) {
    }

    public record OverviewResponse(
            String generatedAt,
            ApplicationSummary application,
            HealthSnapshot health,
            List<KpiCard> kpis,
            List<TrendPoint> requestTrend,
            List<TrendPoint> latencyTrend,
            List<TrendPoint> cacheTrend,
            List<SectionProgress> sections,
            List<EndpointSummary> actuatorEndpoints,
            List<MonitoringLink> monitoringLinks,
            List<InfrastructureStatus> infrastructure,
            ServerStatus server,
            List<StoreUsage> storage,
            List<String> highlights
    ) {
    }

    public record StatisticsResponse(
            String generatedAt,
            ApplicationSummary application,
            List<MetricStat> highlightedMetrics,
            List<DistributionItem> healthBreakdown,
            List<DistributionItem> timerPercentiles,
            List<TagSummary> tags,
            List<TrendPoint> requestTrend,
            List<TrendPoint> latencyTrend,
            List<SectionProgress> sections,
            List<InfrastructureStatus> infrastructure,
            ServerStatus server,
            List<StoreUsage> storage,
            List<TimeWindow> timeWindows,
            List<String> notes
    ) {
    }

    public record ActuatorSummaryResponse(
            String generatedAt,
            ApplicationSummary application,
            HealthSnapshot health,
            Map<String, Object> info,
            Map<String, Object> customEndpoint,
            List<String> metricNames,
            List<EndpointSummary> links,
            List<MonitoringLink> monitoringLinks,
            List<InfrastructureStatus> infrastructure,
            ServerStatus server,
            List<StoreUsage> storage,
            List<TimeWindow> timeWindows
    ) {
    }
}
