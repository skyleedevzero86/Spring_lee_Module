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

function compact(value: number) {
  return new Intl.NumberFormat("ko-KR", {
    notation: "compact",
    maximumFractionDigits: value < 10 ? 1 : 0,
  }).format(value);
}

function statusClass(value: string) {
  return value.toLowerCase().replace(/[^a-z0-9]+/g, "_");
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
  if (value == null) return "N/A";
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
    return value === 1 ? "UP" : "DOWN";
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
  chips: string[];
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
          <span className="toolbar-label">Source</span>
          <span className={`toolbar-chip is-live ${statusClass(source)}`}>{source}</span>
        </div>

        <div className="toolbar-group">
          <span className="toolbar-label">Updated</span>
          <span className="toolbar-chip">{formatDate(updatedAt)}</span>
        </div>

        <div className="toolbar-group">
          <span className="toolbar-label">Window</span>
          {chips.map((chip) => (
            <span key={chip} className="toolbar-chip">
              {chip}
            </span>
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
          <p className="panel-kicker">Metric</p>
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
          <span className={`status-chip ${statusClass(link.kind)}`}>{link.kind}</span>
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
      <div className="server-row"><span>Host</span><strong>{server.hostName}</strong></div>
      <div className="server-row"><span>OS</span><strong>{server.operatingSystem}</strong></div>
      <div className="server-row"><span>Family</span><strong>{server.operatingSystemFamily}</strong></div>
      <div className="server-row"><span>Arch</span><strong>{server.architecture}</strong></div>
      <div className="server-row"><span>CPU</span><strong>{server.availableProcessors} cores</strong></div>
      <div className="server-row"><span>Java</span><strong>{server.javaRuntime}</strong></div>
      <div className="server-row"><span>Memory</span><strong>{formatBytes(server.usedMemoryBytes)} / {formatBytes(server.totalMemoryBytes)}</strong></div>
      <div className="server-row"><span>Disk</span><strong>{formatBytes(server.usedDiskBytes)} / {formatBytes(server.totalDiskBytes)}</strong></div>
      <div className="server-row"><span>Memory %</span><strong>{server.memoryUsagePercent.toFixed(1)}%</strong></div>
      <div className="server-row"><span>Disk %</span><strong>{server.diskUsagePercent.toFixed(1)}%</strong></div>
      <div className="server-row"><span>Disk Path</span><strong>{server.diskPath}</strong></div>
      <div className="server-row"><span>Captured</span><strong>{formatTime(server.capturedAt)}</strong></div>
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
            <span className={`status-chip ${statusClass(item.status)}`}>{item.status}</span>
          </div>
          <div className="storage-metrics">
            <div>
              <span>Used</span>
              <strong>{formatBytes(item.usedBytes)}</strong>
            </div>
            <div>
              <span>24h Delta</span>
              <strong>{`${item.dailyGrowthBytes >= 0 ? "+" : "-"}${formatBytes(Math.abs(item.dailyGrowthBytes))}`}</strong>
            </div>
          </div>
          <div className="storage-metrics">
            <div>
              <span>Limit</span>
              <strong>{formatBytes(item.limitBytes)}</strong>
            </div>
            <div>
              <span>Usage</span>
              <strong>{item.usagePercent == null ? "N/A" : `${item.usagePercent.toFixed(1)}%`}</strong>
            </div>
          </div>
          <p className="storage-detail">{item.detail}</p>
          <small>Version {item.version} · Updated {formatTime(item.capturedAt)}</small>
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
            <span className={`status-chip ${statusClass(item.status)}`}>{item.status}</span>
          </div>
          <div className="infra-stats">
            <div>
              <span>Availability</span>
              <strong>{item.availability === 1 ? "100%" : "0%"}</strong>
            </div>
            <div>
              <span>Latency</span>
              <strong>{item.latencyMs.toFixed(0)} ms</strong>
            </div>
          </div>
          <p>{item.detail}</p>
          <small>Updated {formatTime(item.lastCheckedAt)}</small>
        </article>
      ))}
    </div>
  );
}

export function TimeWindowCards({ windows }: { windows: TimeWindow[] }) {
  return (
    <div className="time-window-grid">
      {windows.map((window) => (
        <article key={window.label} className="time-window-card">
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
          <span>{metric.statistic}</span>
          <strong>{formatMetricValue(metric.value, metric.unit)}</strong>
        </div>
      ))}
    </div>
  );
}

export function TagCards({ tags }: { tags: TagSummary[] }) {
  return (
    <div className="tag-grid">
      {tags.map((tag) => (
        <div key={tag.key} className="tag-card">
          <strong>{tag.key}</strong>
          <div className="tag-list">
            {tag.values.map((value) => (
              <span key={value} className="tag-pill">
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
            <strong>{item.name}</strong>
            <p>{item.detail}</p>
          </div>
          <span className={`status-chip ${statusClass(item.status)}`}>{item.status}</span>
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
