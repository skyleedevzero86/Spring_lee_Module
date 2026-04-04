import type { MonitoringReadPort } from "@/lib/application/ports/monitoring-read.port";
import type {
  ActuatorSummaryPayload,
  OverviewPayload,
  StatisticsPayload,
} from "@/lib/domain/monitoring.read-models";
import { err, ok, type Result } from "@/lib/domain/result";
import type { TransportError } from "@/lib/domain/errors";
import { transportError } from "@/lib/domain/errors";
import type { DashboardWindowKey } from "@/lib/windowing";

async function fetchJson<T>(url: string): Promise<Result<T, TransportError>> {
  try {
    const response = await fetch(url, {
      headers: { accept: "application/json" },
      next: { revalidate: 5 },
    });

    if (!response.ok) {
      return err(
        transportError(`HTTP ${response.status} ${response.statusText}`, {
          status: response.status,
        }),
      );
    }

    const data = (await response.json()) as T;
    return ok(data);
  } catch (cause) {
    return err(transportError("Network or parse error", { cause }));
  }
}

function withWindow(url: string, window?: DashboardWindowKey) {
  if (!window) return url;
  const next = new URL(url);
  next.searchParams.set("window", window);
  return next.toString();
}

export function createMonitoringHttpAdapter(baseUrl: string): MonitoringReadPort {
  const root = baseUrl.replace(/\/$/, "");

  return {
    getOverview: (window?: DashboardWindowKey): Promise<Result<OverviewPayload, TransportError>> =>
      fetchJson<OverviewPayload>(withWindow(`${root}/api/admin/overview`, window)),

    getStatistics: (window?: DashboardWindowKey): Promise<Result<StatisticsPayload, TransportError>> =>
      fetchJson<StatisticsPayload>(withWindow(`${root}/api/admin/statistics`, window)),

    getActuatorSummary: (): Promise<Result<ActuatorSummaryPayload, TransportError>> =>
      fetchJson<ActuatorSummaryPayload>(`${root}/api/admin/actuator-summary`),
  };
}
