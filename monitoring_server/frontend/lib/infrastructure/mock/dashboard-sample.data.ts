import type {
  ActuatorSummaryPayload,
  ApplicationSummary,
  DistributionItem,
  InfrastructureStatus,
  MonitoringLink,
  OverviewPayload,
  SectionProgress,
  ServerStatus,
  StatisticsPayload,
  StoreUsage,
  TimeWindow,
  TrendPoint,
} from "@/lib/domain/monitoring.read-models";

const apiBaseUrl = process.env.MONITORING_API_BASE_URL ?? "http://localhost:8080";
const actuatorBaseUrl = process.env.MONITORING_ACTUATOR_BASE_URL ?? "http://localhost:8081/actuator";
const adminBaseUrl = process.env.SPRING_BOOT_ADMIN_BASE_URL ?? "http://localhost:8080";
const prometheusBaseUrl = "http://localhost:9091";
const grafanaBaseUrl = "http://localhost:3001";

function application(): ApplicationSummary {
  return {
    name: "spring-monitoring-server",
    version: "0.0.1-SNAPSHOT",
    springBootVersion: "4.0.3",
    javaVersion: "25",
    environment: "default",
    backendUrl: apiBaseUrl,
    actuatorBaseUrl,
    adminUrl: adminBaseUrl,
    prometheusUrl: prometheusBaseUrl,
    grafanaUrl: grafanaBaseUrl,
  };
}

function trend(labels: string[], values: number[]): TrendPoint[] {
  return labels.map((label, index) => ({ label, value: values[index] ?? 0 }));
}

function sections(): SectionProgress[] {
  return [
    { title: "Endpoint Basics", chapters: 3, durationMinutes: 44, completionRate: 82, focus: "Dependency setup and custom endpoint", accent: "#7dd3fc" },
    { title: "Health and Info", chapters: 2, durationMinutes: 48, completionRate: 88, focus: "Availability and metadata", accent: "#a3e635" },
    { title: "Metrics Deep Dive 1", chapters: 4, durationMinutes: 59, completionRate: 91, focus: "Counter and tags", accent: "#fbbf24" },
    { title: "Metrics Deep Dive 2", chapters: 4, durationMinutes: 61, completionRate: 86, focus: "Gauge, timer, percentile", accent: "#fb7185" },
    { title: "Spring Boot Admin", chapters: 2, durationMinutes: 36, completionRate: 90, focus: "Central monitoring UI", accent: "#38bdf8" },
  ];
}

function monitoringLinks(): MonitoringLink[] {
  return [
    { title: "Spring Boot Admin", description: "Live application state and endpoint view.", url: adminBaseUrl, kind: "admin" },
    { title: "Actuator Health Group", description: "DB, Redis, and custom health in one endpoint.", url: `${actuatorBaseUrl}/health/infrastructure`, kind: "actuator" },
    { title: "Prometheus", description: "Time-series store for host and storage metrics.", url: prometheusBaseUrl, kind: "prometheus" },
    { title: "Grafana", description: "Visual charts for hour, day, week, and month windows.", url: grafanaBaseUrl, kind: "grafana" },
  ];
}

function infrastructure(): InfrastructureStatus[] {
  return [
    { component: "PostgreSQL", status: "UP", availability: 1, latencyMs: 8, lastCheckedAt: "2026-04-04T10:25:00+09:00", detail: "SELECT 1 probe succeeded." },
    { component: "Redis", status: "UP", availability: 1, latencyMs: 4, lastCheckedAt: "2026-04-04T10:25:00+09:00", detail: "PING probe returned PONG." },
  ];
}

