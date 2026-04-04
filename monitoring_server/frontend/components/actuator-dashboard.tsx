"use client";

import {
  ConsolePanel,
  DashboardTopbar,
  EndpointRows,
  InfrastructureCards,
  localizeOsFamily,
  localizeSource,
  localizeStatus,
  MonitoringLinksGrid,
  PillList,
  ServerSnapshot,
  StatPanel,
  StatusRows,
  StorageCards,
  TimeWindowCards,
} from "@/components/console-fragments";
import type { ConsoleStat } from "@/components/console-fragments";
import type { ActuatorSummaryResponse } from "@/lib/types";

function summaryStats(data: ActuatorSummaryResponse): ConsoleStat[] {
  return [
    { label: "상태", value: localizeStatus(data.health.status), caption: "현재 Actuator 상태", tone: data.health.status === "UP" ? "success" : "danger" },
    { label: "엔드포인트", value: String(data.links.length), caption: "공개된 링크", tone: "info" },
    { label: "지표", value: String(data.metricNames.length), caption: "미리보기 지표 수", tone: "warning" },
    { label: "스토어", value: String(data.storage.length), caption: "연결 스토어 스냅샷", tone: "success" },
    { label: "호스트", value: localizeOsFamily(data.server.operatingSystemFamily), caption: data.server.hostName, tone: "info" },
    { label: "데이터", value: localizeSource(data.dataSource ?? "sample"), caption: "페이로드 모드", tone: "warning" },
  ];
}

export function ActuatorDashboard({ data }: { data: ActuatorSummaryResponse }) {
  return (
    <div className="dashboard-page">
      <DashboardTopbar
        kicker="액추에이터"
        title="엔드포인트 및 페이로드 대시보드"
        subtitle="엔드포인트 링크, 페이로드 확인, 호스트 스냅샷, 스토어 사용량, 조회 프리셋을 한 화면에서 확인합니다."
        source={data.dataSource ?? "sample"}
        updatedAt={data.generatedAt}
        chips={[
          { key: "health", label: "상태" },
          { key: "info", label: "정보" },
          { key: "metrics", label: "지표" },
          { key: "prometheus", label: "프로메테우스" },
          { key: "custom", label: "커스텀" },
        ]}
      />

      <section className="dashboard-grid">
        {summaryStats(data).map((stat) => (
          <StatPanel key={stat.label} className="span-2" label={stat.label} value={stat.value} caption={stat.caption} tone={stat.tone} />
        ))}

        <ConsolePanel className="span-6" kicker="엔드포인트" title="공개된 엔드포인트" note="빠른 실행">
          <EndpointRows endpoints={data.links} />
        </ConsolePanel>

        <ConsolePanel className="span-6" kicker="도구" title="모니터링 링크" note="운영 도구 바로가기">
          <MonitoringLinksGrid links={data.monitoringLinks} />
        </ConsolePanel>

        <ConsolePanel className="span-4" kicker="호스트" title="서버 스냅샷" note={localizeOsFamily(data.server.operatingSystemFamily)}>
          <ServerSnapshot server={data.server} />
        </ConsolePanel>

        <ConsolePanel className="span-8" kicker="스토어" title="연결 스토어 사용량" note="커스텀 엔드포인트 포함 값">
          <StorageCards storage={data.storage} />
        </ConsolePanel>

        <ConsolePanel className="span-6" kicker="정보 JSON" title="/actuator/info" note="런타임 및 환경 메타데이터">
          <pre className="json-block">{JSON.stringify(data.info, null, 2)}</pre>
        </ConsolePanel>

        <ConsolePanel className="span-6" kicker="커스텀 JSON" title="/actuator/course-monitoring" note="커스텀 모니터링 페이로드">
          <pre className="json-block">{JSON.stringify(data.customEndpoint, null, 2)}</pre>
        </ConsolePanel>

        <ConsolePanel className="span-4" kicker="상태" title="컴포넌트 상태 상세" note="현재 컴포넌트 상태">
          <StatusRows items={data.health.components} />
        </ConsolePanel>

        <ConsolePanel className="span-4" kicker="인프라" title="프로브 스냅샷" note="가용성과 지연 시간">
          <InfrastructureCards items={data.infrastructure} />
        </ConsolePanel>

        <ConsolePanel className="span-4" kicker="지표" title="지표 이름 미리보기" note="내보낼 후보">
          <PillList items={data.metricNames} />
        </ConsolePanel>

        <ConsolePanel className="span-8" kicker="이력" title="추천 쿼리" note="기간별 쿼리 프리셋">
          <TimeWindowCards windows={data.timeWindows} />
        </ConsolePanel>

        <ConsolePanel className="span-4" kicker="가이드" title="읽는 법" note="현재 값과 이력">
          <p className="helper-note">
            <strong>Actuator</strong>는 현재 시점을 보여주고, <strong>Prometheus</strong>는 같은 지표의 이력을 저장합니다.
            <strong> Grafana</strong>는 이를 시간별, 일간, 주간, 월간 용량 차트로 시각화합니다.
          </p>
        </ConsolePanel>
      </section>
    </div>
  );
}
