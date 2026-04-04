import type {
  ActuatorSummaryPayload,
  OverviewPayload,
  StatisticsPayload,
} from "@/lib/domain/monitoring.read-models";
import type { Result } from "@/lib/domain/result";
import type { TransportError } from "@/lib/domain/errors";

export interface MonitoringReadPort {
  readonly getOverview: () => Promise<Result<OverviewPayload, TransportError>>;
  readonly getStatistics: () => Promise<Result<StatisticsPayload, TransportError>>;
  readonly getActuatorSummary: () => Promise<Result<ActuatorSummaryPayload, TransportError>>;
}
