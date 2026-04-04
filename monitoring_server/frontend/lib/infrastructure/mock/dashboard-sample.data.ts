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
import {
  deriveTimerPercentiles,
  deriveTrendPoints,
  getActiveWindowMeta,
  type DashboardWindowKey,
} from "@/lib/windowing";

const apiBaseUrl = process.env.MONITORING_API_BASE_URL ?? "http://localhost:8080";
const actuatorBaseUrl = process.env.MONITORING_ACTUATOR_BASE_URL ?? "http://localhost:8081/actuator";
const adminBaseUrl = process.env.SPRING_BOOT_ADMIN_BASE_URL ?? "http://localhost:8080";
const prometheusBaseUrl = "http://localhost:9091";
const grafanaBaseUrl = "http://localhost:3001";
const baseRequestTrend = trend(["09:00", "09:05", "09:10", "09:15", "09:20", "09:25"], [142, 156, 172, 168, 190, 184]);
const baseLatencyTrend = trend(["09:00", "09:05", "09:10", "09:15", "09:20", "09:25"], [114, 118, 127, 121, 136, 126]);
const baseCacheTrend = trend(["09:00", "09:05", "09:10", "09:15", "09:20", "09:25"], [88, 89, 91, 93, 92, 94]);
const growthFactors: Record<DashboardWindowKey, number> = {
  "15s": 0.05,
  "1h": 0.2,
  "24h": 1,
  "7d": 4.8,
  "30d": 12,
};

function application(): ApplicationSummary {
  return {
    name: "spring-monitoring-server",
    version: "0.0.1-SNAPSHOT",
    springBootVersion: "4.0.3",
    javaVersion: "25",
    environment: "기본",
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
    { title: "엔드포인트 기초와 설정", chapters: 3, durationMinutes: 44, completionRate: 82, focus: "노출 범위, 의존성 설정, 커스텀 엔드포인트", accent: "#7dd3fc" },
    { title: "Health / Info 엔드포인트", chapters: 2, durationMinutes: 48, completionRate: 88, focus: "상태 점검과 운영 정보 공개", accent: "#a3e635" },
    { title: "메트릭 심화 1", chapters: 4, durationMinutes: 59, completionRate: 91, focus: "카운터, 태그, 공통 태그", accent: "#fbbf24" },
    { title: "메트릭 심화 2", chapters: 4, durationMinutes: 61, completionRate: 86, focus: "게이지, 타이머, @Timed, 백분위", accent: "#fb7185" },
    { title: "Spring Boot Admin 연동", chapters: 2, durationMinutes: 36, completionRate: 90, focus: "중앙 모니터링 UI와 운영 흐름", accent: "#38bdf8" },
  ];
}

function monitoringLinks(): MonitoringLink[] {
  return [
    { title: "Spring Boot Admin", description: "실시간 애플리케이션 상태와 엔드포인트를 확인합니다.", url: adminBaseUrl, kind: "admin" },
    { title: "Actuator 헬스 그룹", description: "DB, Redis, 커스텀 상태를 한 엔드포인트에서 확인합니다.", url: `${actuatorBaseUrl}/health/infrastructure`, kind: "actuator" },
    { title: "Prometheus", description: "호스트와 스토어 지표를 시계열로 저장합니다.", url: prometheusBaseUrl, kind: "prometheus" },
    { title: "Grafana", description: "시간별, 일간, 주간, 월간 차트를 시각화합니다.", url: grafanaBaseUrl, kind: "grafana" },
  ];
}

