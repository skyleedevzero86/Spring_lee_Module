import { loadActuatorSummary, loadOverview, loadStatistics } from "@/lib/application/use-cases/monitoring-queries";
import { createMonitoringHttpAdapter } from "@/lib/infrastructure/http/monitoring-http.adapter";
import { createMonitoringMockAdapter } from "@/lib/infrastructure/mock/monitoring-mock.adapter";
import { getMonitoringApiBaseUrl } from "@/lib/infrastructure/config/monitoring-env";

export function createDefaultMonitoringClient() {
  const baseUrl = getMonitoringApiBaseUrl();
  const live = createMonitoringHttpAdapter(baseUrl);
  const sample = createMonitoringMockAdapter();

  return {
    getOverview: () => loadOverview(live, sample),
    getStatistics: () => loadStatistics(live, sample),
    getActuatorSummary: () => loadActuatorSummary(live, sample),
  };
}
