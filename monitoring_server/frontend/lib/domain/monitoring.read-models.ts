export type DataSource = "live" | "sample";

export interface ApplicationSummary {
  name: string;
  version: string;
  springBootVersion: string;
  javaVersion: string;
  environment: string;
  backendUrl: string;
  actuatorBaseUrl: string;
  adminUrl: string;
  prometheusUrl: string;
  grafanaUrl: string;
}

export interface KpiCard {
  label: string;
  value: string;
  caption: string;
  tone: string;
}

export interface TrendPoint {
  label: string;
  value: number;
}

export interface SectionProgress {
  title: string;
  chapters: number;
  durationMinutes: number;
  completionRate: number;
  focus: string;
  accent: string;
}

export interface ComponentStatus {
  name: string;
  status: string;
  detail: string;
}

export interface HealthSnapshot {
  status: string;
  uptimeMinutes: number;
  components: ComponentStatus[];
}

export interface EndpointSummary {
  id: string;
  description: string;
  path: string;
  exposed: boolean;
}

export interface MetricStat {
  name: string;
  description: string;
  statistic: string;
  value: number;
  unit: string;
}

export interface TagSummary {
  key: string;
  values: string[];
}

export interface DistributionItem {
  label: string;
  value: number;
  accent: string;
}

export interface MonitoringLink {
  title: string;
  description: string;
  url: string;
  kind: string;
}

export interface InfrastructureStatus {
  component: string;
  status: string;
  availability: number;
  latencyMs: number;
  lastCheckedAt: string;
  detail: string;
}

export interface ServerStatus {
  hostName: string;
  operatingSystemFamily: string;
  operatingSystem: string;
  architecture: string;
  javaRuntime: string;
  availableProcessors: number;
  totalMemoryBytes: number;
  usedMemoryBytes: number;
  freeMemoryBytes: number;
  memoryUsagePercent: number;
  totalDiskBytes: number;
  usedDiskBytes: number;
  freeDiskBytes: number;
  diskUsagePercent: number;
  diskPath: string;
  capturedAt: string;
}

export interface StoreUsage {
  component: string;
  status: string;
  version: string;
  usedBytes: number | null;
  limitBytes: number | null;
  freeBytes: number | null;
  usagePercent: number | null;
  dailyGrowthBytes: number;
  growthLabel: string;
  capturedAt: string;
  detail: string;
}

export interface TimeWindow {
  label: string;
  range: string;
  purpose: string;
  query: string;
}

export interface OverviewPayload {
  generatedAt: string;
  application: ApplicationSummary;
  health: HealthSnapshot;
  kpis: KpiCard[];
  requestTrend: TrendPoint[];
  latencyTrend: TrendPoint[];
  cacheTrend: TrendPoint[];
  sections: SectionProgress[];
  actuatorEndpoints: EndpointSummary[];
  monitoringLinks: MonitoringLink[];
  infrastructure: InfrastructureStatus[];
  server: ServerStatus;
  storage: StoreUsage[];
  highlights: string[];
}

export interface StatisticsPayload {
  generatedAt: string;
  application: ApplicationSummary;
  highlightedMetrics: MetricStat[];
  healthBreakdown: DistributionItem[];
  timerPercentiles: DistributionItem[];
  tags: TagSummary[];
  requestTrend: TrendPoint[];
  latencyTrend: TrendPoint[];
  sections: SectionProgress[];
  infrastructure: InfrastructureStatus[];
  server: ServerStatus;
  storage: StoreUsage[];
  timeWindows: TimeWindow[];
  notes: string[];
}

export interface ActuatorSummaryPayload {
  generatedAt: string;
  application: ApplicationSummary;
  health: HealthSnapshot;
  info: Record<string, unknown>;
  customEndpoint: Record<string, unknown>;
  metricNames: string[];
  links: EndpointSummary[];
  monitoringLinks: MonitoringLink[];
  infrastructure: InfrastructureStatus[];
  server: ServerStatus;
  storage: StoreUsage[];
  timeWindows: TimeWindow[];
}

export type OverviewResponse = OverviewPayload & { dataSource?: DataSource };
export type StatisticsResponse = StatisticsPayload & { dataSource?: DataSource };
export type ActuatorSummaryResponse = ActuatorSummaryPayload & { dataSource?: DataSource };
