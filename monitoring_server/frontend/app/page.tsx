import { OverviewDashboard } from "@/components/overview-dashboard";
import { getOverview } from "@/lib/api";

export default async function HomePage() {
  const overview = await getOverview();
  return <OverviewDashboard data={overview} />;
}

