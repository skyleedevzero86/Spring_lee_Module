<script setup lang="ts">
import { Chart, registerables } from 'chart.js'
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import {
  fetchDetailedStats,
  fetchPolicy,
  fetchUsageAlerts,
  runAbTest,
  updatePolicy,
  type AbTestResult,
  type DetailedStats,
  type RuntimePolicy,
  type UsageAlert,
} from '../api/searchAiApi'

Chart.register(...registerables)

const props = defineProps<{ token: string }>()

const granularity = ref<'HOUR' | 'DAY' | 'WEEK'>('DAY')
const rangeDays = ref(7)
const usernameFilter = ref('')
const stats = ref<DetailedStats | null>(null)
const alerts = ref<UsageAlert[]>([])
const policy = ref<RuntimePolicy | null>(null)
const loading = ref(false)
const error = ref('')
const saved = ref('')
const abPrompt = ref('Design PostgreSQL failover architecture')
const abTiers = ref('SOL,ASTRA')
const abLoading = ref(false)
const abResult = ref<AbTestResult | null>(null)

const timelineCanvas = ref<HTMLCanvasElement | null>(null)
const tierCanvas = ref<HTMLCanvasElement | null>(null)
const dowCanvas = ref<HTMLCanvasElement | null>(null)
const hourCanvas = ref<HTMLCanvasElement | null>(null)
const typeCanvas = ref<HTMLCanvasElement | null>(null)
const routingCanvas = ref<HTMLCanvasElement | null>(null)

let timelineChart: Chart | null = null
let tierChart: Chart | null = null
let dowChart: Chart | null = null
let hourChart: Chart | null = null
let typeChart: Chart | null = null
let routingChart: Chart | null = null

const destroyCharts = () => {
  timelineChart?.destroy()
  tierChart?.destroy()
  dowChart?.destroy()
  hourChart?.destroy()
  typeChart?.destroy()
  routingChart?.destroy()
  timelineChart = null
  tierChart = null
  dowChart = null
  hourChart = null
  typeChart = null
  routingChart = null
}

const palette = ['#1f5f8b', '#2a8f7b', '#c27803', '#b42318', '#6941c6', '#026aa2', '#3e4784']

