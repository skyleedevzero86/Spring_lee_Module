package com.sleekydz86.monitoring.backend.monitoring.application.service;

import com.sleekydz86.monitoring.backend.monitoring.application.dto.DashboardPayloads;
import com.sleekydz86.monitoring.backend.monitoring.application.port.in.DashboardQueryUseCase;
import com.sleekydz86.monitoring.backend.monitoring.domain.course.CourseSection;
import com.sleekydz86.monitoring.backend.monitoring.domain.port.CourseCatalogPort;
import com.sleekydz86.monitoring.backend.monitoring.infrastructure.observation.ConnectedStoreObservationService;
import com.sleekydz86.monitoring.backend.monitoring.infrastructure.observation.HostSystemObservationService;
import com.sleekydz86.monitoring.backend.monitoring.infrastructure.observation.InfrastructureProbeService;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.boot.health.actuate.endpoint.CompositeHealthDescriptor;
import org.springframework.boot.health.actuate.endpoint.HealthDescriptor;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.health.actuate.endpoint.IndicatedHealthDescriptor;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.micrometer.metrics.actuate.endpoint.MetricsEndpoint;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class DashboardApplicationService implements DashboardQueryUseCase {
    private static final int MAX_POINTS = 12;
    private static final DateTimeFormatter LABEL_FORMATTER = DateTimeFormatter.ofPattern("HH:mm", Locale.KOREAN).withZone(ZoneId.of("Asia/Seoul"));
    private final InfoEndpoint infoEndpoint;
    private final MetricsEndpoint metricsEndpoint;
    private final HealthEndpoint healthEndpoint;
    private final CourseCatalogPort courseCatalog;
    private final InfrastructureProbeService infrastructureProbeService;
    private final HostSystemObservationService hostSystemObservationService;
    private final ConnectedStoreObservationService connectedStoreObservationService;
    private final Environment environment;
    private final BuildProperties buildProperties;
    private final Counter overviewViews;
    private final Counter statisticsViews;
    private final Counter actuatorViews;
    private final Counter syntheticRequests;
    private final Timer syntheticLatencyTimer;
    private final AtomicInteger activeSessions = new AtomicInteger(46);
    private final AtomicInteger alertQueue = new AtomicInteger(3);
    private final AtomicInteger cacheHitRate = new AtomicInteger(91);
    private final AtomicReference<Instant> lastSyntheticRefresh = new AtomicReference<>(Instant.now());
    private final Map<String, AtomicInteger> sectionCompletionGauges = new LinkedHashMap<>();
    private final ConcurrentLinkedDeque<DashboardPayloads.TrendPoint> requestTrend = new ConcurrentLinkedDeque<>();
    private final ConcurrentLinkedDeque<DashboardPayloads.TrendPoint> latencyTrend = new ConcurrentLinkedDeque<>();
    private final ConcurrentLinkedDeque<DashboardPayloads.TrendPoint> cacheTrend = new ConcurrentLinkedDeque<>();

    public DashboardApplicationService(MeterRegistry meterRegistry, InfoEndpoint infoEndpoint, MetricsEndpoint metricsEndpoint, HealthEndpoint healthEndpoint, CourseCatalogPort courseCatalog, InfrastructureProbeService infrastructureProbeService, HostSystemObservationService hostSystemObservationService, ConnectedStoreObservationService connectedStoreObservationService, Environment environment, ObjectProvider<BuildProperties> buildProperties) {
        this.infoEndpoint = infoEndpoint;
        this.metricsEndpoint = metricsEndpoint;
        this.healthEndpoint = healthEndpoint;
        this.courseCatalog = courseCatalog;
        this.infrastructureProbeService = infrastructureProbeService;
        this.hostSystemObservationService = hostSystemObservationService;
        this.connectedStoreObservationService = connectedStoreObservationService;
        this.environment = environment;
        this.buildProperties = buildProperties.getIfAvailable();
        this.overviewViews = Counter.builder("monitoring.admin.page.views").tag("page", "overview").description("Overview page visits").register(meterRegistry);
        this.statisticsViews = Counter.builder("monitoring.admin.page.views").tag("page", "statistics").description("Statistics page visits").register(meterRegistry);
        this.actuatorViews = Counter.builder("monitoring.admin.page.views").tag("page", "actuator").description("Actuator page visits").register(meterRegistry);
        this.syntheticRequests = Counter.builder("monitoring.synthetic.requests").description("Synthetic request volume for the dashboard demo").register(meterRegistry);
        this.syntheticLatencyTimer = Timer.builder("monitoring.synthetic.latency").description("Synthetic latency timer used for percentile demos").publishPercentiles(0.5, 0.9, 0.95).register(meterRegistry);
        Gauge.builder("monitoring.active.sessions", this.activeSessions, AtomicInteger::get).description("Current active admin sessions").register(meterRegistry);
        Gauge.builder("monitoring.alert.queue", this.alertQueue, AtomicInteger::get).description("Current queued alerts").register(meterRegistry);
        Gauge.builder("monitoring.cache.hit.rate", this.cacheHitRate, AtomicInteger::get).description("Synthetic cache hit rate").baseUnit("percent").register(meterRegistry);
        for (CourseSection section : this.courseCatalog.sections()) {
            AtomicInteger gauge = new AtomicInteger((int) Math.round(section.completionRate()));
            this.sectionCompletionGauges.put(section.id(), gauge);
            Gauge.builder("monitoring.section.completion", gauge, AtomicInteger::get).tag("section", section.id()).description("Per-section completion gauge").baseUnit("percent").register(meterRegistry);
        }
        seedSyntheticData();
    }

    @Override
    @Timed(value = "monitoring.dashboard.render", extraTags = {"page", "overview"}, percentiles = {0.5, 0.9, 0.95}, histogram = true)
    public DashboardPayloads.OverviewResponse overview() {
        this.overviewViews.increment();
        DashboardPayloads.HealthSnapshot health = createHealthSnapshot();
        return new DashboardPayloads.OverviewResponse(Instant.now().toString(), applicationSummary(), health, buildOverviewCards(health), snapshot(this.requestTrend), snapshot(this.latencyTrend), snapshot(this.cacheTrend), sectionProgress(), actuatorEndpoints(), monitoringLinks(), infrastructureStatuses(), serverStatus(), storageStatuses(), List.of("See Windows/Linux host details, memory, disk, PostgreSQL size, and Redis memory in one place.", "Store usage shows current size plus net 24 hour delta from in-app snapshots.", "Prometheus and Grafana keep the same metrics for longer history and charts.", "PostgreSQL total disk capacity is not exposed by standard SQL, so the dashboard focuses on current database size."));
    }

    @Override
    @Timed(value = "monitoring.dashboard.render", extraTags = {"page", "statistics"}, percentiles = {0.5, 0.9, 0.95}, histogram = true)
    public DashboardPayloads.StatisticsResponse statistics() {
        this.statisticsViews.increment();
        DashboardPayloads.HealthSnapshot health = createHealthSnapshot();
        return new DashboardPayloads.StatisticsResponse(Instant.now().toString(), applicationSummary(), highlightedMetrics(), healthBreakdown(health), timerPercentiles(), availableTags(), snapshot(this.requestTrend), snapshot(this.latencyTrend), sectionProgress(), infrastructureStatuses(), serverStatus(), storageStatuses(), timeWindows(), List.of("monitoring.host.* tracks the current host resources.", "monitoring.storage.used and monitoring.storage.daily.growth track PostgreSQL and Redis usage.", "Prometheus is the right place for day, week, and month storage trend analysis."));
    }

    @Override
    @Timed(value = "monitoring.dashboard.render", extraTags = {"page", "actuator"}, percentiles = {0.5, 0.9, 0.95}, histogram = true)
    public DashboardPayloads.ActuatorSummaryResponse actuatorSummary() {
        this.actuatorViews.increment();
        return new DashboardPayloads.ActuatorSummaryResponse(Instant.now().toString(), applicationSummary(), createHealthSnapshot(), this.infoEndpoint.info(), customEndpointPayload(), this.metricsEndpoint.listNames().getNames().stream().sorted().filter((name) -> name.startsWith("monitoring") || name.startsWith("jvm") || name.startsWith("system") || name.startsWith("process")).limit(40).toList(), actuatorEndpoints(), monitoringLinks(), infrastructureStatuses(), serverStatus(), storageStatuses(), timeWindows());
    }

    @Override
    public Map<String, Object> customEndpointPayload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("generatedAt", Instant.now().toString());
        payload.put("averageCompletion", this.courseCatalog.averageCompletion());
        payload.put("totalMinutes", this.courseCatalog.totalMinutes());
        payload.put("liveGauges", Map.of("activeSessions", this.activeSessions.get(), "alertQueue", this.alertQueue.get(), "cacheHitRate", this.cacheHitRate.get()));
        payload.put("sections", this.courseCatalog.sections());
        payload.put("infrastructure", infrastructureStatuses());
        payload.put("server", serverStatus());
        payload.put("storage", storageStatuses());
        payload.put("monitoringLinks", monitoringLinks());
        payload.put("timeWindows", timeWindows());
        payload.put("recommendedChecks", List.of("/actuator/health", "/actuator/health/infrastructure", "/actuator/info", "/actuator/metrics/monitoring.host.memory.used", "/actuator/metrics/monitoring.storage.used", "/actuator/prometheus"));
        payload.put("lastSyntheticRefresh", this.lastSyntheticRefresh.get().toString());
        return payload;
    }

    @Override
    public Map<String, Object> simulateTraffic(String target, int burstSize) {
        int burst = Math.max(1, Math.min(burstSize, 50));
        if ("statistics".equalsIgnoreCase(target)) this.statisticsViews.increment(burst); else if ("actuator".equalsIgnoreCase(target)) this.actuatorViews.increment(burst); else this.overviewViews.increment(burst);
        int requests = burst * 14;
        double latency = 95 + ThreadLocalRandom.current().nextDouble(40, 140);
        this.syntheticRequests.increment(requests);
        this.syntheticLatencyTimer.record(Duration.ofMillis(Math.round(latency)));
        this.activeSessions.set(clamp(this.activeSessions.get() + burst / 2, 20, 160));
        pushPoint(this.requestTrend, requests);
        pushPoint(this.latencyTrend, latency);
        pushPoint(this.cacheTrend, this.cacheHitRate.get());
        this.lastSyntheticRefresh.set(Instant.now());
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("target", target);
        response.put("burstSize", burst);
        response.put("latestRequestsPerMinute", latestValue(this.requestTrend));
        response.put("latestLatencyMs", latestValue(this.latencyTrend));
        response.put("timestamp", Instant.now().toString());
        return response;
    }

    @Scheduled(fixedRate = 15000, initialDelay = 5000)
    void refreshSyntheticMetrics() {
        int requests = ThreadLocalRandom.current().nextInt(120, 230);
        double latency = ThreadLocalRandom.current().nextDouble(82, 185);
        int nextCache = ThreadLocalRandom.current().nextInt(84, 98);
        this.syntheticRequests.increment(requests);
        this.syntheticLatencyTimer.record(Duration.ofMillis(Math.round(latency)));
        this.activeSessions.set(clamp(this.activeSessions.get() + ThreadLocalRandom.current().nextInt(-5, 6), 24, 140));
        this.alertQueue.set(clamp(this.alertQueue.get() + ThreadLocalRandom.current().nextInt(-2, 3), 0, 18));
        this.cacheHitRate.set(nextCache);
        pushPoint(this.requestTrend, requests);
        pushPoint(this.latencyTrend, latency);
        pushPoint(this.cacheTrend, nextCache);
        this.lastSyntheticRefresh.set(Instant.now());
    }

    private void seedSyntheticData() {
        for (int index = 11; index >= 0; index--) {
            Instant pointTime = Instant.now().minusSeconds(index * 60L);
            this.requestTrend.add(new DashboardPayloads.TrendPoint(LABEL_FORMATTER.format(pointTime), ThreadLocalRandom.current().nextInt(115, 210)));
            this.latencyTrend.add(new DashboardPayloads.TrendPoint(LABEL_FORMATTER.format(pointTime), ThreadLocalRandom.current().nextDouble(88, 172)));
            this.cacheTrend.add(new DashboardPayloads.TrendPoint(LABEL_FORMATTER.format(pointTime), ThreadLocalRandom.current().nextInt(84, 97)));
        }
    }

    private DashboardPayloads.ApplicationSummary applicationSummary() {
        String profiles = this.environment.getActiveProfiles().length == 0 ? "default" : String.join(", ", this.environment.getActiveProfiles());
        return new DashboardPayloads.ApplicationSummary(this.environment.getProperty("spring.application.name", "spring-monitoring-server"), this.buildProperties != null ? this.buildProperties.getVersion() : "0.0.1-SNAPSHOT", SpringBootVersion.getVersion(), this.environment.getProperty("info.app.java.target", "25"), profiles, this.environment.getProperty("idolglow.monitoring.public-base-url", "http://localhost:8080"), this.environment.getProperty("idolglow.monitoring.actuator-base-url", "http://localhost:8081/actuator"), this.environment.getProperty("idolglow.monitoring.admin-url", "http://localhost:8080"), this.environment.getProperty("idolglow.monitoring.prometheus-url", "http://localhost:9091"), this.environment.getProperty("idolglow.monitoring.grafana-url", "http://localhost:3001"));
    }

    private DashboardPayloads.HealthSnapshot createHealthSnapshot() {
        HealthDescriptor descriptor = this.healthEndpoint.health();
        List<DashboardPayloads.ComponentStatus> components = new ArrayList<>();
        if (descriptor instanceof CompositeHealthDescriptor composite && composite.getComponents() != null) {
            composite.getComponents().forEach((name, child) -> components.add(new DashboardPayloads.ComponentStatus(prettify(name), child.getStatus().getCode(), summarizeHealthDescriptor(child))));
        }
        components.sort(Comparator.comparing(DashboardPayloads.ComponentStatus::name));
        long uptimeMinutes = Math.round(Duration.ofMillis(java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime()).toMinutes());
        return new DashboardPayloads.HealthSnapshot(descriptor.getStatus().getCode(), uptimeMinutes, components);
    }

    private String summarizeHealthDescriptor(HealthDescriptor descriptor) {
        if (descriptor instanceof IndicatedHealthDescriptor indicated) {
            if (indicated.getDetails().isEmpty()) return "No details";
            return indicated.getDetails().entrySet().stream().limit(2).map((entry) -> entry.getKey() + "=" + Objects.toString(entry.getValue())).collect(Collectors.joining(", "));
        }
        if (descriptor instanceof CompositeHealthDescriptor composite && composite.getComponents() != null) return "components=" + composite.getComponents().size();
        return "status=" + descriptor.getStatus().getCode();
    }

    private List<DashboardPayloads.KpiCard> buildOverviewCards(DashboardPayloads.HealthSnapshot health) {
        DashboardPayloads.ServerStatus server = serverStatus();
        List<DashboardPayloads.StoreUsage> stores = storageStatuses();
        return List.of(new DashboardPayloads.KpiCard("Overall Health", health.status(), "Actuator health, DB, Redis, custom contributor", statusTone(health.status())), new DashboardPayloads.KpiCard("Server OS", server.operatingSystemFamily(), server.hostName() + " / " + server.operatingSystem(), "info"), new DashboardPayloads.KpiCard("Host Memory", String.format(Locale.US, "%.1f%%", server.memoryUsagePercent()), formatBytes(server.usedMemoryBytes()) + " / " + formatBytes(server.totalMemoryBytes()), server.memoryUsagePercent() >= 80.0 ? "warning" : "success"), new DashboardPayloads.KpiCard("Host Disk", String.format(Locale.US, "%.1f%%", server.diskUsagePercent()), formatBytes(server.usedDiskBytes()) + " / " + formatBytes(server.totalDiskBytes()), server.diskUsagePercent() >= 85.0 ? "warning" : "success"), storageCard(stores, "PostgreSQL"), storageCard(stores, "Redis"));
    }

    private DashboardPayloads.KpiCard storageCard(List<DashboardPayloads.StoreUsage> stores, String component) {
        DashboardPayloads.StoreUsage usage = stores.stream().filter((item) -> component.equalsIgnoreCase(item.component())).findFirst().orElse(null);
        if (usage == null) return new DashboardPayloads.KpiCard(component, "N/A", "No data", "warning");
        String value = usage.usedBytes() == null ? "N/A" : formatBytes(usage.usedBytes());
        String caption = "24h delta " + signedBytes(usage.dailyGrowthBytes());
        if (usage.usagePercent() != null) caption = caption + " / usage " + String.format(Locale.US, "%.1f%%", usage.usagePercent());
        return new DashboardPayloads.KpiCard(component, value, caption, statusTone(usage.status()));
    }

    private List<DashboardPayloads.EndpointSummary> actuatorEndpoints() {
        DashboardPayloads.ApplicationSummary app = applicationSummary();
        return List.of(new DashboardPayloads.EndpointSummary("health", "Application overall health", app.actuatorBaseUrl() + "/health", true), new DashboardPayloads.EndpointSummary("health/infrastructure", "DB, Redis, custom health group", app.actuatorBaseUrl() + "/health/infrastructure", true), new DashboardPayloads.EndpointSummary("info", "Runtime and monitoring metadata", app.actuatorBaseUrl() + "/info", true), new DashboardPayloads.EndpointSummary("metrics", "Micrometer metric catalog", app.actuatorBaseUrl() + "/metrics", true), new DashboardPayloads.EndpointSummary("prometheus", "Prometheus scrape endpoint", app.actuatorBaseUrl() + "/prometheus", true), new DashboardPayloads.EndpointSummary("course-monitoring", "Custom monitoring endpoint", app.actuatorBaseUrl() + "/course-monitoring", true), new DashboardPayloads.EndpointSummary("spring-boot-admin", "Live Spring Boot Admin UI", app.adminUrl(), true));
    }

    private List<DashboardPayloads.MonitoringLink> monitoringLinks() {
        DashboardPayloads.ApplicationSummary app = applicationSummary();
        return List.of(new DashboardPayloads.MonitoringLink("Spring Boot Admin", "Live app state and endpoint inspection.", app.adminUrl(), "admin"), new DashboardPayloads.MonitoringLink("Actuator Health Group", "DB, Redis, and custom health in one view.", app.actuatorBaseUrl() + "/health/infrastructure", "actuator"), new DashboardPayloads.MonitoringLink("Prometheus", "Time-series store for host, PostgreSQL, and Redis metrics.", app.prometheusUrl(), "prometheus"), new DashboardPayloads.MonitoringLink("Grafana", "Hourly, daily, weekly, and monthly charts.", app.grafanaUrl(), "grafana"));
    }

    private List<DashboardPayloads.InfrastructureStatus> infrastructureStatuses() {
        return this.infrastructureProbeService.snapshots().stream().map((snapshot) -> new DashboardPayloads.InfrastructureStatus(snapshot.component(), snapshot.status(), snapshot.availability(), snapshot.latencyMs(), snapshot.lastCheckedAt().toString(), snapshot.detail())).toList();
    }

    private DashboardPayloads.ServerStatus serverStatus() {
        HostSystemObservationService.HostSnapshot host = this.hostSystemObservationService.snapshot();
        return new DashboardPayloads.ServerStatus(host.hostName(), host.operatingSystemFamily(), host.operatingSystem(), host.architecture(), host.javaRuntime(), host.availableProcessors(), host.totalMemoryBytes(), host.usedMemoryBytes(), host.freeMemoryBytes(), host.memoryUsagePercent(), host.totalDiskBytes(), host.usedDiskBytes(), host.freeDiskBytes(), host.diskUsagePercent(), host.diskPath(), host.capturedAt().toString());
    }

    private List<DashboardPayloads.StoreUsage> storageStatuses() {
        return this.connectedStoreObservationService.snapshots().stream().map((snapshot) -> new DashboardPayloads.StoreUsage(snapshot.component(), snapshot.status(), snapshot.version(), snapshot.usedBytes(), snapshot.limitBytes(), snapshot.freeBytes(), snapshot.usagePercent(), snapshot.dailyGrowthBytes(), snapshot.capturedAt().toString(), snapshot.detail())).toList();
    }

    private List<DashboardPayloads.TimeWindow> timeWindows() {
        return List.of(new DashboardPayloads.TimeWindow("Real-time", "Last 15 seconds to 5 minutes", "Inspect current host memory, disk, DB, and Redis values.", "monitoring_host_memory_used_bytes"), new DashboardPayloads.TimeWindow("Hourly", "Last 1 hour", "Track request rate and latency with host memory changes.", "sum(rate(monitoring_synthetic_requests_total[5m]))"), new DashboardPayloads.TimeWindow("Daily", "Last 24 hours", "Check how much PostgreSQL and Redis grew during the day.", "monitoring_storage_daily_growth_bytes{component=\"postgresql\"}"), new DashboardPayloads.TimeWindow("Weekly", "Last 7 days", "Compare host disk and PostgreSQL size trends.", "max_over_time(monitoring_storage_used_bytes{component=\"postgresql\"}[7d])"), new DashboardPayloads.TimeWindow("Monthly", "Last 30 days", "Review long-term storage growth and disk pressure.", "max_over_time(monitoring_host_disk_used_bytes[30d])"));
    }

    private List<DashboardPayloads.SectionProgress> sectionProgress() {
        return this.courseCatalog.sections().stream().map((section) -> new DashboardPayloads.SectionProgress(section.title(), section.chapters(), section.durationMinutes(), section.completionRate(), section.focus(), section.accent())).toList();
    }

    private List<DashboardPayloads.MetricStat> highlightedMetrics() {
        List<DashboardPayloads.MetricStat> stats = new ArrayList<>();
        List<String> metricNames = List.of("jvm.memory.used", "process.uptime", "system.cpu.usage", "monitoring.active.sessions", "monitoring.alert.queue", "monitoring.synthetic.latency", "monitoring.admin.page.views", "monitoring.host.memory.used", "monitoring.host.disk.used", "monitoring.storage.used", "monitoring.storage.daily.growth");
        for (String metricName : metricNames) {
            MetricsEndpoint.MetricDescriptor descriptor = metricDescriptor(metricName);
            if (descriptor == null) continue;
            descriptor.getMeasurements().stream().limit(3).map((sample) -> new DashboardPayloads.MetricStat(descriptor.getName(), descriptor.getDescription() == null ? "No description" : descriptor.getDescription(), sample.getStatistic().name(), sample.getValue(), descriptor.getBaseUnit() == null ? "" : descriptor.getBaseUnit())).forEach(stats::add);
        }
        for (DashboardPayloads.StoreUsage store : storageStatuses()) {
            String component = store.component().toLowerCase(Locale.ROOT);
            if (store.usedBytes() != null) stats.add(new DashboardPayloads.MetricStat("monitoring.storage.used{component=\"" + component + "\"}", "Current storage or memory usage of the connected store.", "VALUE", store.usedBytes(), "bytes"));
            stats.add(new DashboardPayloads.MetricStat("monitoring.storage.daily.growth{component=\"" + component + "\"}", "Net growth during the recent 24 hours from in-app snapshots.", "VALUE", store.dailyGrowthBytes(), "bytes"));
        }
        DashboardPayloads.ServerStatus server = serverStatus();
        stats.add(new DashboardPayloads.MetricStat("monitoring.host.memory.usage", "Current host physical memory usage percent.", "VALUE", server.memoryUsagePercent(), ""));
        stats.add(new DashboardPayloads.MetricStat("monitoring.host.disk.usage", "Current host disk usage percent.", "VALUE", server.diskUsagePercent(), ""));
        return stats;
    }

    private MetricsEndpoint.MetricDescriptor metricDescriptor(String metricName) { return this.metricsEndpoint.metric(metricName, null); }

    private List<DashboardPayloads.DistributionItem> healthBreakdown(DashboardPayloads.HealthSnapshot health) {
        Map<String, Long> counts = health.components().stream().collect(Collectors.groupingBy(DashboardPayloads.ComponentStatus::status, LinkedHashMap::new, Collectors.counting()));
        if (counts.isEmpty()) counts = Map.of(health.status(), 1L);
        return counts.entrySet().stream().map((entry) -> new DashboardPayloads.DistributionItem(entry.getKey(), entry.getValue(), statusAccent(entry.getKey()))).toList();
    }

    private List<DashboardPayloads.DistributionItem> timerPercentiles() {
        List<Double> values = this.latencyTrend.stream().map(DashboardPayloads.TrendPoint::value).sorted().toList();
        return List.of(new DashboardPayloads.DistributionItem("P50", percentile(values, 0.50), "#7dd3fc"), new DashboardPayloads.DistributionItem("P90", percentile(values, 0.90), "#fbbf24"), new DashboardPayloads.DistributionItem("P95", percentile(values, 0.95), "#fb7185"));
    }

    private List<DashboardPayloads.TagSummary> availableTags() {
        LinkedHashSet<DashboardPayloads.TagSummary> tags = new LinkedHashSet<>();
        List<String> metrics = List.of("monitoring.admin.page.views", "monitoring.section.completion", "monitoring.infrastructure.availability", "monitoring.storage.used");
        for (String metric : metrics) {
            MetricsEndpoint.MetricDescriptor descriptor = metricDescriptor(metric);
            if (descriptor == null) continue;
            descriptor.getAvailableTags().stream()
                    .map(tag -> new DashboardPayloads.TagSummary(tag.getTag(), List.copyOf(tag.getValues())))
                    .forEach(tags::add);
        }
        tags.add(new DashboardPayloads.TagSummary("component", List.of("db", "redis", "postgresql")));
        return List.copyOf(tags);
    }

    private static double percentile(List<Double> sortedValues, double percentile) {
        if (sortedValues.isEmpty()) return 0.0;
        int index = (int) Math.ceil(percentile * sortedValues.size()) - 1;
        return sortedValues.get(Math.max(0, Math.min(index, sortedValues.size() - 1)));
    }

    private void pushPoint(ConcurrentLinkedDeque<DashboardPayloads.TrendPoint> target, double value) {
        target.add(new DashboardPayloads.TrendPoint(LABEL_FORMATTER.format(Instant.now()), value));
        while (target.size() > MAX_POINTS) target.pollFirst();
    }

    private List<DashboardPayloads.TrendPoint> snapshot(ConcurrentLinkedDeque<DashboardPayloads.TrendPoint> source) { return List.copyOf(source); }
    private double latestValue(ConcurrentLinkedDeque<DashboardPayloads.TrendPoint> source) { DashboardPayloads.TrendPoint latest = source.peekLast(); return latest == null ? 0.0 : latest.value(); }
    private static int clamp(int value, int min, int max) { return Math.max(min, Math.min(value, max)); }
    private static String prettify(String raw) { return raw.replace('-', ' ').replace('_', ' ').replaceAll("([a-z])([A-Z])", "$1 $2").strip(); }
    private static String statusTone(String status) { return switch (status.toUpperCase(Locale.ROOT)) { case "UP" -> "success"; case "DOWN", "OUT_OF_SERVICE" -> "danger"; default -> "warning"; }; }
    private static String statusAccent(String status) { return switch (status.toUpperCase(Locale.ROOT)) { case "UP" -> "#a3e635"; case "DOWN" -> "#fb7185"; case "OUT_OF_SERVICE" -> "#f97316"; default -> "#7dd3fc"; }; }
    private static String formatBytes(long bytes) { if (bytes <= 0L) return "0 B"; String[] units = {"B", "KB", "MB", "GB", "TB"}; double value = bytes; int index = 0; while (value >= 1024.0 && index < units.length - 1) { value /= 1024.0; index++; } return String.format(Locale.US, "%.1f %s", value, units[index]); }
    private static String signedBytes(long bytes) { return (bytes >= 0L ? "+" : "-") + formatBytes(Math.abs(bytes)); }
}
