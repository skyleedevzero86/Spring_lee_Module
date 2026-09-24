export type PatternMatchView = Readonly<{
  type: string
  category: string
  matched: boolean
}>

export type AnalyzeResponse = Readonly<{
  value: string
  detectedType: string
  primaryCategory: string
  matches: readonly PatternMatchView[]
}>

export type AnalyzeRequest = Readonly<{
  value: string
}>

export const createAnalyzeRequest = (value: string): AnalyzeRequest => ({ value })

export const formatMatchLine = (match: PatternMatchView): string =>
  `${match.type.padEnd(8)} → ${match.category}`

export const matchedLabel = (matched: boolean): string => (matched ? '매칭' : '미매칭')
