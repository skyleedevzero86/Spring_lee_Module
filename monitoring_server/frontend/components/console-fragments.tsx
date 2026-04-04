"use client";

import type { ReactNode } from "react";
import type {
  ComponentStatus,
  EndpointSummary,
  InfrastructureStatus,
  MetricStat,
  MonitoringLink,
  ServerStatus,
  StoreUsage,
  TagSummary,
  TimeWindow,
} from "@/lib/types";

export interface ConsoleStat {
  label: string;
  value: string;
  caption: string;
  tone: string;
}

export interface DashboardChip {
  key: string;
  label: string;
  active?: boolean;
  onClick?: () => void;
}

const STATUS_LABELS: Record<string, string> = {
  UP: "정상",
  DOWN: "장애",
  OUT_OF_SERVICE: "서비스 중지",
  UNKNOWN: "알 수 없음",
};

const SOURCE_LABELS: Record<string, string> = {
  live: "실시간",
  sample: "샘플",
};

const OS_FAMILY_LABELS: Record<string, string> = {
  WINDOWS: "윈도우",
  LINUX: "리눅스",
  MAC: "맥",
  OTHER: "기타",
};

const LINK_KIND_LABELS: Record<string, string> = {
  admin: "관리자",
  actuator: "액추에이터",
  prometheus: "프로메테우스",
  grafana: "그라파나",
};

const METRIC_STATISTIC_LABELS: Record<string, string> = {
  VALUE: "값",
  COUNT: "개수",
  TOTAL: "총합",
  MAX: "최대",
  SUM: "합계",
};

function compact(value: number) {
  return new Intl.NumberFormat("ko-KR", {
    notation: "compact",
    maximumFractionDigits: value < 10 ? 1 : 0,
  }).format(value);
}

function normalizeKey(value: string) {
  return value.toLowerCase().replace(/[^a-z0-9]+/g, "");
}

function statusClass(value: string) {
  return value.toLowerCase().replace(/[^a-z0-9]+/g, "_");
}

export function localizeStatus(value: string) {
  return STATUS_LABELS[value.toUpperCase()] ?? value;
}

export function localizeSource(value: string) {
  return SOURCE_LABELS[value.toLowerCase()] ?? value;
}

export function localizeOsFamily(value: string) {
  return OS_FAMILY_LABELS[value.toUpperCase()] ?? value;
}

export function localizeLinkKind(value: string) {
  return LINK_KIND_LABELS[value.toLowerCase()] ?? value;
}

export function localizeMetricStatistic(value: string) {
  return METRIC_STATISTIC_LABELS[value.toUpperCase()] ?? value;
}

export function localizeComponentName(value: string) {
  switch (normalizeKey(value)) {
    case "db":
    case "database":
      return "데이터베이스";
    case "redis":
      return "Redis";
    case "postgresql":
      return "PostgreSQL";
    case "diskspace":
      return "디스크 공간";
    case "ping":
      return "핑";
    case "coursecoverage":
      return "학습 진행률";
    case "livenessstate":
      return "라이브니스 상태";
    case "readinessstate":
      return "레디니스 상태";
    default:
      return value;
  }
}

export function formatDate(value: string) {
  return new Intl.DateTimeFormat("ko-KR", {
    dateStyle: "short",
    timeStyle: "short",
    hour12: false,
  }).format(new Date(value));
}

export function formatTime(value: string) {
  return new Intl.DateTimeFormat("ko-KR", {
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
    hour12: false,
  }).format(new Date(value));
}

export function formatBytes(value: number | null) {
  if (value == null) return "없음";
  if (value === 0) return "0 B";

  const units = ["B", "KB", "MB", "GB", "TB"];
  let current = value;
  let index = 0;

  while (current >= 1024 && index < units.length - 1) {
    current /= 1024;
    index += 1;
  }

  return `${current.toFixed(1)} ${units[index]}`;
}

export function formatMetricValue(value: number, unit: string) {
  if (unit === "bytes") {
    return formatBytes(value);
  }

  if (unit === "milliseconds") {
    return `${value.toFixed(0)} ms`;
  }

  if (value === 1 || value === 0) {
    return value === 1 ? "정상" : "장애";
  }

  if (value >= 1000) {
    return compact(value);
  }

  return value < 10 ? value.toFixed(2) : value.toFixed(0);
}

