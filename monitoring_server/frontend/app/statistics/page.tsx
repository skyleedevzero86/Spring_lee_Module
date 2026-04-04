import { StatisticsDashboard } from "@/components/statistics-dashboard";
import { getStatistics } from "@/lib/api";
import { isDashboardWindowKey, type DashboardWindowKey } from "@/lib/windowing";

type PageProps = {
  searchParams?: Promise<{ window?: string | string[] }>;
};

function resolveWindow(value: string | string[] | undefined): DashboardWindowKey {
  const candidate = Array.isArray(value) ? value[0] : value;
  return candidate && isDashboardWindowKey(candidate) ? candidate : "15s";
}

export default async function StatisticsPage({ searchParams }: PageProps) {
  const params = searchParams ? await searchParams : undefined;
  const activeWindow = resolveWindow(params?.window);
  const statistics = await getStatistics(activeWindow);
  return <StatisticsDashboard data={statistics} activeWindow={activeWindow} />;
}

