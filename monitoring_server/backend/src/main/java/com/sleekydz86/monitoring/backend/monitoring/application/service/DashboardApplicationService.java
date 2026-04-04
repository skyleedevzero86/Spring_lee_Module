package com.sleekydz86.monitoring.backend.monitoring.application.service;

import com.sleekydz86.monitoring.backend.monitoring.application.dto.DashboardPayloads;
import com.sleekydz86.monitoring.backend.monitoring.application.port.in.DashboardQueryUseCase;
import com.sleekydz86.monitoring.backend.monitoring.application.support.DashboardWindow;
import com.sleekydz86.monitoring.backend.monitoring.domain.course.CourseSection;
import com.sleekydz86.monitoring.backend.monitoring.domain.port.CourseCatalogPort;
import com.sleekydz86.monitoring.backend.monitoring.infrastructure.observation.ConnectedStoreObservationService;
import com.sleekydz86.monitoring.backend.monitoring.infrastructure.observation.HostSystemObservationService;
import com.sleekydz86.monitoring.backend.monitoring.infrastructure.observation.InfrastructureProbeService;
import com.sleekydz86.monitoring.backend.monitoring.infrastructure.persistence.DashboardSnapshotJdbcRepository;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(DashboardApplicationService.class);
    private static final int MAX_POINTS = 12;
    private static final DateTimeFormatter LABEL_FORMATTER = DateTimeFormatter.ofPattern("HH:mm", Locale.KOREAN).withZone(ZoneId.of("Asia/Seoul"));

    private record WindowedTrendSeries(
            List<DashboardPayloads.TrendPoint> requestTrend,
            List<DashboardPayloads.TrendPoint> latencyTrend,
            List<DashboardPayloads.TrendPoint> cacheTrend
    ) {
    }

    private final InfoEndpoint infoEndpoint;
    private final MetricsEndpoint metricsEndpoint;
    private final HealthEndpoint healthEndpoint;
    private final CourseCatalogPort courseCatalog;
    private final InfrastructureProbeService infrastructureProbeService;
    private final HostSystemObservationService hostSystemObservationService;
    private final ConnectedStoreObservationService connectedStoreObservationService;
    private final DashboardSnapshotJdbcRepository snapshotRepository;
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

    public DashboardApplicationService(
            MeterRegistry meterRegistry,
            InfoEndpoint infoEndpoint,
            MetricsEndpoint metricsEndpoint,
            HealthEndpoint healthEndpoint,
            CourseCatalogPort courseCatalog,
            InfrastructureProbeService infrastructureProbeService,
            HostSystemObservationService hostSystemObservationService,
            ConnectedStoreObservationService connectedStoreObservationService,
            DashboardSnapshotJdbcRepository snapshotRepository,
            Environment environment,
            ObjectProvider<BuildProperties> buildProperties
    ) {
        this.infoEndpoint = infoEndpoint;
        this.metricsEndpoint = metricsEndpoint;
        this.healthEndpoint = healthEndpoint;
        this.courseCatalog = courseCatalog;
        this.infrastructureProbeService = infrastructureProbeService;
        this.hostSystemObservationService = hostSystemObservationService;
        this.connectedStoreObservationService = connectedStoreObservationService;
        this.snapshotRepository = snapshotRepository;
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
    public DashboardPayloads.OverviewResponse overview(String windowKey) {
        DashboardWindow window = DashboardWindow.fromKey(windowKey);
        this.overviewViews.increment();
        DashboardPayloads.HealthSnapshot health = createHealthSnapshot();
        DashboardPayloads.ServerStatus server = serverStatus();
        List<DashboardPayloads.StoreUsage> storage = storageStatuses(window);
        WindowedTrendSeries trends = loadWindowedTrendSeries(window);
        return new DashboardPayloads.OverviewResponse(Instant.now().toString(), applicationSummary(), health, buildOverviewCards(health, server, storage), trends.requestTrend(), trends.latencyTrend(), trends.cacheTrend(), sectionProgress(), actuatorEndpoints(), monitoringLinks(), infrastructureStatuses(), server, storage, List.of("선택한 기간 기준으로 요청 추이와 응답 지연, 캐시 적중률을 DB 스냅샷에서 다시 집계해 보여줍니다.", "스토어 사용량 카드는 현재 크기와 선택한 기간의 순증감을 함께 제공합니다.", "Prometheus와 Grafana에서는 같은 지표를 더 긴 기간의 이력과 차트로 확인할 수 있습니다.", "PostgreSQL 전체 디스크 용량은 표준 SQL로 직접 노출되지 않아 현재 데이터베이스 크기 중심으로 보여줍니다."));
    }

    @Override
    @Timed(value = "monitoring.dashboard.render", extraTags = {"page", "statistics"}, percentiles = {0.5, 0.9, 0.95}, histogram = true)
    public DashboardPayloads.StatisticsResponse statistics(String windowKey) {
        DashboardWindow window = DashboardWindow.fromKey(windowKey);
        this.statisticsViews.increment();
        DashboardPayloads.HealthSnapshot health = createHealthSnapshot();
        DashboardPayloads.ServerStatus server = serverStatus();
        List<DashboardPayloads.StoreUsage> storage = storageStatuses(window);
        WindowedTrendSeries trends = loadWindowedTrendSeries(window);
        return new DashboardPayloads.StatisticsResponse(Instant.now().toString(), applicationSummary(), highlightedMetrics(storage, window), healthBreakdown(health), timerPercentiles(trends.latencyTrend()), availableTags(), trends.requestTrend(), trends.latencyTrend(), sectionProgress(), infrastructureStatuses(), server, storage, timeWindows(), List.of("monitoring.host.* 는 현재 호스트 자원 상태를 추적합니다.", "스토어 사용량 카드는 DB 스냅샷을 기준으로 선택한 기간의 증감을 계산합니다.", "일간, 주간, 월간 스토리지 추세 분석은 Prometheus와 Grafana에서 확인하는 것이 가장 적합합니다."));
    }

    @Override
    @Timed(value = "monitoring.dashboard.render", extraTags = {"page", "actuator"}, percentiles = {0.5, 0.9, 0.95}, histogram = true)
    public DashboardPayloads.ActuatorSummaryResponse actuatorSummary() {
        this.actuatorViews.increment();
        DashboardWindow window = DashboardWindow.DAILY;
        return new DashboardPayloads.ActuatorSummaryResponse(Instant.now().toString(), applicationSummary(), createHealthSnapshot(), this.infoEndpoint.info(), customEndpointPayload(), this.metricsEndpoint.listNames().getNames().stream().sorted().filter((name) -> name.startsWith("monitoring") || name.startsWith("jvm") || name.startsWith("system") || name.startsWith("process")).limit(40).toList(), actuatorEndpoints(), monitoringLinks(), infrastructureStatuses(), serverStatus(), storageStatuses(window), timeWindows());
    }

    @Override
    public Map<String, Object> customEndpointPayload() {
        DashboardWindow window = DashboardWindow.DAILY;
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("generatedAt", Instant.now().toString());
        payload.put("averageCompletion", this.courseCatalog.averageCompletion());
        payload.put("totalMinutes", this.courseCatalog.totalMinutes());
        payload.put("liveGauges", Map.of("activeSessions", this.activeSessions.get(), "alertQueue", this.alertQueue.get(), "cacheHitRate", this.cacheHitRate.get()));
        payload.put("sections", this.courseCatalog.sections());
        payload.put("infrastructure", infrastructureStatuses());
        payload.put("server", serverStatus());
        payload.put("storage", storageStatuses(window));
        payload.put("monitoringLinks", monitoringLinks());
        payload.put("timeWindows", timeWindows());
        payload.put("recommendedChecks", List.of("/actuator/health", "/actuator/health/infrastructure", "/actuator/info", "/actuator/metrics/monitoring.host.memory.used", "/actuator/metrics/monitoring.storage.used", "/actuator/prometheus"));
        payload.put("snapshotWindow", window.key());
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
        persistCurrentSnapshotSafely();
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
        persistCurrentSnapshotSafely();
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
        String profiles = this.environment.getActiveProfiles().length == 0 ? "기본" : String.join(", ", this.environment.getActiveProfiles());
        return new DashboardPayloads.ApplicationSummary(this.environment.getProperty("spring.application.name", "spring-monitoring-server"), this.buildProperties != null ? this.buildProperties.getVersion() : "0.0.1-SNAPSHOT", SpringBootVersion.getVersion(), this.environment.getProperty("info.app.java.target", "25"), profiles, this.environment.getProperty("idolglow.monitoring.public-base-url", "http://localhost:8080"), this.environment.getProperty("idolglow.monitoring.actuator-base-url", "http://localhost:8081/actuator"), this.environment.getProperty("idolglow.monitoring.admin-url", "http://localhost:8080"), this.environment.getProperty("idolglow.monitoring.prometheus-url", "http://localhost:9091"), this.environment.getProperty("idolglow.monitoring.grafana-url", "http://localhost:3001"));
    }

    private DashboardPayloads.HealthSnapshot createHealthSnapshot() {
        HealthDescriptor descriptor = this.healthEndpoint.health();
        List<DashboardPayloads.ComponentStatus> components = new ArrayList<>();
        if (descriptor instanceof CompositeHealthDescriptor composite && composite.getComponents() != null) {
            composite.getComponents().forEach((name, child) -> components.add(new DashboardPayloads.ComponentStatus(localizeHealthComponentName(name), child.getStatus().getCode(), summarizeHealthDescriptor(child))));
        }
        components.sort(Comparator.comparing(DashboardPayloads.ComponentStatus::name));
        long uptimeMinutes = Math.round(Duration.ofMillis(java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime()).toMinutes());
        return new DashboardPayloads.HealthSnapshot(descriptor.getStatus().getCode(), uptimeMinutes, components);
    }

    private String summarizeHealthDescriptor(HealthDescriptor descriptor) {
        if (descriptor instanceof IndicatedHealthDescriptor indicated) {
            if (indicated.getDetails().isEmpty()) return "세부 정보 없음";
            return indicated.getDetails().entrySet().stream().limit(2).map((entry) -> localizeHealthDetailKey(entry.getKey()) + "=" + Objects.toString(entry.getValue())).collect(Collectors.joining(", "));
        }
        if (descriptor instanceof CompositeHealthDescriptor composite && composite.getComponents() != null) return "구성요소=" + composite.getComponents().size();
        return "상태=" + descriptor.getStatus().getCode();
    }

    private List<DashboardPayloads.KpiCard> buildOverviewCards(DashboardPayloads.HealthSnapshot health, DashboardPayloads.ServerStatus server, List<DashboardPayloads.StoreUsage> stores) {
        return List.of(new DashboardPayloads.KpiCard("전체 상태", health.status(), "Actuator 상태와 DB, Redis, 커스텀 상태 기여자", statusTone(health.status())), new DashboardPayloads.KpiCard("서버 운영체제", server.operatingSystemFamily(), server.hostName() + " / " + server.operatingSystem(), "info"), new DashboardPayloads.KpiCard("호스트 메모리", String.format(Locale.US, "%.1f%%", server.memoryUsagePercent()), formatBytes(server.usedMemoryBytes()) + " / " + formatBytes(server.totalMemoryBytes()), server.memoryUsagePercent() >= 80.0 ? "warning" : "success"), new DashboardPayloads.KpiCard("호스트 디스크", String.format(Locale.US, "%.1f%%", server.diskUsagePercent()), formatBytes(server.usedDiskBytes()) + " / " + formatBytes(server.totalDiskBytes()), server.diskUsagePercent() >= 85.0 ? "warning" : "success"), storageCard(stores, "PostgreSQL"), storageCard(stores, "Redis"));
    }

    private DashboardPayloads.KpiCard storageCard(List<DashboardPayloads.StoreUsage> stores, String component) {
        DashboardPayloads.StoreUsage usage = stores.stream().filter((item) -> component.equalsIgnoreCase(item.component())).findFirst().orElse(null);
        if (usage == null) return new DashboardPayloads.KpiCard(component, "없음", "데이터 없음", "warning");
        String value = usage.usedBytes() == null ? "없음" : formatBytes(usage.usedBytes());
        String caption = usage.growthLabel() + " 증감 " + signedBytes(usage.dailyGrowthBytes());
        if (usage.usagePercent() != null) caption = caption + " / 사용률 " + String.format(Locale.US, "%.1f%%", usage.usagePercent());
        return new DashboardPayloads.KpiCard(component, value, caption, statusTone(usage.status()));
    }

    private List<DashboardPayloads.EndpointSummary> actuatorEndpoints() {
        DashboardPayloads.ApplicationSummary app = applicationSummary();
        return List.of(new DashboardPayloads.EndpointSummary("health", "애플리케이션 전체 상태", app.actuatorBaseUrl() + "/health", true), new DashboardPayloads.EndpointSummary("health/infrastructure", "DB, Redis, 커스텀 상태 그룹", app.actuatorBaseUrl() + "/health/infrastructure", true), new DashboardPayloads.EndpointSummary("info", "런타임 및 모니터링 메타데이터", app.actuatorBaseUrl() + "/info", true), new DashboardPayloads.EndpointSummary("metrics", "Micrometer 지표 카탈로그", app.actuatorBaseUrl() + "/metrics", true), new DashboardPayloads.EndpointSummary("prometheus", "Prometheus 수집 엔드포인트", app.actuatorBaseUrl() + "/prometheus", true), new DashboardPayloads.EndpointSummary("course-monitoring", "커스텀 모니터링 엔드포인트", app.actuatorBaseUrl() + "/course-monitoring", true), new DashboardPayloads.EndpointSummary("spring-boot-admin", "실시간 Spring Boot Admin UI", app.adminUrl(), true));
    }

    private List<DashboardPayloads.MonitoringLink> monitoringLinks() {
        DashboardPayloads.ApplicationSummary app = applicationSummary();
        return List.of(new DashboardPayloads.MonitoringLink("Spring Boot Admin", "실시간 애플리케이션 상태와 엔드포인트를 확인합니다.", app.adminUrl(), "admin"), new DashboardPayloads.MonitoringLink("Actuator 헬스 그룹", "DB, Redis, 커스텀 상태를 한 번에 확인합니다.", app.actuatorBaseUrl() + "/health/infrastructure", "actuator"), new DashboardPayloads.MonitoringLink("Prometheus", "호스트, PostgreSQL, Redis 지표를 시계열로 저장합니다.", app.prometheusUrl(), "prometheus"), new DashboardPayloads.MonitoringLink("Grafana", "시간별, 일간, 주간, 월간 차트를 시각화합니다.", app.grafanaUrl(), "grafana"));
    }

    private List<DashboardPayloads.InfrastructureStatus> infrastructureStatuses() {
        return this.infrastructureProbeService.snapshots().stream().map((snapshot) -> new DashboardPayloads.InfrastructureStatus(snapshot.component(), snapshot.status(), snapshot.availability(), snapshot.latencyMs(), snapshot.lastCheckedAt().toString(), snapshot.detail())).toList();
    }

    private DashboardPayloads.ServerStatus serverStatus() {
        HostSystemObservationService.HostSnapshot host = this.hostSystemObservationService.snapshot();
        return new DashboardPayloads.ServerStatus(host.hostName(), host.operatingSystemFamily(), host.operatingSystem(), host.architecture(), host.javaRuntime(), host.availableProcessors(), host.totalMemoryBytes(), host.usedMemoryBytes(), host.freeMemoryBytes(), host.memoryUsagePercent(), host.totalDiskBytes(), host.usedDiskBytes(), host.freeDiskBytes(), host.diskUsagePercent(), host.diskPath(), host.capturedAt().toString());
    }

    private List<DashboardPayloads.StoreUsage> storageStatuses(DashboardWindow window) {
        try {
            DashboardSnapshotJdbcRepository.StoreBaseline baseline = this.snapshotRepository.findStoreBaseline(window);
            return this.connectedStoreObservationService.snapshots().stream().map((snapshot) -> new DashboardPayloads.StoreUsage(snapshot.component(), snapshot.status(), snapshot.version(), snapshot.usedBytes(), snapshot.limitBytes(), snapshot.freeBytes(), snapshot.usagePercent(), calculateGrowth(snapshot.component(), snapshot.usedBytes(), baseline), window.rangeLabel(), snapshot.capturedAt().toString(), snapshot.detail())).toList();
        } catch (Exception exception) {
            log.warn("스토어 기간 증감 계산에 실패했습니다. 실시간 스냅샷 값으로 대체합니다.", exception);
            return this.connectedStoreObservationService.snapshots().stream().map((snapshot) -> new DashboardPayloads.StoreUsage(snapshot.component(), snapshot.status(), snapshot.version(), snapshot.usedBytes(), snapshot.limitBytes(), snapshot.freeBytes(), snapshot.usagePercent(), snapshot.dailyGrowthBytes(), "최근 24시간", snapshot.capturedAt().toString(), snapshot.detail())).toList();
        }
    }

    private List<DashboardPayloads.TimeWindow> timeWindows() {
        return List.of(new DashboardPayloads.TimeWindow("실시간", "최근 15초 ~ 5분", "현재 호스트 메모리, 디스크, DB, Redis 값을 점검합니다.", "monitoring_host_memory_used_bytes"), new DashboardPayloads.TimeWindow("시간별", "최근 1시간", "요청량과 응답 지연, 호스트 메모리 변화를 함께 추적합니다.", "sum(rate(monitoring_synthetic_requests_total[5m]))"), new DashboardPayloads.TimeWindow("일간", "최근 24시간", "하루 동안 PostgreSQL과 Redis가 얼마나 증가했는지 확인합니다.", "monitoring_storage_daily_growth_bytes{component=\"postgresql\"}"), new DashboardPayloads.TimeWindow("주간", "최근 7일", "호스트 디스크와 PostgreSQL 크기 추세를 비교합니다.", "max_over_time(monitoring_storage_used_bytes{component=\"postgresql\"}[7d])"), new DashboardPayloads.TimeWindow("월간", "최근 30일", "장기 스토리지 증가와 디스크 압박을 검토합니다.", "max_over_time(monitoring_host_disk_used_bytes[30d])"));
    }

    private List<DashboardPayloads.SectionProgress> sectionProgress() {
        return this.courseCatalog.sections().stream().map((section) -> new DashboardPayloads.SectionProgress(section.title(), section.chapters(), section.durationMinutes(), section.completionRate(), section.focus(), section.accent())).toList();
    }

    private List<DashboardPayloads.MetricStat> highlightedMetrics(List<DashboardPayloads.StoreUsage> storage, DashboardWindow window) {
        List<DashboardPayloads.MetricStat> stats = new ArrayList<>();
        List<String> metricNames = List.of("jvm.memory.used", "process.uptime", "system.cpu.usage", "monitoring.active.sessions", "monitoring.alert.queue", "monitoring.synthetic.latency", "monitoring.admin.page.views", "monitoring.host.memory.used", "monitoring.host.disk.used", "monitoring.storage.used");
        for (String metricName : metricNames) {
            MetricsEndpoint.MetricDescriptor descriptor = metricDescriptor(metricName);
            if (descriptor == null) continue;
            descriptor.getMeasurements().stream().limit(3).map((sample) -> new DashboardPayloads.MetricStat(descriptor.getName(), descriptor.getDescription() == null ? "설명 없음" : descriptor.getDescription(), sample.getStatistic().name(), sample.getValue(), descriptor.getBaseUnit() == null ? "" : descriptor.getBaseUnit())).forEach(stats::add);
        }
        for (DashboardPayloads.StoreUsage store : storage) {
            String component = store.component().toLowerCase(Locale.ROOT);
            if (store.usedBytes() != null) stats.add(new DashboardPayloads.MetricStat("monitoring.storage.used{component=\"" + component + "\"}", "연결된 스토어의 현재 저장공간 또는 메모리 사용량입니다.", "VALUE", store.usedBytes(), "bytes"));
            stats.add(new DashboardPayloads.MetricStat("dashboard.storage.growth{component=\"" + component + "\",window=\"" + window.key() + "\"}", store.growthLabel() + " 기준 스토어 증감입니다.", "VALUE", store.dailyGrowthBytes(), "bytes"));
        }
        DashboardPayloads.ServerStatus server = serverStatus();
        stats.add(new DashboardPayloads.MetricStat("monitoring.host.memory.usage", "현재 호스트 물리 메모리 사용률입니다.", "VALUE", server.memoryUsagePercent(), ""));
        stats.add(new DashboardPayloads.MetricStat("monitoring.host.disk.usage", "현재 호스트 디스크 사용률입니다.", "VALUE", server.diskUsagePercent(), ""));
        return stats;
    }

    private MetricsEndpoint.MetricDescriptor metricDescriptor(String metricName) {
        return this.metricsEndpoint.metric(metricName, null);
    }

    private List<DashboardPayloads.DistributionItem> healthBreakdown(DashboardPayloads.HealthSnapshot health) {
        Map<String, Long> counts = health.components().stream().collect(Collectors.groupingBy(DashboardPayloads.ComponentStatus::status, LinkedHashMap::new, Collectors.counting()));
        if (counts.isEmpty()) counts = Map.of(health.status(), 1L);
        return counts.entrySet().stream().map((entry) -> new DashboardPayloads.DistributionItem(entry.getKey(), entry.getValue(), statusAccent(entry.getKey()))).toList();
    }

    private List<DashboardPayloads.DistributionItem> timerPercentiles(List<DashboardPayloads.TrendPoint> latencyTrend) {
        List<Double> values = latencyTrend.stream().map(DashboardPayloads.TrendPoint::value).sorted().toList();
        return List.of(new DashboardPayloads.DistributionItem("P50", percentile(values, 0.50), "#7dd3fc"), new DashboardPayloads.DistributionItem("P90", percentile(values, 0.90), "#fbbf24"), new DashboardPayloads.DistributionItem("P95", percentile(values, 0.95), "#fb7185"));
    }

    private List<DashboardPayloads.TagSummary> availableTags() {
        LinkedHashSet<DashboardPayloads.TagSummary> tags = new LinkedHashSet<>();
        List<String> metrics = List.of("monitoring.admin.page.views", "monitoring.section.completion", "monitoring.infrastructure.availability", "monitoring.storage.used");
        for (String metric : metrics) {
            MetricsEndpoint.MetricDescriptor descriptor = metricDescriptor(metric);
            if (descriptor == null) continue;
            descriptor.getAvailableTags().stream().map(tag -> new DashboardPayloads.TagSummary(tag.getTag(), List.copyOf(tag.getValues()))).forEach(tags::add);
        }
        tags.add(new DashboardPayloads.TagSummary("component", List.of("db", "redis", "postgresql")));
        tags.add(new DashboardPayloads.TagSummary("window", List.of("15s", "1h", "24h", "7d", "30d")));
        return List.copyOf(tags);
    }

    private WindowedTrendSeries loadWindowedTrendSeries(DashboardWindow window) {
        try {
            List<DashboardSnapshotJdbcRepository.TrendBucket> buckets = this.snapshotRepository.findTrendBuckets(window);
            if (buckets.isEmpty()) return fallbackTrends();
            return new WindowedTrendSeries(buckets.stream().map((bucket) -> new DashboardPayloads.TrendPoint(window.formatLabel(bucket.capturedAt()), roundWhole(bucket.requestPerMinute()))).toList(), buckets.stream().map((bucket) -> new DashboardPayloads.TrendPoint(window.formatLabel(bucket.capturedAt()), roundWhole(bucket.latencyMs()))).toList(), buckets.stream().map((bucket) -> new DashboardPayloads.TrendPoint(window.formatLabel(bucket.capturedAt()), roundSingle(bucket.cacheHitRate()))).toList());
        } catch (Exception exception) {
            log.warn("기간별 스냅샷 조회에 실패했습니다. 메모리 추세로 대체합니다.", exception);
            return fallbackTrends();
        }
    }

    private WindowedTrendSeries fallbackTrends() {
        return new WindowedTrendSeries(snapshot(this.requestTrend), snapshot(this.latencyTrend), snapshot(this.cacheTrend));
    }

    private void persistCurrentSnapshotSafely() {
        try {
            this.snapshotRepository.save(currentSnapshotRecord());
        } catch (Exception exception) {
            log.warn("모니터링 스냅샷 저장에 실패했습니다.", exception);
        }
    }

    private DashboardSnapshotJdbcRepository.DashboardSnapshotRecord currentSnapshotRecord() {
        DashboardPayloads.ServerStatus server = serverStatus();
        Map<String, ConnectedStoreObservationService.StoreSnapshot> stores = this.connectedStoreObservationService.snapshots().stream().collect(Collectors.toMap((snapshot) -> snapshot.component().toLowerCase(Locale.ROOT), snapshot -> snapshot, (left, right) -> right, LinkedHashMap::new));
        Map<String, InfrastructureProbeService.ProbeSnapshot> probes = this.infrastructureProbeService.snapshots().stream().collect(Collectors.toMap((snapshot) -> snapshot.component().toLowerCase(Locale.ROOT), snapshot -> snapshot, (left, right) -> right, LinkedHashMap::new));
        ConnectedStoreObservationService.StoreSnapshot postgresql = stores.get("postgresql");
        ConnectedStoreObservationService.StoreSnapshot redis = stores.get("redis");
        InfrastructureProbeService.ProbeSnapshot databaseProbe = probes.get("postgresql");
        InfrastructureProbeService.ProbeSnapshot redisProbe = probes.get("redis");
        return new DashboardSnapshotJdbcRepository.DashboardSnapshotRecord(Instant.now(), (int) Math.round(latestValue(this.requestTrend)), latestValue(this.latencyTrend), this.cacheHitRate.get(), this.activeSessions.get(), this.alertQueue.get(), server.memoryUsagePercent(), server.diskUsagePercent(), server.usedMemoryBytes(), server.totalMemoryBytes(), server.usedDiskBytes(), server.totalDiskBytes(), postgresql == null ? null : postgresql.usedBytes(), redis == null ? null : redis.usedBytes(), redis == null ? null : redis.limitBytes(), databaseProbe == null ? 0.0 : databaseProbe.availability(), databaseProbe == null ? 0.0 : databaseProbe.latencyMs(), redisProbe == null ? 0.0 : redisProbe.availability(), redisProbe == null ? 0.0 : redisProbe.latencyMs());
    }

    private long calculateGrowth(String component, Long currentBytes, DashboardSnapshotJdbcRepository.StoreBaseline baseline) {
        if (currentBytes == null || baseline == null) return 0L;
        Long baselineBytes = switch (component.toLowerCase(Locale.ROOT)) {
            case "postgresql" -> baseline.postgresqlUsedBytes();
            case "redis" -> baseline.redisUsedBytes();
            default -> null;
        };
        return baselineBytes == null ? 0L : currentBytes - baselineBytes;
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

    private List<DashboardPayloads.TrendPoint> snapshot(ConcurrentLinkedDeque<DashboardPayloads.TrendPoint> source) {
        return List.copyOf(source);
    }

    private double latestValue(ConcurrentLinkedDeque<DashboardPayloads.TrendPoint> source) {
        DashboardPayloads.TrendPoint latest = source.peekLast();
        return latest == null ? 0.0 : latest.value();
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }

    private static String prettify(String raw) {
        return raw.replace('-', ' ').replace('_', ' ').replaceAll("([a-z])([A-Z])", "$1 $2").strip();
    }

    private static String statusTone(String status) {
        return switch (status.toUpperCase(Locale.ROOT)) {
            case "UP" -> "success";
            case "DOWN", "OUT_OF_SERVICE" -> "danger";
            default -> "warning";
        };
    }

    private static String statusAccent(String status) {
        return switch (status.toUpperCase(Locale.ROOT)) {
            case "UP" -> "#a3e635";
            case "DOWN" -> "#fb7185";
            case "OUT_OF_SERVICE" -> "#f97316";
            default -> "#7dd3fc";
        };
    }

    private static String formatBytes(long bytes) {
        if (bytes <= 0L) return "0 B";
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        double value = bytes;
        int index = 0;
        while (value >= 1024.0 && index < units.length - 1) {
            value /= 1024.0;
            index++;
        }
        return String.format(Locale.US, "%.1f %s", value, units[index]);
    }

    private static String signedBytes(long bytes) {
        return (bytes >= 0L ? "+" : "-") + formatBytes(Math.abs(bytes));
    }

    private static double roundWhole(double value) {
        return Math.round(value);
    }

    private static double roundSingle(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private static String localizeHealthComponentName(String raw) {
        String normalized = raw.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
        return switch (normalized) {
            case "db", "database" -> "데이터베이스";
            case "redis" -> "레디스";
            case "ping" -> "핑";
            case "diskspace" -> "디스크 공간";
            case "coursecoverage" -> "학습 진행률";
            case "livenessstate" -> "라이브니스 상태";
            case "readinessstate" -> "레디니스 상태";
            default -> prettify(raw);
        };
    }

    private static String localizeHealthDetailKey(String raw) {
        return switch (raw) {
            case "database" -> "데이터베이스";
            case "version" -> "버전";
            case "averageCompletion" -> "평균 완료율";
            case "sectionCount" -> "섹션 수";
            case "totalMinutes" -> "총 학습 시간";
            case "lowestSection" -> "가장 낮은 진행 섹션";
            case "free" -> "여유 공간";
            case "path" -> "경로";
            case "exists" -> "존재 여부";
            case "responseTime" -> "응답 시간";
            default -> raw;
        };
    }
}
