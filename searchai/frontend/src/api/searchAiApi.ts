import type { AuthSession, ChatMode } from '../domain/chat'

const jsonHeaders = (token?: string): HeadersInit => ({
  'Content-Type': 'application/json',
  ...(token ? { Authorization: `Bearer ${token}` } : {}),
})

export const login = async (username: string, password: string): Promise<AuthSession> => {
  const response = await fetch('/auth/login', {
    method: 'POST',
    headers: jsonHeaders(),
    body: JSON.stringify({ username, password }),
  })
  if (!response.ok) {
    throw new Error('아이디 또는 비밀번호가 올바르지 않습니다')
  }
  return response.json()
}

export const sendChat = async (
  token: string,
  sessionId: string,
  message: string,
  mode: ChatMode,
  routingMode: 'AUTO' | 'MANUAL' = 'AUTO',
  forcedTier?: string,
) => {
  const response = await fetch('/chat/send', {
    method: 'POST',
    headers: jsonHeaders(token),
    body: JSON.stringify({
      currentUserName: sessionId,
      message,
      mode,
      routingMode,
      forcedTier,
    }),
  })
  if (response.status === 403) {
    throw new Error('권한이 없습니다. 일반 사용자는 채팅만 가능합니다.')
  }
  if (!response.ok) {
    throw new Error('메시지 전송에 실패했습니다')
  }
}

export const uploadPdf = async (token: string, file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  const response = await fetch('/rag/upload', {
    method: 'POST',
    headers: { Authorization: `Bearer ${token}` },
    body: formData,
  })
  const result = await response.json()
  if (!response.ok || result.status !== 200) {
    throw new Error(result.msg || '업로드에 실패했습니다')
  }
  return result.msg as string
}

export type ModelStat = {
  tier: string
  model: string
  calls: number
  totalTokens: number
  estimatedCost: number
  avgLatencyMs: number
  successRate: number
  successCount?: number
  failureCount?: number
  p95LatencyMs?: number
  avgPromptTokens?: number
  avgCompletionTokens?: number
  fallbackCount?: number
  cacheHitRatio?: number
}

export type UsageSummary = {
  todayTokens: number
  monthTokens: number
  todayCost: number
  monthCost: number
  dailyLimit: number
  monthlyLimit: number
  perRequestLimit: number
  softRatio: number
  budgetState: string
  models: ModelStat[]
}

export type UsageAlert = {
  code: string
  message: string
  severity: string
  username: string
  createdAtEpochMs: number
}

export type NamedMetric = { name: string; tokens: number; cost: number }

export type DetailedStats = {
  from: string
  to: string
  granularity: string
  totalCalls: number
  totalTokens: number
  totalCost: number
  cacheHits: number
  cacheHitRatio: number
  avgLatencyMs: number
  successRate: number
  byTier: NamedMetric[]
  byRequestType: NamedMetric[]
  byDayOfWeek: NamedMetric[]
  byHour: NamedMetric[]
  byUsername: NamedMetric[]
  byRoutingType: NamedMetric[]
  timeline: Array<{
    bucket: string
    calls: number
    tokens: number
    cost: number
    cacheHits: number
    avgLatencyMs: number
  }>
  models: ModelStat[]
}

export type RuntimePolicy = {
  maxOutputTokens: number
  perRequestLimit: number
  dailyLimit: number
  weeklyLimit: number
  monthlyLimit: number
  dailyCostLimit: number
  monthlyCostLimit: number
  softRatio: number
  warnRatio: number
  anomalyMultiplier: number
  rateLimitPerMinute: number
  routerMode: string
  confidenceThreshold: number
  maxTierCap: string
  cacheEnabled: boolean
  defaultPlan: string
}

export type AbTestResult = {
  prompt: string
  variants: Array<{
    tier: string
    model: string
    response: string
    latencyMs: number
    promptTokens: number
    completionTokens: number
    totalTokens: number
    estimatedCost: number
    success: boolean
    error: string
  }>
}


export const fetchUsageSummary = async (token: string): Promise<UsageSummary> => {
  const response = await fetch('/admin/usage/summary', {
    headers: { Authorization: `Bearer ${token}` },
  })
  if (!response.ok) throw new Error('사용량 조회에 실패했습니다')
  return response.json()
}

export const fetchUsageAlerts = async (token: string): Promise<UsageAlert[]> => {
  const response = await fetch('/admin/usage/alerts', {
    headers: { Authorization: `Bearer ${token}` },
  })
  if (!response.ok) throw new Error('알림 조회에 실패했습니다')
  return response.json()
}

export const fetchDetailedStats = async (
  token: string,
  params: { from?: string; to?: string; granularity: string; username?: string },
): Promise<DetailedStats> => {
  const query = new URLSearchParams()
  query.set('granularity', params.granularity)
  if (params.from) query.set('from', params.from)
  if (params.to) query.set('to', params.to)
  if (params.username) query.set('username', params.username)
  const response = await fetch(`/admin/stats?${query}`, {
    headers: { Authorization: `Bearer ${token}` },
  })
  if (!response.ok) throw new Error('통계 조회에 실패했습니다')
  return response.json()
}

export const fetchPolicy = async (token: string): Promise<RuntimePolicy> => {
  const response = await fetch('/admin/policy', {
    headers: { Authorization: `Bearer ${token}` },
  })
  if (!response.ok) throw new Error('정책 조회에 실패했습니다')
  return response.json()
}

export const updatePolicy = async (token: string, policy: RuntimePolicy): Promise<RuntimePolicy> => {
  const response = await fetch('/admin/policy', {
    method: 'PUT',
    headers: jsonHeaders(token),
    body: JSON.stringify(policy),
  })
  if (!response.ok) throw new Error('정책 저장에 실패했습니다')
  return response.json()
}

export const runAbTest = async (
  token: string,
  prompt: string,
  tiers: string[],
): Promise<AbTestResult> => {
  const response = await fetch('/admin/abtest', {
    method: 'POST',
    headers: jsonHeaders(token),
    body: JSON.stringify({ prompt, tiers }),
  })
  if (!response.ok) throw new Error('A/B 테스트에 실패했습니다')
  return response.json()
}

export const openSse = (token: string, sessionId: string) =>
  new EventSource(`/sse/connect?sessionId=${encodeURIComponent(sessionId)}&token=${encodeURIComponent(token)}`)
