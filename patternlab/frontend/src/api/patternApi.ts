import { createAnalyzeRequest, type AnalyzeResponse } from '../domain/analysis'

const readErrorMessage = async (response: Response): Promise<string> => {
  const body = (await response.json().catch(() => null)) as { message?: string } | null
  return body?.message ?? `요청 실패 (${response.status})`
}

export const analyzeNumber = async (value: string): Promise<AnalyzeResponse> => {
  const response = await fetch('/api/patterns/analyze', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(createAnalyzeRequest(value)),
  })

  if (!response.ok) {
    throw new Error(await readErrorMessage(response))
  }

  return (await response.json()) as AnalyzeResponse
}