function server(): ServerStatus {
  return {
    hostName: "idolglow-node-01",
    operatingSystemFamily: "WINDOWS",
    operatingSystem: "Windows Server 2025",
    architecture: "amd64",
    javaRuntime: "OpenJDK 25",
    availableProcessors: 12,
    totalMemoryBytes: 34_359_738_368,
    usedMemoryBytes: 18_421_080_064,
    freeMemoryBytes: 15_938_658_304,
    memoryUsagePercent: 53.6,
    totalDiskBytes: 1_024_000_000_000,
    usedDiskBytes: 612_000_000_000,
    freeDiskBytes: 412_000_000_000,
    diskUsagePercent: 59.8,
    diskPath: "C:\\",
    capturedAt: "2026-04-04T10:25:00+09:00",
  };
}

function storage(): StoreUsage[] {
  return [
    {
      component: "PostgreSQL",
      status: "UP",
      version: "17.2",
      usedBytes: 4_289_699_840,
      limitBytes: null,
      freeBytes: null,
      usagePercent: null,
      dailyGrowthBytes: 214_958_080,
      capturedAt: "2026-04-04T10:25:00+09:00",
      detail: "database=monitoring_db, 24h delta from in-app snapshots.",
    },
    {
      component: "Redis",
      status: "UP",
      version: "7.4.1",
      usedBytes: 356_515_840,
      limitBytes: 1_073_741_824,
      freeBytes: 717_225_984,
      usagePercent: 33.2,
      dailyGrowthBytes: 44_040_192,
      capturedAt: "2026-04-04T10:25:00+09:00",
      detail: "os=Linux 6.x, hits=245832, misses=2137, maxmemory is configured.",
    },
  ];
}

function timeWindows(): TimeWindow[] {
  return [
    { label: "Real-time", range: "Last 15 seconds to 5 minutes", purpose: "Inspect current host memory, disk, DB, and Redis values.", query: "monitoring_host_memory_used_bytes" },
    { label: "Hourly", range: "Last 1 hour", purpose: "Track request rate and latency with host memory changes.", query: "sum(rate(monitoring_synthetic_requests_total[5m]))" },
    { label: "Daily", range: "Last 24 hours", purpose: "Check how much PostgreSQL and Redis grew during the day.", query: 'monitoring_storage_daily_growth_bytes{component="postgresql"}' },
    { label: "Weekly", range: "Last 7 days", purpose: "Compare host disk and PostgreSQL size trends.", query: 'max_over_time(monitoring_storage_used_bytes{component="postgresql"}[7d])' },
    { label: "Monthly", range: "Last 30 days", purpose: "Review long-term storage growth and disk pressure.", query: "max_over_time(monitoring_host_disk_used_bytes[30d])" },
  ];
}

function healthBreakdown(): DistributionItem[] {
  return [
    { label: "UP", value: 5, accent: "#a3e635" },
    { label: "UNKNOWN", value: 1, accent: "#7dd3fc" },
  ];
}

