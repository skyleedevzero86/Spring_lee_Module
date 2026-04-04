import { loadActuatorSummary, loadOverview, loadStatistics } from "@/lib/application/use-cases/monitoring-queries";
import { createMonitoringHttpAdapter } from "@/lib/infrastructure/http/monitoring-http.adapter";
import { createMonitoringMockAdapter } from "@/lib/infrastructure/mock/monitoring-mock.adapter";
import { getMonitoringApiBaseUrl } from "@/lib/infrastructure/config/monitoring-env";
import type { DashboardWindowKey } from "@/lib/windowing";

export function createDefaultMonitoringClient() {
  const baseUrl = getMonitoringApiBaseUrl();
  const live = createMonitoringHttpAdapter(baseUrl);
  const sample = createMonitoringMockAdapter();

  return {
    getOverview: (window?: DashboardWindowKey) => loadOverview(live, sample, window),
    getStatistics: (window?: DashboardWindowKey) => loadStatistics(live, sample, window),
    getActuatorSummary: () => loadActuatorSummary(live, sample),
  };
}
