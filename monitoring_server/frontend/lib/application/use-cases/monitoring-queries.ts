import type { MonitoringReadPort } from "@/lib/application/ports/monitoring-read.port";
import type {
  ActuatorSummaryResponse,
  OverviewResponse,
  StatisticsResponse,
} from "@/lib/domain/monitoring.read-models";
import { resolveWithFallback } from "@/lib/application/use-cases/resolve-with-fallback";

export function loadOverview(live: MonitoringReadPort, sample: MonitoringReadPort): Promise<OverviewResponse> {
  return resolveWithFallback(
    () => live.getOverview(),
    () => sample.getOverview(),
    (source, payload) => ({ ...payload, dataSource: source }),
  );
}

export function loadStatistics(live: MonitoringReadPort, sample: MonitoringReadPort): Promise<StatisticsResponse> {
  return resolveWithFallback(
    () => live.getStatistics(),
    () => sample.getStatistics(),
    (source, payload) => ({ ...payload, dataSource: source }),
  );
}

export function loadActuatorSummary(
  live: MonitoringReadPort,
  sample: MonitoringReadPort,
): Promise<ActuatorSummaryResponse> {
  return resolveWithFallback(
    () => live.getActuatorSummary(),
    () => sample.getActuatorSummary(),
    (source, payload) => ({ ...payload, dataSource: source }),
  );
}
