export type Snapshot = Readonly<{
  heap: {
    usedBytes: number
    maxBytes: number
    usedLabel: string
    maxLabel: string
    usagePercent: number
  }
  gc: {
    name: string
    count: number
    timeMillis: number
  }
  threads: {
    platform: number
    virtualLabel: string
  }
}>

export type LoadCompare = Readonly<{
  heapLabel: string
  gcCount: number
}>

export type MemoryLoadResponse = Readonly<{
  before: LoadCompare
  after: LoadCompare
  allocatedMegabytes: number
  retainedChunks: number
  snapshot: Snapshot
}>

export const barWidth = (percent: number): string => `${Math.min(100, Math.max(0, percent))}%`

export const formatGcTime = (millis: number): string => `${millis.toLocaleString('ko-KR')} ms`
