<script setup lang="ts">
import { useOpsPilotDashboard } from './composables/useOpsPilotDashboard'
import type { IncidentType, Severity, StatsGranularity, RangePreset } from './domain/incident'

const {
  form,
  loading,
  statsLoading,
  error,
  latest,
  incidents,
  stats,
  mockMode,
  granularity,
  rangePreset,
  faultMessage,
  flow,
  cacheHitLabel,
  timelineCanvas,
  severityCanvas,
  categoryCanvas,
  teamCanvas,
  runTriage,
  changeMockMode,
  runFault,
  setType,
  setSeverity,
} = useOpsPilotDashboard()

const types: IncidentType[] = [
  'DATABASE',
  'REDIS',
  'EXTERNAL_API',
  'TIMEOUT',
  'CPU',
  'MEMORY',
  'HTTP',
  'UNKNOWN',
]
const severities: Severity[] = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL']
const grains: StatsGranularity[] = ['HOUR', 'DAY', 'WEEK']
const ranges: RangePreset[] = ['24h', '7d', '30d']
const mockModes = ['NORMAL', 'SLOW', 'HTTP_429', 'HTTP_500', 'HTTP_503', 'TIMEOUT', 'MALFORMED_RESPONSE']
</script>

<template>
  <main class="shell">
    <header class="hero">
      <p class="eyebrow">OpsPilot</p>
      <h1>AI Incident Triage</h1>
      <p class="lead">
        장애 입력부터 Fingerprint · Cache · AI 분석 · DB 저장 · 알림까지 흐름을 화면에서 확인합니다.
      </p>
    </header>

    <section class="flow panel">
      <h2>처리 흐름</h2>
      <ol class="pipeline">
        <li
          v-for="step in flow"
          :key="step.id"
          :class="{ done: step.done, active: step.active }"
        >
          <span class="dot"></span>
          <strong>{{ step.label }}</strong>
          <small>{{ step.detail }}</small>
        </li>
      </ol>
    </section>

    <div class="grid">
      <section class="panel">
        <h2>장애 분류 요청</h2>
        <div class="field">
          <label for="title">제목</label>
          <input id="title" v-model="form.title" />
        </div>
        <div class="field">
          <label for="description">설명</label>
          <textarea id="description" v-model="form.description" rows="4" />
        </div>
        <div class="field">
          <label for="source">소스</label>
          <input id="source" v-model="form.source" />
        </div>
        <div class="chips">
          <button
            v-for="type in types"
            :key="type"
            type="button"
            class="chip"
            :class="{ on: form.type === type }"
            @click="setType(type)"
          >
            {{ type }}
          </button>
        </div>
        <div class="chips">
          <button
            v-for="sev in severities"
            :key="sev"
            type="button"
            class="chip soft"
            :class="{ on: form.severity === sev }"
            @click="setSeverity(sev)"
          >
            {{ sev }}
          </button>
        </div>
        <button class="primary" type="button" :disabled="loading" @click="runTriage">
          {{ loading ? '분석 중…' : 'AI Triage 실행' }}
        </button>
        <p v-if="error" class="error">{{ error }}</p>
      </section>

      <section class="panel">
        <h2>최근 분석 결과</h2>
        <template v-if="latest">
          <div class="result-grid">
            <div>
              <span class="label">긴급</span>
              <strong :class="latest.urgent ? 'bad' : 'ok'">
                {{ latest.urgent ? 'YES' : 'NO' }}
                ({{ (latest.urgencyProbability * 100).toFixed(0) }}%)
              </strong>
            </div>
            <div>
              <span class="label">유형</span>
              <strong>{{ latest.category }}</strong>
            </div>
            <div>
              <span class="label">심각도</span>
              <strong>{{ latest.analysisSeverity }}</strong>
            </div>
            <div>
              <span class="label">담당 팀</span>
              <strong>{{ latest.team }}</strong>
            </div>
            <div>
              <span class="label">분석 소스</span>
              <strong>{{ latest.analysisSource }}</strong>
            </div>
            <div>
              <span class="label">캐시</span>
              <strong :class="latest.cacheHit ? 'ok' : ''">
                {{ latest.cacheHit ? 'HIT' : 'MISS' }}
              </strong>
            </div>
          </div>
          <p class="summary">{{ latest.summary }}</p>
          <p class="action">{{ latest.recommendedAction }}</p>
        </template>
        <p v-else class="muted">아직 실행된 분석이 없습니다.</p>
      </section>
    </div>

    <section class="panel">
      <div class="section-head">
        <h2>통계 대시보드</h2>
        <div class="controls">
          <select v-model="rangePreset">
            <option v-for="r in ranges" :key="r" :value="r">{{ r }}</option>
          </select>
          <select v-model="granularity">
            <option v-for="g in grains" :key="g" :value="g">{{ g }}</option>
          </select>
        </div>
      </div>

      <div v-if="stats" class="kpi">
        <article>
          <span>총 장애</span>
          <strong>{{ stats.totalIncidents }}</strong>
        </article>
        <article>
          <span>긴급</span>
          <strong>{{ stats.urgentCount }}</strong>
        </article>
        <article>
          <span>캐시 적중률</span>
          <strong>{{ cacheHitLabel }}</strong>
        </article>
        <article>
          <span>Fallback</span>
          <strong>{{ stats.fallbackCount }}</strong>
        </article>
        <article>
          <span>캐시 HIT</span>
          <strong>{{ stats.cacheHitCount }}</strong>
        </article>
        <article>
          <span>기간</span>
          <strong class="tiny">{{ rangePreset }} / {{ granularity }}</strong>
        </article>
      </div>
      <p v-if="statsLoading" class="muted">통계 갱신 중…</p>

      <div class="charts">
        <div class="chart-card wide">
          <canvas ref="timelineCanvas"></canvas>
        </div>
        <div class="chart-card">
          <canvas ref="severityCanvas"></canvas>
        </div>
        <div class="chart-card">
          <canvas ref="categoryCanvas"></canvas>
        </div>
        <div class="chart-card">
          <canvas ref="teamCanvas"></canvas>
        </div>
      </div>

      <div v-if="stats" class="source-row">
        <div v-for="(count, source) in stats.bySource" :key="source" class="pill">
          {{ source }} · {{ count }}
        </div>
      </div>
    </section>

    <div class="grid">
      <section class="panel">
        <h2>Mock AI 모드</h2>
        <p class="muted">현재: <strong>{{ mockMode }}</strong></p>
        <div class="chips">
          <button
            v-for="mode in mockModes"
            :key="mode"
            type="button"
            class="chip"
            :class="{ on: mockMode === mode }"
            @click="changeMockMode(mode)"
          >
            {{ mode }}
          </button>
        </div>
      </section>

      <section class="panel">
        <h2>Fault Simulator</h2>
        <div class="chips">
          <button type="button" class="chip soft" @click="runFault('timeout', '?delay=2000')">Timeout</button>
          <button type="button" class="chip soft" @click="runFault('http-500')">HTTP 500</button>
          <button type="button" class="chip soft" @click="runFault('http-503')">HTTP 503</button>
          <button type="button" class="chip soft" @click="runFault('rate-limit')">429</button>
          <button type="button" class="chip soft" @click="runFault('database/slow', '?delay=1500')">DB Slow</button>
          <button type="button" class="chip soft" @click="runFault('database/error')">DB Error</button>
          <button type="button" class="chip soft" @click="runFault('redis/error')">Redis Error</button>
          <button type="button" class="chip soft" @click="runFault('random')">Random</button>
        </div>
        <p v-if="faultMessage" class="muted">{{ faultMessage }}</p>
      </section>
    </div>

    <section class="panel">
      <h2>최근 Incident</h2>
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>시간</th>
              <th>제목</th>
              <th>유형</th>
              <th>심각도</th>
              <th>팀</th>
              <th>소스</th>
              <th>캐시</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in incidents" :key="item.id">
              <td>{{ item.createdAt.replace('T', ' ').slice(0, 19) }}</td>
              <td>{{ item.title }}</td>
              <td>{{ item.category }}</td>
              <td>{{ item.analysisSeverity }}</td>
              <td>{{ item.team }}</td>
              <td>{{ item.analysisSource }}</td>
              <td>{{ item.cacheHit ? 'HIT' : 'MISS' }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </main>
</template>
