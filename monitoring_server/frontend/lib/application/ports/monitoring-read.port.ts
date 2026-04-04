import type {
  ActuatorSummaryPayload,
  OverviewPayload,
  StatisticsPayload,
} from "@/lib/domain/monitoring.read-models";
import type { Result } from "@/lib/domain/result";
import type { TransportError } from "@/lib/domain/errors";
import type { DashboardWindowKey } from "@/lib/windowing";

export interface MonitoringReadPort {
  readonly getOverview: (window?: DashboardWindowKey) => Promise<Result<OverviewPayload, TransportError>>;
  readonly getStatistics: (window?: DashboardWindowKey) => Promise<Result<StatisticsPayload, TransportError>>;
  readonly getActuatorSummary: () => Promise<Result<ActuatorSummaryPayload, TransportError>>;
}
