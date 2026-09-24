import type { IncidentResponse, StatsGranularity, StatsResponse, TriageRequest } from '../domain/incident'

const readError = async (response: Response): Promise<string> => {
  try {
    const body = await response.json()
    return body.message || body.detail || body.title || '요청에 실패했습니다'
  } catch {
    return '요청에 실패했습니다'
  }
}

const request = async <T>(url: string, init?: RequestInit): Promise<T> => {
  const response = await fetch(url, {
    headers: { 'Content-Type': 'application/json', ...(init?.headers ?? {}) },
    ...init,
  })
  if (!response.ok) {
    throw new Error(await readError(response))
  }
  if (response.status === 204) {
    return undefined as T
  }
  return response.json() as Promise<T>
}

export const triageIncident = (payload: TriageRequest): Promise<IncidentResponse> =>
  request<IncidentResponse>('/api/incidents/triage', {
    method: 'POST',
    body: JSON.stringify(payload),
  })

export const listIncidents = (limit = 30): Promise<IncidentResponse[]> =>
  request<IncidentResponse[]>(`/api/incidents?limit=${limit}`)

export const fetchStats = (params: {
  from: string
  to: string
  granularity: StatsGranularity
}): Promise<StatsResponse> => {
  const query = new URLSearchParams({
    from: params.from,
    to: params.to,
    granularity: params.granularity,
  })
  return request<StatsResponse>(`/api/stats?${query.toString()}`)
}

export const getMockAiMode = (): Promise<{ mode: string }> => request('/api/mock-ai/mode')

export const setMockAiMode = (mode: string): Promise<{ mode: string }> =>
  request(`/api/mock-ai/mode/${mode}`, { method: 'POST' })

export const triggerFault = (path: string, query = ''): Promise<unknown> =>
  request(`/api/faults/${path}${query}`, { method: 'POST' })
