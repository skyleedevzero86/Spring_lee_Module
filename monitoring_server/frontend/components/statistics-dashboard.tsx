"use client";

import { useState } from "react";
import { BarList, DonutBreakdown, LineChart } from "@/components/chart-primitives";
import {
  ConsolePanel,
  DashboardTopbar,
  InfrastructureCards,
  MetricRows,
  NotesList,
  ServerSnapshot,
  StatPanel,
  StorageCards,
  TagCards,
  TimeWindowCards,
} from "@/components/console-fragments";
import type { ConsoleStat } from "@/components/console-fragments";
import type { StatisticsResponse } from "@/lib/types";
import {
  DASHBOARD_WINDOW_OPTIONS,
  deriveTimerPercentiles,
  deriveTrendPoints,
  type DashboardWindowKey,
  getActiveWindowMeta,
  orderTimeWindows,
} from "@/lib/windowing";

function summaryStats(
  data: StatisticsResponse,
  activeWindowLabel: string,
  activeRangeLabel: string,
): ConsoleStat[] {
  const [postgresql, redis] = data.storage;

  return [
    {
      label: "Host Memory",
      value: `${data.server.memoryUsagePercent.toFixed(1)}%`,
      caption: `${(data.server.usedMemoryBytes / 1024 / 1024 / 1024).toFixed(1)} GB used`,
      tone: "info",
    },
    {
      label: "Host Disk",
      value: `${data.server.diskUsagePercent.toFixed(1)}%`,
      caption: `${(data.server.usedDiskBytes / 1024 / 1024 / 1024).toFixed(1)} GB used`,
      tone: "warning",
    },
    {
      label: "PostgreSQL",
      value: postgresql?.usedBytes == null ? "N/A" : `${(postgresql.usedBytes / 1024 / 1024 / 1024).toFixed(1)} GB`,
      caption: postgresql ? `24h +${(postgresql.dailyGrowthBytes / 1024 / 1024).toFixed(1)} MB` : "No data",
      tone: "success",
    },
    {
      label: "Redis",
      value: redis?.usedBytes == null ? "N/A" : `${(redis.usedBytes / 1024 / 1024).toFixed(1)} MB`,
      caption: redis ? `24h +${(redis.dailyGrowthBytes / 1024 / 1024).toFixed(1)} MB` : "No data",
      tone: "success",
    },
    { label: "Metric Set", value: String(data.highlightedMetrics.length), caption: "highlighted metrics", tone: "info" },
    { label: "Window", value: activeWindowLabel, caption: activeRangeLabel, tone: "warning" },
  ];
}

export function StatisticsDashboard({ data }: { data: StatisticsResponse }) {
  const [activeWindow, setActiveWindow] = useState<DashboardWindowKey>("15s");
  const activeMeta = getActiveWindowMeta(activeWindow);
  const requestTrend = deriveTrendPoints(data.requestTrend, activeWindow, "request", data.generatedAt);
  const latencyTrend = deriveTrendPoints(data.latencyTrend, activeWindow, "latency", data.generatedAt);
  const timerPercentiles = deriveTimerPercentiles(latencyTrend);
  const orderedWindows = orderTimeWindows(data.timeWindows, activeWindow);

  return (
    <div className="dashboard-page">
      <DashboardTopbar
        kicker="Metrics View"
        title="Metrics, Capacity, and Growth Dashboard"
        subtitle="Dense metric panels for host resources, PostgreSQL and Redis capacity, health distribution, and time window queries."
        source={data.dataSource ?? "sample"}
        updatedAt={data.generatedAt}
        chips={DASHBOARD_WINDOW_OPTIONS.map((window) => ({
          key: window.key,
          label: window.label,
          active: window.key === activeWindow,
          onClick: () => setActiveWindow(window.key),
        }))}
      />

      <section className="dashboard-grid">
        {summaryStats(data, activeMeta.chipLabel, activeMeta.rangeLabel).map((stat) => (
          <StatPanel
            key={stat.label}
            className="span-2"
            label={stat.label}
            value={stat.value}
            caption={stat.caption}
            tone={stat.tone}
          />
        ))}

        <ConsolePanel className="span-6" kicker="Traffic" title="Request Trend" note={`req/min / ${activeMeta.rangeLabel}`}>
          <LineChart points={requestTrend} accent="#57c7ff" unit="req/min" />
        </ConsolePanel>

        <ConsolePanel className="span-3" kicker="Percentile" title="Timer Percentiles" note={`P50 / P90 / P95 / ${activeMeta.chipLabel}`}>
          <BarList
            items={timerPercentiles.map((item) => ({
              label: item.label,
              value: item.value,
              accent: item.accent,
              subtitle: "synthetic latency",
              suffix: " ms",
            }))}
          />
        </ConsolePanel>

        <ConsolePanel className="span-3" kicker="Health" title="Health Breakdown" note="component distribution">
          <DonutBreakdown items={data.healthBreakdown} />
        </ConsolePanel>

        <ConsolePanel className="span-8" kicker="Metrics" title="Highlighted Metric Samples" note="Prometheus-oriented values">
          <MetricRows metrics={data.highlightedMetrics} />
        </ConsolePanel>

        <ConsolePanel className="span-4" kicker="History" title="Suggested Time Windows" note="ready to paste into Grafana">
          <TimeWindowCards windows={orderedWindows} activeLabel={activeMeta.timeWindowLabel} />
        </ConsolePanel>

        <ConsolePanel className="span-4" kicker="Host" title="Server Snapshot" note={data.server.operatingSystemFamily}>
          <ServerSnapshot server={data.server} />
        </ConsolePanel>

        <ConsolePanel className="span-4" kicker="Stores" title="Store Usage" note="current size and net growth">
          <StorageCards storage={data.storage} />
        </ConsolePanel>

        <ConsolePanel className="span-4" kicker="Infrastructure" title="DB and Redis Status" note="probe latency">
          <InfrastructureCards items={data.infrastructure} />
        </ConsolePanel>

        <ConsolePanel className="span-4" kicker="Tags" title="Metric Tags" note="page / section / component">
          <TagCards tags={data.tags} />
        </ConsolePanel>

        <ConsolePanel className="span-4" kicker="Roadmap" title="Section Mapping" note="course progress gauges">
          <BarList
            items={data.sections.map((section) => ({
              label: section.title,
              value: section.completionRate,
              accent: section.accent,
              subtitle: section.focus,
              suffix: "%",
            }))}
          />
        </ConsolePanel>

        <ConsolePanel className="span-4" kicker="Notes" title="Operating Notes" note="history interpretation">
          <NotesList notes={data.notes} />
        </ConsolePanel>
      </section>
    </div>
  );
}
