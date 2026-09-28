export type Product = {
  id: number
  name: string
  keywords: string[]
  category?: string | null
  brand?: string | null
  color?: string | null
  price?: number
  stock?: number
  status?: string
  popularityScore?: number
  embeddingStatus?: string
  embeddingModel?: string | null
}

export type RecommendationItem = {
  id: number
  name: string
  keywords: string[]
  category?: string | null
  brand?: string | null
  color?: string | null
  price?: number
  stock?: number
  score: number
  reasons: string[]
}

export type RecommendResult = {
  userId: string
  condition: Record<string, unknown>
  items: RecommendationItem[]
}

export type RecommendationResponse = {
  productIds: number[]
  explanation: string
  evidences?: { productId: number; score: number; reasons: string[] }[]
}

export type ChatResult = {
  message: string
  recommendation: RecommendationResponse | null
  conversationId: string
}

const base = '/api/v1/products'
const CONVERSATION_KEY = 'product-recommend.conversationId'
const USER_KEY = 'product-recommend.userId'

function conversationHeaders(): HeadersInit {
  const headers: Record<string, string> = { 'Content-Type': 'application/json' }
  const existing = localStorage.getItem(CONVERSATION_KEY)
  if (existing) {
    headers['X-Conversation-Id'] = existing
  }
  return headers
}

function rememberConversation(conversationId?: string | null) {
  if (conversationId) {
    localStorage.setItem(CONVERSATION_KEY, conversationId)
  }
}

function currentUserId(): string {
  let userId = localStorage.getItem(USER_KEY)
  if (!userId) {
    userId = `user-${crypto.randomUUID()}`
    localStorage.setItem(USER_KEY, userId)
  }
  return userId
}

async function parse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const text = await response.text()
    throw new Error(text || `요청 실패 (${response.status})`)
  }
  if (response.status === 204) {
    return undefined as T
  }
  return response.json() as Promise<T>
}

export async function listProducts(): Promise<Product[]> {
  return parse(await fetch(base, { credentials: 'include' }))
}

export async function createProduct(body: {
  name: string
  keywords: string[]
  category?: string
  brand?: string
  color?: string
  price?: number
  stock?: number
}): Promise<Product> {
  return parse(
    await fetch(base, {
      method: 'POST',
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body)
    })
  )
}

export async function searchProducts(
  keywords: string[],
  k = 5,
  hybrid = false
): Promise<Product[]> {
  return parse(
    await fetch(`${base}/search`, {
      method: 'POST',
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ keywords, k, hybrid })
    })
  )
}

export async function recommendProducts(query: string, limit = 5): Promise<RecommendResult> {
  return parse(
    await fetch(`${base}/recommend`, {
      method: 'POST',
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ query, userId: currentUserId(), limit })
    })
  )
}

export async function trackEvent(body: {
  productId?: number
  eventType: string
  impressionId?: string
  position?: number
  query?: string
}): Promise<void> {
  await parse(
    await fetch(`${base}/events`, {
      method: 'POST',
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ ...body, userId: currentUserId() })
    })
  )
}

export async function findSimilar(id: number, k = 5): Promise<Product[]> {
  return parse(await fetch(`${base}/${id}/similar?k=${k}`, { credentials: 'include' }))
}

export async function retryEmbeddings(): Promise<{ queued: number }> {
  return parse(
    await fetch(`${base}/embeddings/retry`, {
      method: 'POST',
      credentials: 'include'
    })
  )
}

export async function reindexEmbeddings(): Promise<{ queued: number }> {
  return parse(
    await fetch(`${base}/embeddings/reindex`, {
      method: 'POST',
      credentials: 'include'
    })
  )
}

export async function chatRecommend(message: string): Promise<ChatResult> {
  const data = await parse<ChatResult>(
    await fetch(`${base}/chat`, {
      method: 'POST',
      credentials: 'include',
      headers: conversationHeaders(),
      body: JSON.stringify({ message })
    })
  )
  rememberConversation(data.conversationId)
  return data
}
