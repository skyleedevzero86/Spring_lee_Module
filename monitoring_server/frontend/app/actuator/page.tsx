import { ActuatorDashboard } from "@/components/actuator-dashboard";
import { getActuatorSummary } from "@/lib/api";

export default async function ActuatorPage() {
  const actuatorSummary = await getActuatorSummary();
  return <ActuatorDashboard data={actuatorSummary} />;
}

