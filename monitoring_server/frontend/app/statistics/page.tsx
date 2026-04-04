import { StatisticsDashboard } from "@/components/statistics-dashboard";
import { getStatistics } from "@/lib/api";

export default async function StatisticsPage() {
  const statistics = await getStatistics();
  return <StatisticsDashboard data={statistics} />;
}

