export function getMonitoringApiBaseUrl(): string {
  return process.env.MONITORING_API_BASE_URL ?? "http://localhost:8080";
}
