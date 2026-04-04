"use client";

import { useState } from "react";
import { BarList, DonutBreakdown, LineChart } from "@/components/chart-primitives";
import {
  ConsolePanel,
  DashboardTopbar,
  EndpointRows,
  InfrastructureCards,
  MonitoringLinksGrid,
  NotesList,
  ServerSnapshot,
  StatPanel,
  StatusRows,
  StorageCards,
} from "@/components/console-fragments";
import type { DistributionItem, OverviewResponse } from "@/lib/types";
import {
  DASHBOARD_WINDOW_OPTIONS,
  deriveTrendPoints,
  type DashboardWindowKey,
  getActiveWindowMeta,
} from "@/lib/windowing";

function healthDistribution(statuses: OverviewResponse["health"]["components"]): DistributionItem[] {
  const counts = new Map<string, number>();
  for (const status of statuses) {
    counts.set(status.status, (counts.get(status.status) ?? 0) + 1);
  }

  return Array.from(counts.entries()).map(([label, value]) => ({
    label,
    value,
    accent:
      label === "UP"
        ? "#88ff2a"
        : label === "DOWN"
          ? "#ff5d73"
          : label === "OUT_OF_SERVICE"
            ? "#ff9b4d"
            : "#57c7ff",
  }));
}

export function OverviewDashboard({ data }: { data: OverviewResponse }) {
  const [activeWindow, setActiveWindow] = useState<DashboardWindowKey>("15s");
  const activeMeta = getActiveWindowMeta(activeWindow);
  const requestTrend = deriveTrendPoints(data.requestTrend, activeWindow, "request", data.generatedAt);
  const latencyTrend = deriveTrendPoints(data.latencyTrend, activeWindow, "latency", data.generatedAt);
  const cacheTrend = deriveTrendPoints(data.cacheTrend, activeWindow, "percent", data.generatedAt);

  return (
    <div className="dashboard-page">
      <DashboardTopbar
        kicker="Operations View"
        title="Infrastructure Monitoring Console"
        subtitle="Current health, live capacity, connected store usage, and monitoring links in one dense dashboard."
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
        {data.kpis.map((kpi) => (
          <StatPanel
            key={kpi.label}
            className="span-2"
            label={kpi.label}
            value={kpi.value}
            caption={kpi.caption}
            tone={kpi.tone}
          />
        ))}

        <ConsolePanel className="span-6" kicker="Traffic" title="Request Trend" note={`req/min / ${activeMeta.rangeLabel}`}>
          <LineChart points={requestTrend} accent="#57c7ff" unit="req/min" />
        </ConsolePanel>

        <ConsolePanel className="span-3" kicker="Latency" title="Response Latency" note={`@Timed / ${activeMeta.chipLabel}`}>
          <LineChart points={latencyTrend} accent="#ffb020" unit="ms" />
        </ConsolePanel>

        <ConsolePanel className="span-3" kicker="Cache" title="Cache Hit Rate" note={`gauge % / ${activeMeta.chipLabel}`}>
          <LineChart points={cacheTrend} accent="#88ff2a" unit="%" />
        </ConsolePanel>

        <ConsolePanel className="span-5" kicker="Host" title="Application Host Snapshot" note={data.server.operatingSystemFamily}>
          <ServerSnapshot server={data.server} />
        </ConsolePanel>

        <ConsolePanel className="span-7" kicker="Stores" title="Connected Stores" note="size and recent delta">
          <StorageCards storage={data.storage} />
        </ConsolePanel>

        <ConsolePanel className="span-4" kicker="Health" title="Component Health" note="Actuator contributors">
          <DonutBreakdown items={healthDistribution(data.health.components)} />
          <StatusRows items={data.health.components} />
        </ConsolePanel>

        <ConsolePanel className="span-4" kicker="Sections" title="Section Progress" note="course gauge mapping">
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

        <ConsolePanel className="span-4" kicker="Infrastructure" title="Probe Status" note="availability and latency">
          <InfrastructureCards items={data.infrastructure} />
        </ConsolePanel>

        <ConsolePanel className="span-6" kicker="Links" title="Monitoring Links" note="jump to live tools">
          <MonitoringLinksGrid links={data.monitoringLinks} />
        </ConsolePanel>

        <ConsolePanel className="span-6" kicker="Endpoints" title="Useful Actuator Endpoints" note="read-only preview">
          <EndpointRows endpoints={data.actuatorEndpoints} />
        </ConsolePanel>

        <ConsolePanel className="span-12" kicker="Notes" title="Operator Highlights" note="what to watch">
          <NotesList notes={data.highlights} />
        </ConsolePanel>
      </section>
    </div>
  );
}
