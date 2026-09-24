export type IncidentType =
  | 'DATABASE'
  | 'REDIS'
  | 'EXTERNAL_API'
  | 'TIMEOUT'
  | 'CPU'
  | 'MEMORY'
  | 'HTTP'
  | 'UNKNOWN'

export type Severity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'
export type StatsGranularity = 'HOUR' | 'DAY' | 'WEEK'

export type TriageRequest = {
  title: string
  description: string
  type: IncidentType
  severity: Severity
  source: string
}

export type IncidentResponse = {
  id: string
  title: string
  description: string
  type: string
  severity: string
  status: string
  source: string
  fingerprint: string
  urgent: boolean
  urgencyProbability: number
  category: string
  analysisSeverity: string
  team: string
  summary: string
  recommendedAction: string
  analysisSource: string
  cacheHit: boolean
  model: string
  createdAt: string
  resolvedAt: string | null
  analyzedAt: string
}

export type StatsResponse = {
  from: string
  to: string
  granularity: string
  totalIncidents: number
  urgentCount: number
  cacheHitCount: number
  fallbackCount: number
  cacheHitRatio: number
  bySeverity: Record<string, number>
  byCategory: Record<string, number>
  bySource: Record<string, number>
  byTeam: Record<string, number>
  timeline: Array<{
    bucket: string
    count: number
    urgentCount: number
    cacheHits: number
  }>
}

export type FlowStep = {
  id: string
  label: string
  detail: string
  active: boolean
  done: boolean
}

export const percent = (value: number): string => `${(value * 100).toFixed(1)}%`

export const formatRatio = (value: number): string => percent(Number.isFinite(value) ? value : 0)

export const entriesSorted = (map: Record<string, number>): Array<[string, number]> =>
  Object.entries(map).sort((a, b) => b[1] - a[1])

export const sumValues = (map: Record<string, number>): number =>
  Object.values(map).reduce((acc, n) => acc + n, 0)

export const buildFlow = (incident: IncidentResponse | null, loading: boolean): FlowStep[] => {
  const base: FlowStep[] = [
    { id: 'api', label: 'API 수신', detail: '장애 입력', active: false, done: false },
    { id: 'fingerprint', label: 'Fingerprint', detail: 'SHA-256 지문', active: false, done: false },
    { id: 'cache', label: 'Cache 조회', detail: 'Redis/Memory', active: false, done: false },
    { id: 'ai', label: 'AI 분석', detail: 'Jev / Mock', active: false, done: false },
    { id: 'db', label: 'PostgreSQL', detail: '분석 저장', active: false, done: false },
    { id: 'notify', label: '알림', detail: '로그 채널', active: false, done: false },
  ]

  if (loading) {
    return base.map((step, index) => ({
      ...step,
      active: index <= 2,
      done: index < 2,
    }))
  }

  if (!incident) {
    return base
  }

  const cacheDetail = incident.cacheHit ? '캐시 적중' : '캐시 미스 → AI 호출'
  const aiDetail = `${incident.analysisSource} / ${incident.model}`

  return [
    { id: 'api', label: 'API 수신', detail: incident.source, active: false, done: true },
    { id: 'fingerprint', label: 'Fingerprint', detail: incident.fingerprint.slice(0, 12) + '…', active: false, done: true },
    { id: 'cache', label: 'Cache 조회', detail: cacheDetail, active: false, done: true },
    { id: 'ai', label: 'AI 분석', detail: aiDetail, active: false, done: true },
    { id: 'db', label: 'PostgreSQL', detail: incident.status, active: false, done: true },
    { id: 'notify', label: '알림', detail: incident.urgent ? '긴급 알림' : '일반 알림', active: false, done: true },
  ]
}

export const rangePresets = {
  '24h': () => ({ from: hoursAgo(24), to: nowIso() }),
  '7d': () => ({ from: hoursAgo(24 * 7), to: nowIso() }),
  '30d': () => ({ from: hoursAgo(24 * 30), to: nowIso() }),
} as const

export type RangePreset = keyof typeof rangePresets

const nowIso = (): string => new Date().toISOString()

const hoursAgo = (hours: number): string =>
  new Date(Date.now() - hours * 60 * 60 * 1000).toISOString()