export function createMockOverview(): OverviewPayload {
  return {
    generatedAt: "2026-04-04T10:25:00+09:00",
    application: application(),
    health: {
      status: "UP",
      uptimeMinutes: 37,
      components: [
        { name: "db", status: "UP", detail: "database=PostgreSQL" },
        { name: "redis", status: "UP", detail: "version=7.4.x" },
        { name: "courseCoverage", status: "UP", detail: "averageCompletion=87.4" },
        { name: "diskSpace", status: "UP", detail: "free=412GB" },
        { name: "ping", status: "UP", detail: "responseTime=3ms" },
      ],
    },
    kpis: [
      { label: "Overall Health", value: "UP", caption: "Actuator health, DB, Redis, custom contributor", tone: "success" },
      { label: "Server OS", value: "WINDOWS", caption: "idolglow-node-01 / Windows Server 2025", tone: "info" },
      { label: "Host Memory", value: "53.6%", caption: "17.2 GB / 32.0 GB", tone: "success" },
      { label: "Host Disk", value: "59.8%", caption: "570.0 GB / 953.7 GB", tone: "success" },
      { label: "PostgreSQL", value: "4.0 GB", caption: "24h delta +205.0 MB", tone: "success" },
      { label: "Redis", value: "340.0 MB", caption: "24h delta +42.0 MB / usage 33.2%", tone: "success" },
    ],
    requestTrend: trend(["09:00", "09:05", "09:10", "09:15", "09:20", "09:25"], [142, 156, 172, 168, 190, 184]),
    latencyTrend: trend(["09:00", "09:05", "09:10", "09:15", "09:20", "09:25"], [114, 118, 127, 121, 136, 126]),
    cacheTrend: trend(["09:00", "09:05", "09:10", "09:15", "09:20", "09:25"], [88, 89, 91, 93, 92, 94]),
    sections: sections(),
    actuatorEndpoints: [
      { id: "health", description: "Application overall health", path: `${actuatorBaseUrl}/health`, exposed: true },
      { id: "health/infrastructure", description: "DB, Redis, custom health group", path: `${actuatorBaseUrl}/health/infrastructure`, exposed: true },
      { id: "info", description: "Runtime and monitoring metadata", path: `${actuatorBaseUrl}/info`, exposed: true },
      { id: "metrics", description: "Micrometer metric catalog", path: `${actuatorBaseUrl}/metrics`, exposed: true },
      { id: "prometheus", description: "Prometheus scrape endpoint", path: `${actuatorBaseUrl}/prometheus`, exposed: true },
      { id: "course-monitoring", description: "Custom monitoring endpoint", path: `${actuatorBaseUrl}/course-monitoring`, exposed: true },
      { id: "spring-boot-admin", description: "Live Spring Boot Admin UI", path: adminBaseUrl, exposed: true },
    ],
    monitoringLinks: monitoringLinks(),
    infrastructure: infrastructure(),
    server: server(),
    storage: storage(),
    highlights: [
      "See Windows or Linux host details together with current memory and disk usage.",
      "PostgreSQL shows database size and recent 24 hour growth.",
      "Redis shows used memory, configured limit, and recent 24 hour growth.",
      "Prometheus and Grafana extend the same metrics into long-term history.",
    ],
  };
}

export function createMockStatistics(): StatisticsPayload {
  return {
    generatedAt: "2026-04-04T10:25:00+09:00",
    application: application(),
    highlightedMetrics: [
      { name: "monitoring.host.memory.used", description: "Used physical memory of the host", statistic: "VALUE", value: 18_421_080_064, unit: "bytes" },
      { name: "monitoring.host.disk.used", description: "Used disk size of the host filesystem", statistic: "VALUE", value: 612_000_000_000, unit: "bytes" },
      { name: 'monitoring.storage.used{component="postgresql"}', description: "Current storage usage of PostgreSQL", statistic: "VALUE", value: 4_289_699_840, unit: "bytes" },
      { name: 'monitoring.storage.daily.growth{component="postgresql"}', description: "Recent 24 hour PostgreSQL growth", statistic: "VALUE", value: 214_958_080, unit: "bytes" },
      { name: 'monitoring.storage.used{component="redis"}', description: "Current memory usage of Redis", statistic: "VALUE", value: 356_515_840, unit: "bytes" },
      { name: 'monitoring.storage.daily.growth{component="redis"}', description: "Recent 24 hour Redis growth", statistic: "VALUE", value: 44_040_192, unit: "bytes" },
      { name: "monitoring.synthetic.latency", description: "Synthetic latency timer", statistic: "MAX", value: 182, unit: "milliseconds" },
      { name: "system.cpu.usage", description: "Recent CPU usage", statistic: "VALUE", value: 0.34, unit: "" },
    ],
    healthBreakdown: healthBreakdown(),
    timerPercentiles: [
      { label: "P50", value: 121, accent: "#7dd3fc" },
      { label: "P90", value: 154, accent: "#fbbf24" },
      { label: "P95", value: 169, accent: "#fb7185" },
    ],
    tags: [
      { key: "page", values: ["overview", "statistics", "actuator"] },
      { key: "section", values: ["section-2", "section-3", "section-4", "section-5", "section-6"] },
      { key: "component", values: ["db", "redis", "postgresql"] },
    ],
    requestTrend: trend(["09:00", "09:05", "09:10", "09:15", "09:20", "09:25"], [142, 156, 172, 168, 190, 184]),
    latencyTrend: trend(["09:00", "09:05", "09:10", "09:15", "09:20", "09:25"], [114, 118, 127, 121, 136, 126]),
    sections: sections(),
    infrastructure: infrastructure(),
    server: server(),
    storage: storage(),
    timeWindows: timeWindows(),
    notes: [
      "monitoring.host.* exposes the current application host resources.",
      "monitoring.storage.used and monitoring.storage.daily.growth expose current size and recent 24 hour change.",
      "Prometheus is the right place to keep the same values for day, week, and month views.",
    ],
  };
}

