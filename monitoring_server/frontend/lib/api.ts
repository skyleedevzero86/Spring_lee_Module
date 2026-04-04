import { createDefaultMonitoringClient } from "@/lib/application/create-monitoring-client";
import type { DashboardWindowKey } from "@/lib/windowing";

const client = createDefaultMonitoringClient();

export function getOverview(window?: DashboardWindowKey) {
  return client.getOverview(window);
}

export function getStatistics(window?: DashboardWindowKey) {
  return client.getStatistics(window);
}

export function getActuatorSummary() {
  return client.getActuatorSummary();
}