const renderCharts = async () => {
  if (!stats.value) return
  await nextTick()
  destroyCharts()
  const data = stats.value

  if (timelineCanvas.value) {
    timelineChart = new Chart(timelineCanvas.value, {
      type: 'line',
      data: {
        labels: data.timeline.map((item) => item.bucket),
        datasets: [
          {
            label: '토큰',
            data: data.timeline.map((item) => item.tokens),
            borderColor: '#1f5f8b',
            backgroundColor: 'rgba(31,95,139,0.15)',
            tension: 0.25,
            yAxisID: 'y',
          },
          {
            label: '비용($)',
            data: data.timeline.map((item) => item.cost),
            borderColor: '#2a8f7b',
            backgroundColor: 'rgba(42,143,123,0.12)',
            tension: 0.25,
            yAxisID: 'y1',
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        scales: {
          y: { position: 'left', title: { display: true, text: 'tokens' } },
          y1: { position: 'right', grid: { drawOnChartArea: false }, title: { display: true, text: 'USD' } },
        },
      },
    })
  }

  if (tierCanvas.value) {
    tierChart = new Chart(tierCanvas.value, {
      type: 'doughnut',
      data: {
        labels: data.byTier.map((item) => item.name),
        datasets: [{
          data: data.byTier.map((item) => item.tokens),
          backgroundColor: palette,
        }],
      },
      options: { responsive: true, maintainAspectRatio: false },
    })
  }

  if (dowCanvas.value) {
    dowChart = new Chart(dowCanvas.value, {
      type: 'bar',
      data: {
        labels: data.byDayOfWeek.map((item) => item.name),
        datasets: [{
          label: '요일별 토큰',
          data: data.byDayOfWeek.map((item) => item.tokens),
          backgroundColor: '#1f5f8b',
        }],
      },
      options: { responsive: true, maintainAspectRatio: false },
    })
  }

  if (hourCanvas.value) {
    hourChart = new Chart(hourCanvas.value, {
      type: 'bar',
      data: {
        labels: data.byHour.map((item) => item.name),
        datasets: [{
          label: '시간대별 토큰',
          data: data.byHour.map((item) => item.tokens),
          backgroundColor: '#2a8f7b',
        }],
      },
      options: { responsive: true, maintainAspectRatio: false },
    })
  }

  if (typeCanvas.value) {
    typeChart = new Chart(typeCanvas.value, {
      type: 'pie',
      data: {
        labels: data.byRequestType.map((item) => item.name),
        datasets: [{
          data: data.byRequestType.map((item) => item.tokens),
          backgroundColor: palette,
        }],
      },
      options: { responsive: true, maintainAspectRatio: false },
    })
  }

  if (routingCanvas.value && data.byRoutingType?.length) {
    routingChart = new Chart(routingCanvas.value, {
      type: 'doughnut',
      data: {
        labels: data.byRoutingType.map((item) => item.name),
        datasets: [{
          data: data.byRoutingType.map((item) => item.tokens),
          backgroundColor: palette,
        }],
      },
      options: { responsive: true, maintainAspectRatio: false },
    })
  }
}

const load = async () => {
  loading.value = true
  error.value = ''
  try {
    const to = new Date()
    const from = new Date(to.getTime() - rangeDays.value * 86400000)
    stats.value = await fetchDetailedStats(props.token, {
      from: from.toISOString(),
      to: to.toISOString(),
      granularity: granularity.value,
      username: usernameFilter.value.trim() || undefined,
    })
    alerts.value = await fetchUsageAlerts(props.token)
    policy.value = await fetchPolicy(props.token)
    await renderCharts()
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : '통계 로딩 실패'
  } finally {
    loading.value = false
  }
}

const savePolicy = async () => {
  if (!policy.value) return
  saved.value = ''
  try {
    policy.value = await updatePolicy(props.token, policy.value)
    saved.value = '토큰·라우터 정책이 저장되었습니다'
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : '정책 저장 실패'
  }
}

const runAb = async () => {
  abLoading.value = true
  error.value = ''
  try {
    const tiers = abTiers.value.split(',').map((item) => item.trim()).filter(Boolean)
    abResult.value = await runAbTest(props.token, abPrompt.value, tiers)
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : 'A/B 테스트 실패'
  } finally {
    abLoading.value = false
  }
}

watch([granularity, rangeDays], () => {
  void load()
})

onMounted(() => {
  void load()
})

onBeforeUnmount(() => {
  destroyCharts()
})
</script>

<template>
  <section class="admin-dash">
    <header class="dash-head">
      <div>
        <h2>관리자 통계 · 정책</h2>
        <p>토큰 / 난이도(티어) / 요일 / 시간대 / 기간별 시계열</p>
      </div>
      <div class="filters">
        <select v-model="granularity">
          <option value="HOUR">HOUR</option>
          <option value="DAY">DAY</option>
          <option value="WEEK">WEEK</option>
        </select>
        <select v-model.number="rangeDays">
          <option :value="1">24h</option>
          <option :value="7">7일</option>
          <option :value="30">30일</option>
        </select>
        <input v-model="usernameFilter" placeholder="사용자 필터" @keyup.enter="load" />
        <button type="button" @click="load">조회</button>
      </div>
    </header>

    <p v-if="loading" class="hint">통계 불러오는 중…</p>
    <p v-if="error" class="error">{{ error }}</p>
    <p v-if="saved" class="ok">{{ saved }}</p>

    <div v-if="stats" class="kpi">
      <article><span>호출</span><strong>{{ stats.totalCalls }}</strong></article>
      <article><span>토큰</span><strong>{{ stats.totalTokens }}</strong></article>
      <article><span>비용</span><strong>${{ stats.totalCost.toFixed(4) }}</strong></article>
      <article><span>캐시 적중</span><strong>{{ stats.cacheHitRatio.toFixed(1) }}%</strong></article>
      <article><span>평균 지연</span><strong>{{ stats.avgLatencyMs.toFixed(0) }}ms</strong></article>
      <article><span>성공률</span><strong>{{ stats.successRate.toFixed(1) }}%</strong></article>
    </div>

    <div class="charts">
      <article class="wide"><h3>시계열 ({{ granularity }})</h3><div class="chart-box"><canvas ref="timelineCanvas"></canvas></div></article>
      <article><h3>난이도(티어)별 토큰</h3><div class="chart-box"><canvas ref="tierCanvas"></canvas></div></article>
      <article><h3>요청 유형별</h3><div class="chart-box"><canvas ref="typeCanvas"></canvas></div></article>
      <article><h3>Routing 방식</h3><div class="chart-box"><canvas ref="routingCanvas"></canvas></div></article>
      <article><h3>요일별</h3><div class="chart-box"><canvas ref="dowCanvas"></canvas></div></article>
      <article class="wide"><h3>시간대별 (0–23시)</h3><div class="chart-box"><canvas ref="hourCanvas"></canvas></div></article>
    </div>

    <div class="tables">
      <article>
        <h3>사용자별</h3>
        <ul>
          <li v-for="item in stats?.byUsername || []" :key="item.name">
            <b>{{ item.name }}</b>
            <span>{{ item.tokens }} tok · ${{ item.cost.toFixed(4) }}</span>
          </li>
        </ul>
      </article>
      <article>
        <h3>모델별 성능</h3>
        <ul>
          <li v-for="item in stats?.models || []" :key="item.model">
            <b>{{ item.tier }} / {{ item.model }}</b>
            <span>
              {{ item.calls }}회 · 성공 {{ item.successCount ?? '-' }}/실패 {{ item.failureCount ?? '-' }}
              · p95 {{ (item.p95LatencyMs ?? 0).toFixed(0) }}ms · avg {{ item.avgLatencyMs.toFixed(0) }}ms
              · tok {{ item.totalTokens }} · ${{ item.estimatedCost.toFixed(4) }}
              · fallback {{ item.fallbackCount ?? 0 }} · cache {{ (item.cacheHitRatio ?? 0).toFixed(1) }}%
            </span>
          </li>
        </ul>
      </article>
      <article>
        <h3>알림</h3>
        <ul>
          <li v-for="(item, idx) in alerts" :key="idx" :data-sev="item.severity">{{ item.message }}</li>
          <li v-if="alerts.length === 0">알림 없음</li>
        </ul>
      </article>
    </div>

    <article class="policy">
      <h3>A/B Model Test</h3>
      <div class="policy-grid">
        <label>Prompt<textarea v-model="abPrompt" rows="3" /></label>
        <label>비교 티어 (콤마)<input v-model="abTiers" placeholder="SOL,ASTRA" /></label>
      </div>
      <button type="button" :disabled="abLoading" @click="runAb">{{ abLoading ? '비교 중…' : 'A/B 실행' }}</button>
      <div v-if="abResult" class="ab-grid">
        <article v-for="item in abResult.variants" :key="item.tier">
          <h4>{{ item.tier }} · {{ item.model }}</h4>
          <p>{{ item.latencyMs }}ms · {{ item.totalTokens }} tok · ${{ item.estimatedCost.toFixed(4) }} · {{ item.success ? 'OK' : 'FAIL' }}</p>
          <pre>{{ item.response || item.error }}</pre>
        </article>
      </div>
    </article>

    <article v-if="policy" class="policy">
      <h3>토큰 · 라우터 정책</h3>
      <div class="policy-grid">
        <label>maxOutputTokens<input v-model.number="policy.maxOutputTokens" type="number" /></label>
        <label>요청당 한도<input v-model.number="policy.perRequestLimit" type="number" /></label>
        <label>일일 한도<input v-model.number="policy.dailyLimit" type="number" /></label>
        <label>주간 한도<input v-model.number="policy.weeklyLimit" type="number" /></label>
        <label>월간 한도<input v-model.number="policy.monthlyLimit" type="number" /></label>
        <label>일일 비용 $<input v-model.number="policy.dailyCostLimit" type="number" step="0.01" /></label>
        <label>월간 비용 $<input v-model.number="policy.monthlyCostLimit" type="number" step="0.01" /></label>
        <label>분당 Rate<input v-model.number="policy.rateLimitPerMinute" type="number" /></label>
        <label>Soft 비율<input v-model.number="policy.softRatio" type="number" step="0.01" /></label>
        <label>Warn 비율<input v-model.number="policy.warnRatio" type="number" step="0.01" /></label>
        <label>이상탐지 배수<input v-model.number="policy.anomalyMultiplier" type="number" step="0.1" /></label>
        <label>Confidence<input v-model.number="policy.confidenceThreshold" type="number" step="0.01" /></label>
        <label>기본 Plan
          <select v-model="policy.defaultPlan">
            <option value="FREE">FREE</option>
            <option value="PRO">PRO</option>
            <option value="ADMIN">ADMIN</option>
          </select>
        </label>
        <label>라우터 모드
          <select v-model="policy.routerMode">
            <option value="heuristic">heuristic</option>
            <option value="jev">jev</option>
          </select>
        </label>
        <label>최대 티어
          <select v-model="policy.maxTierCap">
            <option value="LUNA">LUNA</option>
            <option value="TERRA">TERRA</option>
            <option value="SOL">SOL</option>
            <option value="ASTRA">ASTRA</option>
          </select>
        </label>
        <label class="check"><input v-model="policy.cacheEnabled" type="checkbox" /> 응답 캐시</label>
      </div>
      <button type="button" @click="savePolicy">정책 저장</button>
    </article>
  </section>
</template>

<style scoped>
.admin-dash {
  display: grid;
  gap: 16px;
}

.dash-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  align-items: end;
}

.dash-head h2 {
  margin: 0;
  font-family: "Fraunces", "IBM Plex Serif", serif;
}

.dash-head p,
.hint {
  margin: 4px 0 0;
  color: #5b6b7c;
}

.filters {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.filters select,
.filters input,
.policy input,
.policy select {
  border: 1px solid #c9d5e1;
  border-radius: 10px;
  padding: 8px 10px;
}

.filters button,
.policy button {
  border: 0;
  border-radius: 999px;
  padding: 8px 14px;
  background: #1f5f8b;
  color: #fff;
  cursor: pointer;
}

.kpi {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 8px;
}

.kpi article {
  background: #f3f7fb;
  border-radius: 12px;
  padding: 10px;
  display: grid;
  gap: 4px;
}

.kpi span {
  font-size: 0.78rem;
  color: #667788;
}

.charts {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.charts article,
.tables article,
.policy {
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(31, 95, 139, 0.12);
  border-radius: 14px;
  padding: 12px;
}

.charts .wide {
  grid-column: 1 / -1;
}

.chart-box {
  height: 240px;
}

.tables {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.tables ul {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  gap: 8px;
  max-height: 220px;
  overflow: auto;
}

.tables li {
  padding: 8px;
  background: #f8fbfd;
  border-radius: 10px;
  font-size: 0.86rem;
}

.tables li span {
  display: block;
  color: #667788;
  margin-top: 4px;
}

.tables li[data-sev="HIGH"] {
  border-left: 3px solid #b42318;
}

.policy-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  margin: 12px 0;
}

.policy label {
  display: grid;
  gap: 4px;
  font-size: 0.86rem;
}

.policy .check {
  display: flex;
  align-items: center;
  gap: 8px;
}

.policy textarea {
  border: 1px solid #c9d5e1;
  border-radius: 10px;
  padding: 8px 10px;
  width: 100%;
}

.ab-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  margin-top: 12px;
}

.ab-grid article {
  background: #f8fbfd;
  border-radius: 10px;
  padding: 10px;
}

.ab-grid pre {
  white-space: pre-wrap;
  font-size: 0.82rem;
  max-height: 180px;
  overflow: auto;
}

.error { color: #b42318; }
.ok { color: #1f7a4d; }

@media (max-width: 980px) {
  .kpi { grid-template-columns: repeat(2, 1fr); }
  .charts, .tables, .policy-grid { grid-template-columns: 1fr; }
  .charts .wide { grid-column: auto; }
}
</style>
