export type Gender = 'FEMALE' | 'MALE'
export type VoiceStyle = 'STANDARD' | 'NEURAL2'

export interface SpeechPayload {
  text: string
  filename: string
  language: string
  gender: Gender
  voiceStyle: VoiceStyle
  speakingRate: number
  pitch: number
}

export interface SavedAudio {
  filename: string
  size: number
  modifiedAt: string
}

export class TtsApiError extends Error {
  readonly code: string

  constructor(code: string, message: string) {
    super(message)
    this.code = code
  }
}

const VOICES: Record<string, Record<VoiceStyle, Record<Gender, string>>> = {
  'ko-KR': {
    NEURAL2: { FEMALE: 'ko-KR-Neural2-A', MALE: 'ko-KR-Neural2-C' },
    STANDARD: { FEMALE: 'ko-KR-Standard-A', MALE: 'ko-KR-Standard-C' },
  },
  'ja-JP': {
    NEURAL2: { FEMALE: 'ja-JP-Neural2-B', MALE: 'ja-JP-Neural2-C' },
    STANDARD: { FEMALE: 'ja-JP-Standard-A', MALE: 'ja-JP-Standard-C' },
  },
  'cmn-CN': {
    NEURAL2: { FEMALE: 'cmn-CN-Neural2-A', MALE: 'cmn-CN-Neural2-B' },
    STANDARD: { FEMALE: 'cmn-CN-Standard-A', MALE: 'cmn-CN-Standard-B' },
  },
  'en-US': {
    NEURAL2: { FEMALE: 'en-US-Neural2-C', MALE: 'en-US-Neural2-D' },
    STANDARD: { FEMALE: 'en-US-Standard-C', MALE: 'en-US-Standard-B' },
  },
}

export function voiceName(language: string, gender: Gender, style: VoiceStyle): string {
  return VOICES[language]?.[style]?.[gender] ?? ''
}

async function errorFrom(response: Response): Promise<TtsApiError> {
  try {
    const body = (await response.json()) as { code?: string; message?: string }
    return new TtsApiError(body.code ?? 'TTS_FAILED', body.message ?? response.statusText)
  } catch {
    return new TtsApiError('TTS_FAILED', response.statusText)
  }
}

export async function synthesize(payload: SpeechPayload): Promise<{ blob: Blob; filename: string }> {
  const response = await fetch('/api/tts', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })
  if (!response.ok) {
    throw await errorFrom(response)
  }
  const header = response.headers.get('X-Audio-Filename')
  const filename = header ? decodeURIComponent(header) : 'speech.mp3'
  return { blob: await response.blob(), filename }
}

export async function listFiles(): Promise<SavedAudio[]> {
  const response = await fetch('/api/tts/files')
  if (!response.ok) {
    throw await errorFrom(response)
  }
  return response.json() as Promise<SavedAudio[]>
}

export async function fetchFile(filename: string): Promise<Blob> {
  const response = await fetch(`/api/tts/files/${encodeURIComponent(filename)}`)
  if (!response.ok) {
    throw await errorFrom(response)
  }
  return response.blob()
}

export async function deleteFile(filename: string): Promise<void> {
  const response = await fetch(`/api/tts/files/${encodeURIComponent(filename)}`, { method: 'DELETE' })
  if (!response.ok) {
    throw await errorFrom(response)
  }
}