export function DashboardTopbar({
  kicker,
  title,
  subtitle,
  source,
  updatedAt,
  chips,
}: {
  kicker: string;
  title: string;
  subtitle: string;
  source: string;
  updatedAt: string;
  chips: DashboardChip[];
}) {
  return (
    <section className="board-topbar">
      <div className="board-title">
        <p className="board-kicker">{kicker}</p>
        <h1>{title}</h1>
        <p>{subtitle}</p>
      </div>

      <div className="board-toolbar">
        <div className="toolbar-group">
          <span className="toolbar-label">데이터</span>
          <span className={`toolbar-chip is-live ${statusClass(source)}`}>{localizeSource(source)}</span>
        </div>

        <div className="toolbar-group">
          <span className="toolbar-label">업데이트</span>
          <span className="toolbar-chip">{formatDate(updatedAt)}</span>
        </div>

        <div className="toolbar-group">
          <span className="toolbar-label">기간</span>
          {chips.map((chip) => (
            chip.onClick ? (
              <button
                key={chip.key}
                type="button"
                className={`toolbar-chip toolbar-chip-button ${chip.active ? "is-active" : ""}`.trim()}
                onClick={chip.onClick}
                aria-pressed={chip.active}
              >
                {chip.label}
              </button>
            ) : (
              <span key={chip.key} className={`toolbar-chip ${chip.active ? "is-active" : ""}`.trim()}>
                {chip.label}
              </span>
            )
          ))}
        </div>
      </div>
    </section>
  );
}

export function ConsolePanel({
  className,
  kicker,
  title,
  note,
  children,
}: {
  className?: string;
  kicker: string;
  title: string;
  note?: string;
  children: ReactNode;
}) {
  return (
    <article className={`console-panel ${className ?? ""}`.trim()}>
      <div className="console-panel-head">
        <div>
          <p className="panel-kicker">{kicker}</p>
          <h2>{title}</h2>
        </div>
        {note ? <span className="panel-note">{note}</span> : null}
      </div>
      {children}
    </article>
  );
}

export function StatPanel({
  className,
  label,
  value,
  caption,
  tone,
}: ConsoleStat & { className?: string }) {
  return (
    <article className={`console-panel stat-panel ${className ?? ""} tone-${tone}`.trim()}>
      <div className="console-panel-head">
        <div>
          <p className="panel-kicker">지표</p>
          <h2>{label}</h2>
        </div>
      </div>
      <div className="stat-body">
        <strong className="stat-value">{value}</strong>
        <p className="stat-caption">{caption}</p>
      </div>
    </article>
  );
}

export function MonitoringLinksGrid({ links }: { links: MonitoringLink[] }) {
  return (
    <div className="dense-card-grid">
      {links.map((link) => (
        <a key={link.title} className="dense-link-card" href={link.url} target="_blank" rel="noreferrer">
          <span className={`status-chip ${statusClass(link.kind)}`}>{localizeLinkKind(link.kind)}</span>
          <strong>{link.title}</strong>
          <p>{link.description}</p>
          <code>{link.url}</code>
        </a>
      ))}
    </div>
  );
}

export function EndpointRows({ endpoints }: { endpoints: EndpointSummary[] }) {
  return (
    <div className="endpoint-table">
      {endpoints.map((endpoint) => (
        <a key={endpoint.id} className="endpoint-row" href={endpoint.path} target="_blank" rel="noreferrer">
          <div>
            <strong>{endpoint.id}</strong>
            <p>{endpoint.description}</p>
          </div>
          <span>{endpoint.path}</span>
        </a>
      ))}
    </div>
  );
}

export function ServerSnapshot({ server }: { server: ServerStatus }) {
  return (
    <div className="server-grid">
      <div className="server-row"><span>호스트</span><strong>{server.hostName}</strong></div>
      <div className="server-row"><span>운영체제</span><strong>{server.operatingSystem}</strong></div>
      <div className="server-row"><span>계열</span><strong>{localizeOsFamily(server.operatingSystemFamily)}</strong></div>
      <div className="server-row"><span>아키텍처</span><strong>{server.architecture}</strong></div>
      <div className="server-row"><span>CPU</span><strong>{server.availableProcessors}코어</strong></div>
      <div className="server-row"><span>Java</span><strong>{server.javaRuntime}</strong></div>
      <div className="server-row"><span>메모리</span><strong>{formatBytes(server.usedMemoryBytes)} / {formatBytes(server.totalMemoryBytes)}</strong></div>
      <div className="server-row"><span>디스크</span><strong>{formatBytes(server.usedDiskBytes)} / {formatBytes(server.totalDiskBytes)}</strong></div>
      <div className="server-row"><span>메모리 사용률</span><strong>{server.memoryUsagePercent.toFixed(1)}%</strong></div>
      <div className="server-row"><span>디스크 사용률</span><strong>{server.diskUsagePercent.toFixed(1)}%</strong></div>
      <div className="server-row"><span>디스크 경로</span><strong>{server.diskPath}</strong></div>
      <div className="server-row"><span>수집 시각</span><strong>{formatTime(server.capturedAt)}</strong></div>
    </div>
  );
}

