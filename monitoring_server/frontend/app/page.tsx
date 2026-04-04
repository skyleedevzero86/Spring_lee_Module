import { OverviewDashboard } from "@/components/overview-dashboard";
import { getOverview } from "@/lib/api";
import { isDashboardWindowKey, type DashboardWindowKey } from "@/lib/windowing";

type PageProps = {
  searchParams?: Promise<{ window?: string | string[] }>;
};

function resolveWindow(value: string | string[] | undefined): DashboardWindowKey {
  const candidate = Array.isArray(value) ? value[0] : value;
  return candidate && isDashboardWindowKey(candidate) ? candidate : "15s";
}

export default async function HomePage({ searchParams }: PageProps) {
  const params = searchParams ? await searchParams : undefined;
  const activeWindow = resolveWindow(params?.window);
  const overview = await getOverview(activeWindow);
  return <OverviewDashboard data={overview} activeWindow={activeWindow} />;
}
