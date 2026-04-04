import type { DataSource } from "@/lib/domain/monitoring.read-models";
import type { Result } from "@/lib/domain/result";
import { isOk } from "@/lib/domain/result";
import { MonitoringAggregateError } from "@/lib/domain/errors";

export async function resolveWithFallback<T, E>(
  live: () => Promise<Result<T, E>>,
  sample: () => Promise<Result<T, E>>,
  tag: (source: DataSource, payload: T) => T & { dataSource: DataSource },
): Promise<T & { dataSource: DataSource }> {
  const liveResult = await live();
  if (isOk(liveResult)) {
    return tag("live", liveResult.value);
  }

  const sampleResult = await sample();
  if (isOk(sampleResult)) {
    return tag("sample", sampleResult.value);
  }

  throw new MonitoringAggregateError(liveResult.error, sampleResult.error);
}