export function createMockActuatorSummary(): ActuatorSummaryPayload {
  return {
    generatedAt: "2026-04-04T10:25:00+09:00",
    application: application(),
    health: {
      status: "UP",
      uptimeMinutes: 37,
      components: [
        { name: "db", status: "UP", detail: "database=PostgreSQL" },
        { name: "redis", status: "UP", detail: "version=7.4.x" },
        { name: "courseCoverage", status: "UP", detail: "averageCompletion=87.4" },
      ],
    },
    info: {
      app: { name: "IdolGlow Spring Monitoring", description: "Monitoring dashboard demo", java: { target: 25 } },
      serverEnvironment: {
        hostName: "idolglow-node-01",
        operatingSystemFamily: "WINDOWS",
        operatingSystem: "Windows Server 2025",
        architecture: "amd64",
      },
      connectedStores: {
        PostgreSQL: { status: "UP", usedBytes: 4_289_699_840, dailyGrowthBytes: 214_958_080 },
        Redis: { status: "UP", usedBytes: 356_515_840, dailyGrowthBytes: 44_040_192 },
      },
      observability: {
        actuator: actuatorBaseUrl,
        prometheus: prometheusBaseUrl,
        grafana: grafanaBaseUrl,
      },
    },
    customEndpoint: {
      generatedAt: "2026-04-04T10:25:00+09:00",
      averageCompletion: 87.4,
      totalMinutes: 248,
      liveGauges: { activeSessions: 46, alertQueue: 3, cacheHitRate: 91 },
      server: server(),
      storage: storage(),
      infrastructure: infrastructure(),
      recommendedChecks: [
        "/actuator/health",
        "/actuator/health/infrastructure",
        "/actuator/metrics/monitoring.host.memory.used",
        "/actuator/metrics/monitoring.storage.used",
        "/actuator/prometheus",
      ],
    },
    metricNames: [
      "monitoring.host.memory.used",
      "monitoring.host.disk.used",
      "monitoring.storage.used",
      "monitoring.storage.daily.growth",
      "monitoring.infrastructure.availability",
      "monitoring.synthetic.latency",
      "jvm.memory.used",
      "system.cpu.usage",
    ],
    links: [
      { id: "health", description: "Application overall health", path: `${actuatorBaseUrl}/health`, exposed: true },
      { id: "health/infrastructure", description: "DB, Redis, custom health group", path: `${actuatorBaseUrl}/health/infrastructure`, exposed: true },
      { id: "info", description: "Runtime and monitoring metadata", path: `${actuatorBaseUrl}/info`, exposed: true },
      { id: "metrics", description: "Micrometer metric catalog", path: `${actuatorBaseUrl}/metrics`, exposed: true },
      { id: "prometheus", description: "Prometheus scrape endpoint", path: `${actuatorBaseUrl}/prometheus`, exposed: true },
      { id: "course-monitoring", description: "Custom monitoring endpoint", path: `${actuatorBaseUrl}/course-monitoring`, exposed: true },
      { id: "spring-boot-admin", description: "Live Spring Boot Admin UI", path: adminBaseUrl, exposed: true },
    ],
    monitoringLinks: monitoringLinks(),
    infrastructure: infrastructure(),
    server: server(),
    storage: storage(),
    timeWindows: timeWindows(),
  };
}
