import { createDefaultMonitoringClient } from "@/lib/application/create-monitoring-client";

const client = createDefaultMonitoringClient();

export function getOverview() {
  return client.getOverview();
}

export function getStatistics() {
  return client.getStatistics();
}

export function getActuatorSummary() {
  return client.getActuatorSummary();
}
