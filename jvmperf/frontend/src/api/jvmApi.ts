import type { MemoryLoadResponse, Snapshot } from '../domain/monitor'

const readError = async (response: Response): Promise<string> => {
  const body = (await response.json().catch(() => null)) as { message?: string } | null
  return body?.message ?? `요청 실패 (${response.status})`
}

export const fetchSnapshot = async (): Promise<Snapshot> => {
  const response = await fetch('/api/jvm/snapshot')
  if (!response.ok) {
    throw new Error(await readError(response))
  }
  return (await response.json()) as Snapshot
}

export const allocateMemoryLoad = async (): Promise<MemoryLoadResponse> => {
  const response = await fetch('/api/jvm/load', { method: 'POST' })
  if (!response.ok) {
    throw new Error(await readError(response))
  }
  return (await response.json()) as MemoryLoadResponse
}
