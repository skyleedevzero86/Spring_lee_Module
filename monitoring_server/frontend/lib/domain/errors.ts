export type TransportError = Readonly<{
  kind: "TransportError";
  message: string;
  status?: number;
  cause?: unknown;
}>;

export function transportError(message: string, init?: { status?: number; cause?: unknown }): TransportError {
  return {
    kind: "TransportError",
    message,
    ...init,
  };
}

export class MonitoringAggregateError extends Error {
  constructor(
    public readonly liveError: unknown,
    public readonly sampleError: unknown,
  ) {
    super("Monitoring: live and sample sources both failed");
    this.name = "MonitoringAggregateError";
  }
}
