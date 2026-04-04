import type { DistributionItem, TrendPoint } from "@/lib/types";

interface BarItem {
  label: string;
  value: number;
  accent: string;
  subtitle?: string;
  suffix?: string;
}

function compact(value: number) {
  return new Intl.NumberFormat("ko-KR", {
    notation: "compact",
    maximumFractionDigits: value < 10 ? 1 : 0,
  }).format(value);
}

function average(points: TrendPoint[]) {
  if (points.length === 0) return 0;
  return points.reduce((sum, point) => sum + point.value, 0) / points.length;
}

function formatLegendValue(value: number, unit: string) {
  if (unit === "%") return `${value.toFixed(1)}%`;
  if (unit === "ms") return `${value.toFixed(0)} ms`;
  if (unit === "req/min") return `${compact(value)} req/min`;
  return unit ? `${compact(value)} ${unit}` : compact(value);
}

export function LineChart({
  points,
  accent,
  unit,
}: {
  points: TrendPoint[];
  accent: string;
  unit: string;
}) {
  if (points.length === 0) {
    return <div className="empty-state">No time-series data is available.</div>;
  }

  const width = 720;
  const height = 240;
  const paddingX = 22;
  const paddingY = 18;
  const values = points.map((point) => point.value);
  const max = Math.max(...values, 1);
  const min = Math.min(...values, 0);
  const range = Math.max(max - min, 1);
  const latest = points.at(-1)?.value ?? 0;
  const avg = average(points);

  const coordinates = points.map((point, index) => {
    const x =
      points.length === 1
        ? width / 2
        : paddingX + (index / (points.length - 1)) * (width - paddingX * 2);
    const y = height - paddingY - ((point.value - min) / range) * (height - paddingY * 2);
    return { x, y, label: point.label };
  });

  const gradientId = `fill-${accent.replace("#", "")}`;
  const polyline = coordinates.map((point) => `${point.x},${point.y}`).join(" ");
  const fillPath = `${paddingX},${height - paddingY} ${polyline} ${width - paddingX},${height - paddingY}`;

  return (
    <div className="chart-block">
      <div className="chart-legend">
        <span><i style={{ background: accent }} />Latest {formatLegendValue(latest, unit)}</span>
        <span><i style={{ background: "#6dd6ff" }} />Average {formatLegendValue(avg, unit)}</span>
        <span><i style={{ background: "#5a6376" }} />Peak {formatLegendValue(max, unit)}</span>
      </div>

      <svg viewBox={`0 0 ${width} ${height}`} className="chart-canvas" role="img" aria-label={`${unit} trend`}>
        <defs>
          <linearGradient id={gradientId} x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stopColor={accent} stopOpacity="0.24" />
            <stop offset="100%" stopColor={accent} stopOpacity="0.01" />
          </linearGradient>
        </defs>

        {[0, 1, 2, 3, 4].map((index) => {
          const y = paddingY + (index / 4) * (height - paddingY * 2);
          return (
            <line
              key={y}
              x1={paddingX}
              y1={y}
              x2={width - paddingX}
              y2={y}
              className="chart-grid-line"
            />
          );
        })}

        <line x1={paddingX} y1={height - paddingY} x2={width - paddingX} y2={height - paddingY} className="chart-axis" />
        <polygon points={fillPath} fill={`url(#${gradientId})`} />
        <polyline points={polyline} fill="none" stroke={accent} strokeWidth="2.4" strokeLinejoin="round" strokeLinecap="round" />
        {coordinates.map((coordinate) => (
          <circle key={`${coordinate.label}-${coordinate.x}`} cx={coordinate.x} cy={coordinate.y} r="2.8" fill={accent} />
        ))}
      </svg>

      <div className="chart-footer">
        <div className="chart-labels">
          {points.map((point) => (
            <span key={point.label}>{point.label}</span>
          ))}
        </div>
        <div className="chart-stats">
          <span>min {compact(min)}</span>
          <span>max {compact(max)}</span>
        </div>
      </div>
    </div>
  );
}

export function BarList({ items }: { items: BarItem[] }) {
  if (items.length === 0) {
    return <div className="empty-state">No items are available.</div>;
  }

  const max = Math.max(...items.map((item) => item.value), 1);

  return (
    <div className="bar-list">
      {items.map((item) => (
        <div key={item.label} className="bar-item">
          <div className="bar-item-head">
            <div>
              <strong>{item.label}</strong>
              {item.subtitle ? <p>{item.subtitle}</p> : null}
            </div>
            <span>
              {compact(item.value)}
              {item.suffix ?? ""}
            </span>
          </div>
          <div className="bar-track">
            <div
              className="bar-fill"
              style={{
                width: `${(item.value / max) * 100}%`,
                background: item.accent,
              }}
            />
          </div>
        </div>
      ))}
    </div>
  );
}

export function DonutBreakdown({ items }: { items: DistributionItem[] }) {
  if (items.length === 0) {
    return <div className="empty-state">No breakdown data is available.</div>;
  }

  const total = items.reduce((sum, item) => sum + item.value, 0);
  let cursor = 0;
  const segments = items.map((item) => {
    const start = cursor;
    const end = cursor + (item.value / total) * 100;
    cursor = end;
    return `${item.accent} ${start}% ${end}%`;
  });

  return (
    <div className="donut-layout">
      <div className="donut-ring" style={{ background: `conic-gradient(${segments.join(", ")})` }}>
        <div className="donut-hole">
          <span>Status</span>
          <strong>{total}</strong>
        </div>
      </div>

      <div className="donut-legend">
        {items.map((item) => (
          <div key={item.label} className="legend-item">
            <span className="legend-dot" style={{ background: item.accent }} />
            <div>
              <strong>{item.label}</strong>
              <p>{compact(item.value)}</p>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
