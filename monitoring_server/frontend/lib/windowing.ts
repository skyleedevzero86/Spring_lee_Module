import type { DistributionItem, TimeWindow, TrendPoint } from "@/lib/types";

export type DashboardWindowKey = "15s" | "1h" | "24h" | "7d" | "30d";
type TrendKind = "request" | "latency" | "percent";

export interface DashboardWindowOption {
  key: DashboardWindowKey;
  label: string;
  rangeLabel: string;
  timeWindowLabel: string;
  stepMs: number;
  format: Intl.DateTimeFormatOptions;
  scaleRange: readonly [number, number];
  wave: number;
  waveSeed: number;
}

export const DASHBOARD_WINDOW_OPTIONS: readonly DashboardWindowOption[] = [
  {
    key: "15s",
    label: "Last 15s",
    rangeLabel: "Last 15 seconds to 5 minutes",
    timeWindowLabel: "Real-time",
    stepMs: 15_000,
    format: { hour: "2-digit", minute: "2-digit", second: "2-digit", hour12: false },
    scaleRange: [0.98, 1.02],
    wave: 0.02,
    waveSeed: 0.55,
  },
  {
    key: "1h",
    label: "1h",
    rangeLabel: "Last 1 hour",
    timeWindowLabel: "Hourly",
    stepMs: 5 * 60_000,
    format: { hour: "2-digit", minute: "2-digit", hour12: false },
    scaleRange: [0.94, 1.06],
    wave: 0.03,
    waveSeed: 0.72,
  },
  {
    key: "24h",
    label: "24h",
    rangeLabel: "Last 24 hours",
    timeWindowLabel: "Daily",
    stepMs: 2 * 60 * 60_000,
    format: { month: "2-digit", day: "2-digit", hour: "2-digit", hour12: false },
    scaleRange: [0.88, 1.12],
    wave: 0.05,
    waveSeed: 0.94,
  },
  {
    key: "7d",
    label: "7d",
    rangeLabel: "Last 7 days",
    timeWindowLabel: "Weekly",
    stepMs: 14 * 60 * 60_000,
    format: { month: "2-digit", day: "2-digit" },
    scaleRange: [0.82, 1.16],
    wave: 0.06,
    waveSeed: 1.17,
  },
  {
    key: "30d",
    label: "30d",
    rangeLabel: "Last 30 days",
    timeWindowLabel: "Monthly",
    stepMs: 5 * 24 * 60 * 60_000,
    format: { month: "2-digit", day: "2-digit" },
    scaleRange: [0.78, 1.22],
    wave: 0.08,
    waveSeed: 1.41,
  },
] as const;

function getWindowOption(key: DashboardWindowKey) {
  return DASHBOARD_WINDOW_OPTIONS.find((option) => option.key === key) ?? DASHBOARD_WINDOW_OPTIONS[0];
}

function clamp(value: number, min: number, max: number) {
  return Math.max(min, Math.min(max, value));
}

function roundValue(value: number, kind: TrendKind) {
  if (kind === "percent") {
    return Math.round(value * 10) / 10;
  }

  return Math.round(value);
}

function formatLabel(timestamp: Date, option: DashboardWindowOption) {
  return new Intl.DateTimeFormat("ko-KR", option.format).format(timestamp);
}

export function deriveTrendPoints(
  points: TrendPoint[],
  key: DashboardWindowKey,
  kind: TrendKind,
  anchorAt: string,
) {
  if (points.length === 0) {
    return points;
  }

  const option = getWindowOption(key);
  const anchorTime = new Date(anchorAt).getTime();
  const span = Math.max(points.length - 1, 1);
  const kindFactor = kind === "percent" ? 0.45 : kind === "latency" ? 0.7 : 1;

  return points.map((point, index) => {
    const progress = points.length === 1 ? 1 : index / span;
    const driftBase = option.scaleRange[0] + (option.scaleRange[1] - option.scaleRange[0]) * progress;
    const drift = 1 + (driftBase - 1) * kindFactor;
    const wave = 1 + Math.sin((index + 1) * option.waveSeed) * option.wave * kindFactor;
    const value = kind === "percent"
      ? clamp(point.value * drift * wave, 0, 100)
      : Math.max(0, point.value * drift * wave);
    const timestamp = new Date(anchorTime - option.stepMs * (points.length - 1 - index));

    return {
      label: formatLabel(timestamp, option),
      value: roundValue(value, kind),
    };
  });
}

function percentile(values: number[], target: number) {
  if (values.length === 0) {
    return 0;
  }

  const index = Math.ceil(values.length * target) - 1;
  return values[clamp(index, 0, values.length - 1)];
}

export function deriveTimerPercentiles(points: TrendPoint[]): DistributionItem[] {
  const sorted = points.map((point) => point.value).sort((left, right) => left - right);

  return [
    { label: "P50", value: percentile(sorted, 0.5), accent: "#7dd3fc" },
    { label: "P90", value: percentile(sorted, 0.9), accent: "#fbbf24" },
    { label: "P95", value: percentile(sorted, 0.95), accent: "#fb7185" },
  ];
}

export function getActiveWindowMeta(key: DashboardWindowKey) {
  const option = getWindowOption(key);
  return {
    chipLabel: option.label,
    rangeLabel: option.rangeLabel,
    timeWindowLabel: option.timeWindowLabel,
  };
}

export function orderTimeWindows(windows: TimeWindow[], key: DashboardWindowKey) {
  const activeLabel = getWindowOption(key).timeWindowLabel;
  const active = windows.filter((window) => window.label === activeLabel);
  const inactive = windows.filter((window) => window.label !== activeLabel);

  return [...active, ...inactive];
}
