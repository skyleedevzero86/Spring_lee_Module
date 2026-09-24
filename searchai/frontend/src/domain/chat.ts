export type Role = 'ADMIN' | 'USER'
export type ChatMode = 'DIRECT' | 'KNOWLEDGE_BASE' | 'INTERNET_SEARCH'

export type AuthSession = {
  token: string
  username: string
  role: Role
}

export type ChatMessage = {
  id: string
  role: 'user' | 'bot'
  html: string
}

export const canUseKnowledge = (role: Role) => role === 'ADMIN'
export const canUseInternetSearch = (role: Role) => role === 'ADMIN'
export const canUploadPdf = (role: Role) => role === 'ADMIN'

export const placeholderFor = (mode: ChatMode, role: Role) => {
  if (role === 'USER') return '자유롭게 대화해보세요...'
  if (mode === 'KNOWLEDGE_BASE') return '업로드된 지식 기반으로 질문하세요...'
  if (mode === 'INTERNET_SEARCH') return '인터넷 검색 후 답변해 드릴게요...'
  return '직접 대화하세요...'
}
