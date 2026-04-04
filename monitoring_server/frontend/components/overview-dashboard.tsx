"use client";

import { useTransition } from "react";
import { usePathname, useRouter, useSearchParams } from "next/navigation";
import { BarList, DonutBreakdown, LineChart } from "@/components/chart-primitives";
import {
  ConsolePanel,
  DashboardTopbar,
  EndpointRows,
  InfrastructureCards,
  localizeOsFamily,
  localizeStatus,
  MonitoringLinksGrid,
  NotesList,
  ServerSnapshot,
  StatPanel,
  StatusRows,
  StorageCards,
} from "@/components/console-fragments";
import type { DistributionItem, OverviewResponse } from "@/lib/types";
import {
  DASHBOARD_WINDOW_OPTIONS,
  type DashboardWindowKey,
  getActiveWindowMeta,
} from "@/lib/windowing";

function healthDistribution(statuses: OverviewResponse["health"]["components"]): DistributionItem[] {
  const counts = new Map<string, number>();
  for (const status of statuses) {
    counts.set(status.status, (counts.get(status.status) ?? 0) + 1);
  }

  return Array.from(counts.entries()).map(([label, value]) => ({
    label: localizeStatus(label),
    value,
    accent:
      label === "UP"
        ? "#88ff2a"
        : label === "DOWN"
          ? "#ff5d73"
          : label === "OUT_OF_SERVICE"
            ? "#ff9b4d"
            : "#57c7ff",
  }));
}

export function OverviewDashboard({
  data,
  activeWindow,
}: {
  data: OverviewResponse;
  activeWindow: DashboardWindowKey;
}) {
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();
  const [, startTransition] = useTransition();
  const activeMeta = getActiveWindowMeta(activeWindow);

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
        kicker="운영 관제"
        title="인프라 모니터링 콘솔"
        subtitle="현재 상태, 실시간 용량, 연결 스토어 사용량, 운영 링크를 한 화면에서 확인합니다."
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
        {data.kpis.map((kpi) => (
          <StatPanel
            key={kpi.label}
            className="span-2"
            label={kpi.label}
            value={kpi.value}
            caption={kpi.caption}
            tone={kpi.tone}
          />
        ))}

        <ConsolePanel className="span-6" kicker="트래픽" title="요청 추이" note={`분당 요청 수 / ${activeMeta.rangeLabel}`}>
          <LineChart points={data.requestTrend} accent="#57c7ff" unit="건/분" />
        </ConsolePanel>

        <ConsolePanel className="span-3" kicker="지연 시간" title="응답 지연" note={`@Timed / ${activeMeta.chipLabel}`}>
          <LineChart points={data.latencyTrend} accent="#ffb020" unit="ms" />
        </ConsolePanel>

        <ConsolePanel className="span-3" kicker="캐시" title="캐시 적중률" note={`게이지 % / ${activeMeta.chipLabel}`}>
          <LineChart points={data.cacheTrend} accent="#88ff2a" unit="%" />
        </ConsolePanel>

        <ConsolePanel className="span-5" kicker="호스트" title="애플리케이션 호스트 스냅샷" note={localizeOsFamily(data.server.operatingSystemFamily)}>
          <ServerSnapshot server={data.server} />
        </ConsolePanel>

        <ConsolePanel className="span-7" kicker="스토어" title="연결된 스토어" note="현재 용량과 선택 기간 증감">
          <StorageCards storage={data.storage} />
        </ConsolePanel>

        <ConsolePanel className="span-4" kicker="상태" title="컴포넌트 상태" note="Actuator 기여자">
          <DonutBreakdown items={healthDistribution(data.health.components)} />
          <StatusRows items={data.health.components} />
        </ConsolePanel>

        <ConsolePanel className="span-4" kicker="섹션" title="섹션 진행률" note="학습 지표 매핑">
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

        <ConsolePanel className="span-4" kicker="인프라" title="프로브 상태" note="가용성과 지연 시간">
          <InfrastructureCards items={data.infrastructure} />
        </ConsolePanel>

        <ConsolePanel className="span-6" kicker="링크" title="모니터링 링크" note="실시간 도구 바로가기">
          <MonitoringLinksGrid links={data.monitoringLinks} />
        </ConsolePanel>

        <ConsolePanel className="span-6" kicker="엔드포인트" title="주요 Actuator 엔드포인트" note="읽기 전용 미리보기">
          <EndpointRows endpoints={data.actuatorEndpoints} />
        </ConsolePanel>

        <ConsolePanel className="span-12" kicker="메모" title="운영 포인트" note="확인할 항목">
          <NotesList notes={data.highlights} />
        </ConsolePanel>
      </section>
    </div>
  );
}
