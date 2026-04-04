"use client";

import { useTransition } from "react";
import { usePathname, useRouter, useSearchParams } from "next/navigation";
import { BarList, DonutBreakdown, LineChart } from "@/components/chart-primitives";
import {
  ConsolePanel,
  InfrastructureCards,
  localizeOsFamily,
  localizeStatus,
  MetricRows,
  NotesList,
  ServerSnapshot,
  StatPanel,
  StorageCards,
  TagCards,
  TimeWindowCards,
} from "@/components/console-fragments";
import type { ConsoleStat } from "@/components/console-fragments";
import { DashboardTopbar } from "@/components/console-fragments";
import type { StatisticsResponse } from "@/lib/types";
import {
  DASHBOARD_WINDOW_OPTIONS,
  type DashboardWindowKey,
  getActiveWindowMeta,
  orderTimeWindows,
} from "@/lib/windowing";

function signedMegabytes(value: number) {
  const mb = Math.abs(value) / 1024 / 1024;
  return `${value >= 0 ? "+" : "-"}${mb.toFixed(1)} MB`;
}

function summaryStats(data: StatisticsResponse, activeWindowLabel: string, activeRangeLabel: string): ConsoleStat[] {
  const [postgresql, redis] = data.storage;

  return [
    {
      label: "호스트 메모리",
      value: `${data.server.memoryUsagePercent.toFixed(1)}%`,
      caption: `${(data.server.usedMemoryBytes / 1024 / 1024 / 1024).toFixed(1)} GB 사용 중`,
      tone: "info",
    },
    {
      label: "호스트 디스크",
      value: `${data.server.diskUsagePercent.toFixed(1)}%`,
      caption: `${(data.server.usedDiskBytes / 1024 / 1024 / 1024).toFixed(1)} GB 사용 중`,
      tone: "warning",
    },
    {
      label: "PostgreSQL",
      value: postgresql?.usedBytes == null ? "없음" : `${(postgresql.usedBytes / 1024 / 1024 / 1024).toFixed(1)} GB`,
      caption: postgresql ? `${postgresql.growthLabel} ${signedMegabytes(postgresql.dailyGrowthBytes)}` : "데이터 없음",
      tone: "success",
    },
    {
      label: "Redis",
      value: redis?.usedBytes == null ? "없음" : `${(redis.usedBytes / 1024 / 1024).toFixed(1)} MB`,
      caption: redis ? `${redis.growthLabel} ${signedMegabytes(redis.dailyGrowthBytes)}` : "데이터 없음",
      tone: "success",
    },
    { label: "지표 수", value: String(data.highlightedMetrics.length), caption: "주요 지표", tone: "info" },
    { label: "조회 기간", value: activeWindowLabel, caption: activeRangeLabel, tone: "warning" },
  ];
}

export function StatisticsDashboard({
  data,
  activeWindow,
}: {
  data: StatisticsResponse;
  activeWindow: DashboardWindowKey;
}) {
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();
  const [, startTransition] = useTransition();
  const activeMeta = getActiveWindowMeta(activeWindow);
  const orderedWindows = orderTimeWindows(data.timeWindows, activeWindow);
  const localizedHealthBreakdown = data.healthBreakdown.map((item) => ({
    ...item,
    label: localizeStatus(item.label),
  }));

  function navigateWindow(window: DashboardWindowKey) {
    const next = new URLSearchParams(searchParams.toString());
    if (window === "15s") {
      next.delete("window");
    } else {
      next.set("window", window);
    }
    const query = next.toString();
    startTransition(() => {
      router.replace(query ? `${pathname}?${query}` : pathname);
    });
  }

  return (
    <div className="dashboard-page">
      <DashboardTopbar
        kicker="지표 분석"
        title="메트릭·용량·증감 대시보드"
        subtitle="호스트 자원, PostgreSQL과 Redis 용량, 상태 분포, 기간별 조회 쿼리를 밀도 있게 확인합니다."
        source={data.dataSource ?? "sample"}
        updatedAt={data.generatedAt}
        chips={DASHBOARD_WINDOW_OPTIONS.map((window) => ({
          key: window.key,
          label: window.label,
          active: window.key === activeWindow,
          onClick: () => navigateWindow(window.key),
        }))}
      />

      <section className="dashboard-grid">
        {summaryStats(data, activeMeta.chipLabel, activeMeta.rangeLabel).map((stat) => (
          <StatPanel
            key={stat.label}
            className="span-2"
            label={stat.label}
            value={stat.value}
            caption={stat.caption}
            tone={stat.tone}
          />
        ))}

        <ConsolePanel className="span-6" kicker="트래픽" title="요청 추이" note={`분당 요청 수 / ${activeMeta.rangeLabel}`}>
          <LineChart points={data.requestTrend} accent="#57c7ff" unit="건/분" />
        </ConsolePanel>

        <ConsolePanel className="span-3" kicker="백분위" title="타이머 백분위" note={`P50 / P90 / P95 / ${activeMeta.chipLabel}`}>
          <BarList
            items={data.timerPercentiles.map((item) => ({
              label: item.label,
              value: item.value,
              accent: item.accent,
              subtitle: "합성 지연 시간",
              suffix: " ms",
            }))}
          />
        </ConsolePanel>

        <ConsolePanel className="span-3" kicker="상태" title="상태 분포" note="컴포넌트 분포">
          <DonutBreakdown items={localizedHealthBreakdown} />
        </ConsolePanel>

        <ConsolePanel className="span-8" kicker="지표" title="주요 지표 샘플" note="Prometheus 중심 값">
          <MetricRows metrics={data.highlightedMetrics} />
        </ConsolePanel>

        <ConsolePanel className="span-4" kicker="이력" title="추천 시간창" note="Grafana에 바로 붙여넣기">
          <TimeWindowCards windows={orderedWindows} activeLabel={activeMeta.timeWindowLabel} />
        </ConsolePanel>

        <ConsolePanel className="span-4" kicker="호스트" title="서버 스냅샷" note={localizeOsFamily(data.server.operatingSystemFamily)}>
          <ServerSnapshot server={data.server} />
        </ConsolePanel>

        <ConsolePanel className="span-4" kicker="스토어" title="스토어 사용량" note="현재 크기와 선택 기간 증감">
          <StorageCards storage={data.storage} />
        </ConsolePanel>

        <ConsolePanel className="span-4" kicker="인프라" title="DB 및 Redis 상태" note="프로브 지연 시간">
          <InfrastructureCards items={data.infrastructure} />
        </ConsolePanel>

        <ConsolePanel className="span-4" kicker="태그" title="메트릭 태그" note="페이지 / 섹션 / 컴포넌트">
          <TagCards tags={data.tags} />
        </ConsolePanel>

        <ConsolePanel className="span-4" kicker="로드맵" title="섹션 매핑" note="학습 진행률 게이지">
          <BarList
            items={data.sections.map((section) => ({
              label: section.title,
              value: section.completionRate,
              accent: section.accent,
              subtitle: section.focus,
              suffix: "%",
            }))}
          />
        </ConsolePanel>

        <ConsolePanel className="span-4" kicker="메모" title="운영 노트" note="이력 해석 가이드">
          <NotesList notes={data.notes} />
        </ConsolePanel>
      </section>
    </div>
  );
}
