import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import {
  Chart,
  ArcElement,
  BarElement,
  CategoryScale,
  Legend,
  LinearScale,
  LineElement,
  PointElement,
  Title,
  Tooltip,
  Filler,
} from 'chart.js'
import {
  BarChartStrategy,
  ChartContext,
  DoughnutChartStrategy,
  LineChartStrategy,
  mapToSeries,
  timelineToSeries,
} from '../domain/chartStrategy'
import {
  buildFlow,
  formatRatio,
  rangePresets,
  type IncidentResponse,
  type IncidentType,
  type RangePreset,
  type Severity,
  type StatsGranularity,
  type StatsResponse,
  type TriageRequest,
} from '../domain/incident'
import {
  fetchStats,
  getMockAiMode,
  listIncidents,
  setMockAiMode,
  triageIncident,
  triggerFault,
} from '../api/opspilotApi'

Chart.register(
  CategoryScale,
  LinearScale,
  BarElement,
  LineElement,
  PointElement,
  ArcElement,
  Title,
  Tooltip,
  Legend,
  Filler,
)

const createEmptyForm = (): TriageRequest => ({
  title: 'DB 커넥션 풀 고갈',
  description: 'FATAL: connection timeout postgres pool exhaustion',
  type: 'DATABASE',
  severity: 'HIGH',
  source: 'order-service',
})

export const useOpsPilotDashboard = () => {
  const form = ref<TriageRequest>(createEmptyForm())
  const loading = ref(false)
  const statsLoading = ref(false)
  const error = ref('')
  const latest = ref<IncidentResponse | null>(null)
  const incidents = ref<IncidentResponse[]>([])
  const stats = ref<StatsResponse | null>(null)
  const mockMode = ref('NORMAL')
  const granularity = ref<StatsGranularity>('DAY')
  const rangePreset = ref<RangePreset>('7d')
  const faultMessage = ref('')

  const timelineCanvas = ref<HTMLCanvasElement | null>(null)
  const severityCanvas = ref<HTMLCanvasElement | null>(null)
  const categoryCanvas = ref<HTMLCanvasElement | null>(null)
  const teamCanvas = ref<HTMLCanvasElement | null>(null)

  let timelineChart: Chart | null = null
  let severityChart: Chart | null = null
  let categoryChart: Chart | null = null
  let teamChart: Chart | null = null

  const flow = computed(() => buildFlow(latest.value, loading.value))
  const cacheHitLabel = computed(() =>
    stats.value ? formatRatio(stats.value.cacheHitRatio) : '-',
  )

  const refreshIncidents = async () => {
    incidents.value = await listIncidents(40)
  }

  const refreshStats = async () => {
    statsLoading.value = true
    try {
      const range = rangePresets[rangePreset.value]()
      stats.value = await fetchStats({
        from: range.from,
        to: range.to,
        granularity: granularity.value,
      })
      renderCharts(stats.value)
    } finally {
      statsLoading.value = false
    }
  }

  const refreshMode = async () => {
    const result = await getMockAiMode()
    mockMode.value = result.mode
  }

  const renderCharts = (data: StatsResponse) => {
    if (timelineCanvas.value) {
      timelineChart?.destroy()
      const ctx = new ChartContext(new LineChartStrategy())
      timelineChart = new Chart(
        timelineCanvas.value,
        ctx.configure(timelineToSeries(data.timeline), `시간대별 장애 (${data.granularity})`),
      )
    }
    if (severityCanvas.value) {
      severityChart?.destroy()
      const ctx = new ChartContext(new DoughnutChartStrategy())
      severityChart = new Chart(
        severityCanvas.value,
        ctx.configure(mapToSeries(data.bySeverity), '심각도 분포'),
      )
    }
    if (categoryCanvas.value) {
      categoryChart?.destroy()
      const ctx = new ChartContext(new BarChartStrategy())
      categoryChart = new Chart(
        categoryCanvas.value,
        ctx.configure(mapToSeries(data.byCategory), '유형별 건수'),
      )
    }
    if (teamCanvas.value) {
      teamChart?.destroy()
      const ctx = new ChartContext(new BarChartStrategy())
      teamChart = new Chart(
        teamCanvas.value,
        ctx.configure(mapToSeries(data.byTeam), '담당 팀 분포'),
      )
    }
  }

  const runTriage = async () => {
    loading.value = true
    error.value = ''
    try {
      latest.value = await triageIncident(form.value)
      await Promise.all([refreshIncidents(), refreshStats()])
    } catch (cause) {
      error.value = cause instanceof Error ? cause.message : '분석에 실패했습니다'
    } finally {
      loading.value = false
    }
  }

  const changeMockMode = async (mode: string) => {
    const result = await setMockAiMode(mode)
    mockMode.value = result.mode
  }

  const runFault = async (path: string, query = '') => {
    faultMessage.value = ''
    try {
      await triggerFault(path, query)
      faultMessage.value = `${path} 시뮬레이션 완료`
    } catch (cause) {
      faultMessage.value = cause instanceof Error ? cause.message : '장애 시뮬레이션 실패'
    }
  }

  const setType = (type: IncidentType) => {
    form.value = { ...form.value, type }
  }

  const setSeverity = (severity: Severity) => {
    form.value = { ...form.value, severity }
  }

  watch([granularity, rangePreset], () => {
    void refreshStats().catch((cause) => {
      error.value = cause instanceof Error ? cause.message : '통계 조회에 실패했습니다'
    })
  })

  onMounted(() => {
    void Promise.all([refreshIncidents(), refreshStats(), refreshMode()]).catch((cause) => {
      error.value = cause instanceof Error ? cause.message : '초기 로딩에 실패했습니다'
    })
  })

  onUnmounted(() => {
    timelineChart?.destroy()
    severityChart?.destroy()
    categoryChart?.destroy()
    teamChart?.destroy()
  })

  return {
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
    refreshStats,
  }
}