export function StorageCards({ storage }: { storage: StoreUsage[] }) {
  return (
    <div className="storage-grid">
      {storage.map((item) => (
        <article key={item.component} className="storage-card">
          <div className="storage-card-head">
            <strong>{item.component}</strong>
            <span className={`status-chip ${statusClass(item.status)}`}>{localizeStatus(item.status)}</span>
          </div>
          <div className="storage-metrics">
            <div>
              <span>사용량</span>
              <strong>{formatBytes(item.usedBytes)}</strong>
            </div>
            <div>
              <span>{item.growthLabel} 증감</span>
              <strong>{`${item.dailyGrowthBytes >= 0 ? "+" : "-"}${formatBytes(Math.abs(item.dailyGrowthBytes))}`}</strong>
            </div>
          </div>
          <div className="storage-metrics">
            <div>
              <span>한도</span>
              <strong>{formatBytes(item.limitBytes)}</strong>
            </div>
            <div>
              <span>사용률</span>
              <strong>{item.usagePercent == null ? "없음" : `${item.usagePercent.toFixed(1)}%`}</strong>
            </div>
          </div>
          <p className="storage-detail">{item.detail}</p>
          <small>버전 {item.version} · 갱신 {formatTime(item.capturedAt)}</small>
        </article>
      ))}
    </div>
  );
}

export function InfrastructureCards({ items }: { items: InfrastructureStatus[] }) {
  return (
    <div className="infra-grid">
      {items.map((item) => (
        <article key={item.component} className="infra-card">
          <div className="infra-card-head">
            <strong>{item.component}</strong>
            <span className={`status-chip ${statusClass(item.status)}`}>{localizeStatus(item.status)}</span>
          </div>
          <div className="infra-stats">
            <div>
              <span>가용성</span>
              <strong>{`${(item.availability * 100).toFixed(0)}%`}</strong>
            </div>
            <div>
              <span>지연 시간</span>
              <strong>{item.latencyMs.toFixed(0)} ms</strong>
            </div>
          </div>
          <p>{item.detail}</p>
          <small>갱신 {formatTime(item.lastCheckedAt)}</small>
        </article>
      ))}
    </div>
  );
}

export function TimeWindowCards({ windows, activeLabel }: { windows: TimeWindow[]; activeLabel?: string }) {
  return (
    <div className="time-window-grid">
      {windows.map((window) => (
        <article key={window.label} className={`time-window-card ${window.label === activeLabel ? "is-active" : ""}`.trim()}>
          <div className="time-window-head">
            <strong>{window.label}</strong>
            <span>{window.range}</span>
          </div>
          <p>{window.purpose}</p>
          <code>{window.query}</code>
        </article>
      ))}
    </div>
  );
}

export function NotesList({ notes }: { notes: string[] }) {
  return (
    <div className="bullet-list">
      {notes.map((note) => (
        <div key={note} className="bullet-item">
          <span className="bullet-dot" />
          <p>{note}</p>
        </div>
      ))}
    </div>
  );
}

export function MetricRows({ metrics }: { metrics: MetricStat[] }) {
  return (
    <div className="metric-table">
      {metrics.map((metric) => (
        <div key={`${metric.name}-${metric.statistic}`} className="metric-row">
          <div>
            <strong>{metric.name}</strong>
            <p>{metric.description}</p>
          </div>
          <span>{localizeMetricStatistic(metric.statistic)}</span>
          <strong>{formatMetricValue(metric.value, metric.unit)}</strong>
        </div>
      ))}
    </div>
  );
}

export function TagCards({ tags }: { tags: TagSummary[] }) {
  return (
    <div className="tag-grid">
      {tags.map((tag, tagIndex) => (
        <div key={`${tag.key}-${tagIndex}`} className="tag-card">
          <strong>{tag.key}</strong>
          <div className="tag-list">
            {tag.values.map((value: string, valueIndex: number) => (
              <span key={`${value}-${valueIndex}`} className="tag-pill">
                {value}
              </span>
            ))}
          </div>
        </div>
      ))}
    </div>
  );
}

export function StatusRows({ items }: { items: ComponentStatus[] }) {
  return (
    <div className="status-list">
      {items.map((item) => (
        <div key={item.name} className="status-row">
          <div>
            <strong>{localizeComponentName(item.name)}</strong>
            <p>{item.detail}</p>
          </div>
          <span className={`status-chip ${statusClass(item.status)}`}>{localizeStatus(item.status)}</span>
        </div>
      ))}
    </div>
  );
}

export function PillList({ items }: { items: string[] }) {
  return (
    <div className="tag-list">
      {items.map((item) => (
        <span key={item} className="tag-pill">
          {item}
        </span>
      ))}
    </div>
  );
}
