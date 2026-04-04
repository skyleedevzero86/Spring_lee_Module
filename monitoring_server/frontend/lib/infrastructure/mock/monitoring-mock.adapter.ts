import type { MonitoringReadPort } from "@/lib/application/ports/monitoring-read.port";
import type {
  ActuatorSummaryPayload,
  OverviewPayload,
  StatisticsPayload,
} from "@/lib/domain/monitoring.read-models";
import { ok, type Result } from "@/lib/domain/result";
import type { TransportError } from "@/lib/domain/errors";
import type { DashboardWindowKey } from "@/lib/windowing";
import {
  createMockActuatorSummary,
  createMockOverview,
  createMockStatistics,
} from "@/lib/infrastructure/mock/dashboard-sample.data";

export function createMonitoringMockAdapter(): MonitoringReadPort {
  return {
    getOverview: (window?: DashboardWindowKey): Promise<Result<OverviewPayload, TransportError>> =>
      Promise.resolve(ok(createMockOverview(window))),

    getStatistics: (window?: DashboardWindowKey): Promise<Result<StatisticsPayload, TransportError>> =>
      Promise.resolve(ok(createMockStatistics(window))),

    getActuatorSummary: (): Promise<Result<ActuatorSummaryPayload, TransportError>> =>
      Promise.resolve(ok(createMockActuatorSummary())),
  };
}
