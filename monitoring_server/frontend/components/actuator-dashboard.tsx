import {
  ConsolePanel,
  DashboardTopbar,
  EndpointRows,
  InfrastructureCards,
  MonitoringLinksGrid,
  PillList,
  ServerSnapshot,
  StatPanel,
  StatusRows,
  StorageCards,
  TimeWindowCards,
} from "@/components/console-fragments";
import type { ConsoleStat } from "@/components/console-fragments";
import type { ActuatorSummaryResponse } from "@/lib/types";

function summaryStats(data: ActuatorSummaryResponse): ConsoleStat[] {
  return [
    { label: "Health", value: data.health.status, caption: "current actuator status", tone: data.health.status === "UP" ? "success" : "danger" },
    { label: "Endpoints", value: String(data.links.length), caption: "published links", tone: "info" },
    { label: "Metrics", value: String(data.metricNames.length), caption: "metric names in preview", tone: "warning" },
    { label: "Stores", value: String(data.storage.length), caption: "connected store snapshots", tone: "success" },
    { label: "Host", value: data.server.operatingSystemFamily, caption: data.server.hostName, tone: "info" },
    { label: "Source", value: data.dataSource ?? "sample", caption: "payload mode", tone: "warning" },
  ];
}

export function ActuatorDashboard({ data }: { data: ActuatorSummaryResponse }) {
  return (
    <div className="dashboard-page">
      <DashboardTopbar
        kicker="Actuator View"
        title="Raw Endpoint and Payload Dashboard"
        subtitle="A compact operations layout for endpoint links, payload inspection, host snapshots, store usage, and query presets."
        source={data.dataSource ?? "sample"}
        updatedAt={data.generatedAt}
        chips={[
          { key: "health", label: "health" },
          { key: "info", label: "info" },
          { key: "metrics", label: "metrics" },
          { key: "prometheus", label: "prometheus" },
          { key: "custom", label: "custom" },
        ]}
      />

      <section className="dashboard-grid">
        {summaryStats(data).map((stat) => (
          <StatPanel key={stat.label} className="span-2" label={stat.label} value={stat.value} caption={stat.caption} tone={stat.tone} />
        ))}

        <ConsolePanel className="span-6" kicker="Endpoints" title="Published Endpoints" note="quick launch">
          <EndpointRows endpoints={data.links} />
        </ConsolePanel>

        <ConsolePanel className="span-6" kicker="Tools" title="Monitoring Links" note="admin, actuator, prometheus, grafana">
          <MonitoringLinksGrid links={data.monitoringLinks} />
        </ConsolePanel>

        <ConsolePanel className="span-4" kicker="Host" title="Server Snapshot" note={data.server.operatingSystemFamily}>
          <ServerSnapshot server={data.server} />
        </ConsolePanel>

        <ConsolePanel className="span-8" kicker="Stores" title="Connected Store Usage" note="included in the custom endpoint">
          <StorageCards storage={data.storage} />
        </ConsolePanel>

        <ConsolePanel className="span-6" kicker="Info JSON" title="/actuator/info" note="runtime and environment metadata">
          <pre className="json-block">{JSON.stringify(data.info, null, 2)}</pre>
        </ConsolePanel>

        <ConsolePanel className="span-6" kicker="Custom JSON" title="/actuator/course-monitoring" note="custom monitoring payload">
          <pre className="json-block">{JSON.stringify(data.customEndpoint, null, 2)}</pre>
        </ConsolePanel>

        <ConsolePanel className="span-4" kicker="Health" title="Health Component Details" note="current component status">
          <StatusRows items={data.health.components} />
        </ConsolePanel>

        <ConsolePanel className="span-4" kicker="Infrastructure" title="Probe Snapshots" note="availability and latency">
          <InfrastructureCards items={data.infrastructure} />
        </ConsolePanel>

        <ConsolePanel className="span-4" kicker="Metrics" title="Metric Name Preview" note="export candidates">
          <PillList items={data.metricNames} />
        </ConsolePanel>

        <ConsolePanel className="span-8" kicker="History" title="Suggested Queries" note="query presets by time window">
          <TimeWindowCards windows={data.timeWindows} />
        </ConsolePanel>

        <ConsolePanel className="span-4" kicker="Guide" title="How To Read This" note="current vs history">
          <p className="helper-note">
            <strong>Actuator</strong> shows the current moment. <strong>Prometheus</strong> stores the same metrics over time.
            <strong> Grafana</strong> turns them into hourly, daily, weekly, and monthly capacity views.
          </p>
        </ConsolePanel>
      </section>
    </div>
  );
}