function infrastructure(): InfrastructureStatus[] {
  return [
    { component: "PostgreSQL", status: "UP", availability: 1, latencyMs: 8, lastCheckedAt: "2026-04-04T10:25:00+09:00", detail: "SELECT 1 프로브에 성공했습니다." },
    { component: "Redis", status: "UP", availability: 1, latencyMs: 4, lastCheckedAt: "2026-04-04T10:25:00+09:00", detail: "PING 프로브가 PONG 응답을 반환했습니다." },
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

function storage(window: DashboardWindowKey = "24h"): StoreUsage[] {
  const activeMeta = getActiveWindowMeta(window);
  const factor = growthFactors[window];
  return [
    {
      component: "PostgreSQL",
      status: "UP",
      version: "17.2",
      usedBytes: 4_289_699_840,
      limitBytes: null,
      freeBytes: null,
      usagePercent: null,
      dailyGrowthBytes: Math.round(214_958_080 * factor),
      growthLabel: activeMeta.rangeLabel,
      capturedAt: "2026-04-04T10:25:00+09:00",
      detail: "데이터베이스=monitoring_db, 증감 값은 대시보드 스냅샷 기준으로 계산됩니다.",
    },
    {
      component: "Redis",
      status: "UP",
      version: "7.4.1",
      usedBytes: 356_515_840,
      limitBytes: 1_073_741_824,
      freeBytes: 717_225_984,
      usagePercent: 33.2,
      dailyGrowthBytes: Math.round(44_040_192 * factor),
      growthLabel: activeMeta.rangeLabel,
      capturedAt: "2026-04-04T10:25:00+09:00",
      detail: "운영체제=Linux 6.x, hits=245832, misses=2137, maxmemory가 설정되어 있습니다.",
    },
  ];
}

function timeWindows(): TimeWindow[] {
  return [
    { label: "실시간", range: "최근 15초 ~ 5분", purpose: "현재 호스트 메모리, 디스크, DB, Redis 값을 점검합니다.", query: "monitoring_host_memory_used_bytes" },
    { label: "시간별", range: "최근 1시간", purpose: "요청량과 응답 지연, 호스트 메모리 변화를 함께 추적합니다.", query: "sum(rate(monitoring_synthetic_requests_total[5m]))" },
    { label: "일간", range: "최근 24시간", purpose: "하루 동안 PostgreSQL과 Redis가 얼마나 증가했는지 확인합니다.", query: 'monitoring_storage_daily_growth_bytes{component="postgresql"}' },
    { label: "주간", range: "최근 7일", purpose: "호스트 디스크와 PostgreSQL 크기 추세를 비교합니다.", query: 'max_over_time(monitoring_storage_used_bytes{component="postgresql"}[7d])' },
    { label: "월간", range: "최근 30일", purpose: "장기 스토리지 증가와 디스크 압박을 검토합니다.", query: "max_over_time(monitoring_host_disk_used_bytes[30d])" },
  ];
}

function healthBreakdown(): DistributionItem[] {
  return [
    { label: "UP", value: 5, accent: "#a3e635" },
    { label: "UNKNOWN", value: 1, accent: "#7dd3fc" },
  ];
}

export function createMockOverview(window: DashboardWindowKey = "15s"): OverviewPayload {
  const requestTrend = deriveTrendPoints(baseRequestTrend, window, "request", "2026-04-04T10:25:00+09:00");
  const latencyTrend = deriveTrendPoints(baseLatencyTrend, window, "latency", "2026-04-04T10:25:00+09:00");
  const cacheTrend = deriveTrendPoints(baseCacheTrend, window, "percent", "2026-04-04T10:25:00+09:00");
  const storeSnapshots = storage(window);
  const postgresql = storeSnapshots[0];
  const redis = storeSnapshots[1];
  return {
    generatedAt: "2026-04-04T10:25:00+09:00",
    application: application(),
    health: {
      status: "UP",
      uptimeMinutes: 37,
      components: [
        { name: "db", status: "UP", detail: "데이터베이스=PostgreSQL" },
        { name: "redis", status: "UP", detail: "버전=7.4.x" },
        { name: "courseCoverage", status: "UP", detail: "평균 완료율=87.4" },
        { name: "diskSpace", status: "UP", detail: "여유 공간=412GB" },
        { name: "ping", status: "UP", detail: "응답 시간=3ms" },
      ],
    },
    kpis: [
      { label: "전체 상태", value: "UP", caption: "Actuator 상태와 DB, Redis, 커스텀 상태 기여자", tone: "success" },
      { label: "서버 운영체제", value: "WINDOWS", caption: "idolglow-node-01 / Windows Server 2025", tone: "info" },
      { label: "호스트 메모리", value: "53.6%", caption: "17.2 GB / 32.0 GB", tone: "success" },
      { label: "호스트 디스크", value: "59.8%", caption: "570.0 GB / 953.7 GB", tone: "success" },
      { label: "PostgreSQL", value: "4.0 GB", caption: `${postgresql.growthLabel} 증감 +${(postgresql.dailyGrowthBytes / 1024 / 1024).toFixed(1)} MB`, tone: "success" },
      { label: "Redis", value: "340.0 MB", caption: `${redis.growthLabel} 증감 +${(redis.dailyGrowthBytes / 1024 / 1024).toFixed(1)} MB / 사용률 33.2%`, tone: "success" },
    ],
    requestTrend,
    latencyTrend,
    cacheTrend,
    sections: sections(),
    actuatorEndpoints: [
      { id: "health", description: "애플리케이션 전체 상태", path: `${actuatorBaseUrl}/health`, exposed: true },
      { id: "health/infrastructure", description: "DB, Redis, 커스텀 상태 그룹", path: `${actuatorBaseUrl}/health/infrastructure`, exposed: true },
      { id: "info", description: "런타임 및 모니터링 메타데이터", path: `${actuatorBaseUrl}/info`, exposed: true },
      { id: "metrics", description: "Micrometer 지표 카탈로그", path: `${actuatorBaseUrl}/metrics`, exposed: true },
      { id: "prometheus", description: "Prometheus 수집 엔드포인트", path: `${actuatorBaseUrl}/prometheus`, exposed: true },
      { id: "course-monitoring", description: "커스텀 모니터링 엔드포인트", path: `${actuatorBaseUrl}/course-monitoring`, exposed: true },
      { id: "spring-boot-admin", description: "실시간 Spring Boot Admin UI", path: adminBaseUrl, exposed: true },
    ],
    monitoringLinks: monitoringLinks(),
    infrastructure: infrastructure(),
    server: server(),
    storage: storeSnapshots,
    highlights: [
      "Windows 또는 Linux 호스트 정보와 현재 메모리, 디스크 사용량을 함께 확인할 수 있습니다.",
      "PostgreSQL은 데이터베이스 크기와 최근 24시간 증가량을 보여줍니다.",
      "Redis는 사용 메모리, 설정된 한도, 최근 24시간 증가량을 함께 보여줍니다.",
      "Prometheus와 Grafana는 같은 지표를 장기 이력으로 확장해 보여줍니다.",
    ],
  };
}

export function createMockStatistics(window: DashboardWindowKey = "15s"): StatisticsPayload {
  const requestTrend = deriveTrendPoints(baseRequestTrend, window, "request", "2026-04-04T10:25:00+09:00");
  const latencyTrend = deriveTrendPoints(baseLatencyTrend, window, "latency", "2026-04-04T10:25:00+09:00");
  const timerPercentiles = deriveTimerPercentiles(latencyTrend);
  const storeSnapshots = storage(window);
  return {
    generatedAt: "2026-04-04T10:25:00+09:00",
    application: application(),
    highlightedMetrics: [
      { name: "monitoring.host.memory.used", description: "호스트의 사용 중인 물리 메모리", statistic: "VALUE", value: 18_421_080_064, unit: "bytes" },
      { name: "monitoring.host.disk.used", description: "호스트 파일시스템의 사용 중인 디스크 크기", statistic: "VALUE", value: 612_000_000_000, unit: "bytes" },
      { name: 'monitoring.storage.used{component="postgresql"}', description: "PostgreSQL의 현재 저장공간 사용량", statistic: "VALUE", value: 4_289_699_840, unit: "bytes" },
      { name: 'dashboard.storage.growth{component="postgresql"}', description: `${storeSnapshots[0]?.growthLabel ?? "최근 24시간"} 기준 PostgreSQL 증가량`, statistic: "VALUE", value: storeSnapshots[0]?.dailyGrowthBytes ?? 0, unit: "bytes" },
      { name: 'monitoring.storage.used{component="redis"}', description: "Redis의 현재 메모리 사용량", statistic: "VALUE", value: 356_515_840, unit: "bytes" },
      { name: 'dashboard.storage.growth{component="redis"}', description: `${storeSnapshots[1]?.growthLabel ?? "최근 24시간"} 기준 Redis 증가량`, statistic: "VALUE", value: storeSnapshots[1]?.dailyGrowthBytes ?? 0, unit: "bytes" },
      { name: "monitoring.synthetic.latency", description: "합성 지연 시간 타이머", statistic: "MAX", value: 182, unit: "milliseconds" },
      { name: "system.cpu.usage", description: "최근 CPU 사용률", statistic: "VALUE", value: 0.34, unit: "" },
    ],
    healthBreakdown: healthBreakdown(),
    timerPercentiles,
    tags: [
      { key: "page", values: ["overview", "statistics", "actuator"] },
      { key: "section", values: ["section-2", "section-3", "section-4", "section-5", "section-6"] },
      { key: "component", values: ["db", "redis", "postgresql"] },
    ],
    requestTrend,
    latencyTrend,
    sections: sections(),
    infrastructure: infrastructure(),
    server: server(),
    storage: storeSnapshots,
    timeWindows: timeWindows(),
    notes: [
      "monitoring.host.* 는 현재 애플리케이션 호스트 자원을 노출합니다.",
      "스토어 사용량은 현재 크기와 선택한 기간 기준 증감을 함께 보여줍니다.",
      "일간, 주간, 월간 보기는 Prometheus와 Grafana에서 같은 값을 장기 보관해 확인하는 것이 적합합니다.",
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
        { name: "db", status: "UP", detail: "데이터베이스=PostgreSQL" },
        { name: "redis", status: "UP", detail: "버전=7.4.x" },
        { name: "courseCoverage", status: "UP", detail: "평균 완료율=87.4" },
      ],
    },
    info: {
      app: { name: "IdolGlow 스프링 모니터링", description: "모니터링 대시보드 데모", java: { target: 25 } },
      serverEnvironment: {
        hostName: "idolglow-node-01",
        operatingSystemFamily: "WINDOWS",
        operatingSystem: "Windows Server 2025",
        architecture: "amd64",
      },
      connectedStores: {
        PostgreSQL: { status: "UP", usedBytes: 4_289_699_840, dailyGrowthBytes: 214_958_080, growthLabel: "최근 24시간" },
        Redis: { status: "UP", usedBytes: 356_515_840, dailyGrowthBytes: 44_040_192, growthLabel: "최근 24시간" },
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
      storage: storage("24h"),
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
      { id: "health", description: "애플리케이션 전체 상태", path: `${actuatorBaseUrl}/health`, exposed: true },
      { id: "health/infrastructure", description: "DB, Redis, 커스텀 상태 그룹", path: `${actuatorBaseUrl}/health/infrastructure`, exposed: true },
      { id: "info", description: "런타임 및 모니터링 메타데이터", path: `${actuatorBaseUrl}/info`, exposed: true },
      { id: "metrics", description: "Micrometer 지표 카탈로그", path: `${actuatorBaseUrl}/metrics`, exposed: true },
      { id: "prometheus", description: "Prometheus 수집 엔드포인트", path: `${actuatorBaseUrl}/prometheus`, exposed: true },
      { id: "course-monitoring", description: "커스텀 모니터링 엔드포인트", path: `${actuatorBaseUrl}/course-monitoring`, exposed: true },
      { id: "spring-boot-admin", description: "실시간 Spring Boot Admin UI", path: adminBaseUrl, exposed: true },
    ],
    monitoringLinks: monitoringLinks(),
    infrastructure: infrastructure(),
    server: server(),
    storage: storage("24h"),
    timeWindows: timeWindows(),
  };
}
