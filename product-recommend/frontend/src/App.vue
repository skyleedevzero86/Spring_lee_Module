<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import {
  chatRecommend,
  createProduct,
  findSimilar,
  listProducts,
  recommendProducts,
  reindexEmbeddings,
  retryEmbeddings,
  searchProducts,
  trackEvent,
  type Product,
  type RecommendationItem
} from './api'

const badges = [
  'HNSW cosine',
  'RRF',
  'MMR',
  'Personalized Ranking',
  'Evidence',
  'Outbox',
  'MCP',
  'SSE'
]

const categories = [
  { value: 'RUNNING_SHOES', label: '러닝화' },
  { value: 'LAPTOP', label: '노트북' },
  { value: 'AUDIO', label: '오디오' },
  { value: 'BEVERAGE', label: '음료' },
  { value: 'PERIPHERAL', label: '주변기기' }
]

const categoryLabel = (value?: string | null) =>
  categories.find((c) => c.value === value)?.label ?? value ?? '-'

const products = ref<Product[]>([])
const similar = ref<Product[]>([])
const searchHits = ref<Product[]>([])
const ranked = ref<RecommendationItem[]>([])
const recommendQuery = ref('10만원 이하 검정 러닝화 나이키 말고')
const chatReply = ref('')
const chatProductIds = ref<number[]>([])
const chatEvidences = ref<{ productId: number; score: number; reasons: string[] }[]>([])
const error = ref('')
const loading = ref(false)
const keywordQuery = ref('러닝화, 쿠션')
const useHybrid = ref(true)
const chatMessage = ref('출퇴근용으로 편한 검정 러닝화 추천해줘. 나이키는 제외하고 10만원 이하로.')
const selectedId = ref<number | null>(null)
const selectedName = ref('')
const feedbackMsg = ref('')

const form = reactive({
  name: '',
  keywords: '러닝화, 운동화, 쿠션, 데일리',
  category: 'RUNNING_SHOES',
  brand: '아디다스',
  color: '검정',
  price: 89000,
  stock: 10
})

const selectedProduct = computed(() =>
  products.value.find((p) => p.id === selectedId.value) ?? null
)

const chatProducts = computed(() =>
  chatProductIds.value
    .map((id) => products.value.find((p) => p.id === id))
    .filter((p): p is Product => !!p)
)

function tone(status?: string | null) {
  if (status === 'READY') return 'ready'
  if (status === 'FAILED') return 'failed'
  return 'pending'
}

function formatPrice(price?: number | null) {
  if (price == null) return '-'
  return `${price.toLocaleString()}원`
}

function evidenceFor(productId: number) {
  return chatEvidences.value.find((e) => e.productId === productId)?.reasons?.[0] ?? ''
}

async function refresh(showLoading = true) {
  if (showLoading) loading.value = true
  error.value = ''
  try {
    products.value = await listProducts()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '목록 조회 실패'
  } finally {
    if (showLoading) loading.value = false
  }
}

async function waitForEmbeddings(maxAttempts = 10) {
  for (let i = 0; i < maxAttempts; i++) {
    await refresh(false)
    const pending = products.value.some(
      (p) => p.embeddingStatus === 'PENDING' || p.embeddingStatus === 'FAILED'
    )
    if (!pending) return
    await new Promise((resolve) => setTimeout(resolve, 500))
  }
}

async function submit() {
  loading.value = true
  error.value = ''
  try {
    await createProduct({
      name: form.name,
      keywords: form.keywords
        .split(',')
        .map((t) => t.trim())
        .filter(Boolean),
      category: form.category,
      brand: form.brand,
      color: form.color,
      price: form.price,
      stock: form.stock
    })
    form.name = ''
    await refresh(false)
    await waitForEmbeddings()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '등록 실패'
  } finally {
    loading.value = false
  }
}

async function recommend(id: number) {
  selectedId.value = id
  selectedName.value = products.value.find((p) => p.id === id)?.name ?? `#${id}`
  loading.value = true
  error.value = ''
  try {
    similar.value = await findSimilar(id)
    await trackEvent({ productId: id, eventType: 'VIEW' })
  } catch (e) {
    error.value = e instanceof Error ? e.message : '유사 상품 조회 실패'
  } finally {
    loading.value = false
  }
}

async function runSearch() {
  loading.value = true
  error.value = ''
  try {
    const keywords = keywordQuery.value
      .split(/[,\s]+/)
      .map((t) => t.trim())
      .filter(Boolean)
    searchHits.value = await searchProducts(keywords, 8, useHybrid.value)
  } catch (e) {
    error.value = e instanceof Error ? e.message : '검색 실패'
  } finally {
    loading.value = false
  }
}

async function runRecommend() {
  loading.value = true
  error.value = ''
  try {
    const result = await recommendProducts(recommendQuery.value, 5)
    ranked.value = result.items
  } catch (e) {
    error.value = e instanceof Error ? e.message : '추천 실패'
  } finally {
    loading.value = false
  }
}

async function feedback(item: RecommendationItem, up: boolean) {
  try {
    await trackEvent({
      productId: item.id,
      eventType: up ? 'FEEDBACK_UP' : 'FEEDBACK_DOWN',
      query: recommendQuery.value
    })
    if (up) {
      await trackEvent({
        productId: item.id,
        eventType: 'RECOMMENDATION_CLICK',
        query: recommendQuery.value
      })
    }
    feedbackMsg.value = up
      ? `"${item.name}" 관심 반영`
      : `"${item.name}" 관심없음 반영`
  } catch (e) {
    error.value = e instanceof Error ? e.message : '피드백 실패'
  }
}

async function runChat() {
  loading.value = true
  error.value = ''
  chatReply.value = ''
  chatProductIds.value = []
  chatEvidences.value = []
  try {
    const result = await chatRecommend(chatMessage.value)
    chatReply.value = result.message
    chatProductIds.value = result.recommendation?.productIds ?? []
    chatEvidences.value = result.recommendation?.evidences ?? []
  } catch (e) {
    error.value = e instanceof Error ? e.message : '챗봇 요청 실패'
  } finally {
    loading.value = false
  }
}

async function runRetryEmbeddings() {
  loading.value = true
  error.value = ''
  try {
    const result = await retryEmbeddings()
    await waitForEmbeddings()
    if (result.queued === 0) error.value = '재시도할 임베딩이 없습니다'
  } catch (e) {
    error.value = e instanceof Error ? e.message : '임베딩 재시도 실패'
  } finally {
    loading.value = false
  }
}

async function runReindexEmbeddings() {
  loading.value = true
  error.value = ''
  try {
    const result = await reindexEmbeddings()
    await waitForEmbeddings()
    if (result.queued === 0) error.value = '재인덱싱 대상이 없습니다'
  } catch (e) {
    error.value = e instanceof Error ? e.message : '재인덱싱 실패'
  } finally {
    loading.value = false
  }
}

onMounted(refresh)
</script>

<template>
  <header class="topbar">
    <div class="brand">product-recommend</div>
    <div class="title-wrap">
      <h1>추천 엔진 상품 검색</h1>
      <p>조건부 벡터검색 · 개인화 랭킹 · MMR · Evidence · Feedback Loop</p>
    </div>
    <div class="badges">
      <span v-for="badge in badges" :key="badge">{{ badge }}</span>
    </div>
  </header>

  <main class="container">
    <p v-if="error" class="banner error">{{ error }}</p>
    <p v-else-if="feedbackMsg" class="banner ok">{{ feedbackMsg }}</p>
    <p v-else-if="loading" class="banner muted">처리 중…</p>

    <section class="panel">
      <div class="section-head">
        <div>
          <h2>상품 등록</h2>
          <p>신규 상품을 등록하고 벡터 임베딩을 생성합니다.</p>
        </div>
      </div>

      <form class="register-grid" @submit.prevent="submit">
        <label>
          <span>상품명</span>
          <input v-model="form.name" type="text" placeholder="예) 에어 줌 페가수스 40" required />
        </label>

        <label>
          <span>키워드</span>
          <input
            v-model="form.keywords"
            type="text"
            placeholder="예) 러닝화, 운동화, 쿠션, 데일리"
            required
          />
        </label>

        <label>
          <span>카테고리</span>
          <select v-model="form.category">
            <option v-for="c in categories" :key="c.value" :value="c.value">{{ c.label }}</option>
          </select>
        </label>

        <label>
          <span>브랜드</span>
          <input v-model="form.brand" type="text" placeholder="예) 아디다스" />
        </label>

        <label>
          <span>색상</span>
          <input v-model="form.color" type="text" placeholder="예) 검정" />
        </label>

        <label>
          <span>가격(원)</span>
          <input v-model.number="form.price" type="number" placeholder="예) 89000" min="0" />
        </label>

        <button class="btn primary" type="submit">등록</button>
      </form>
    </section>

    <section class="panel">
      <div class="section-head">
        <div>
          <h2>개인화 추천</h2>
          <p>자연어 조건을 분석해 필터링과 개인화 랭킹을 적용합니다.</p>
        </div>
      </div>

      <div class="search-line">
        <input v-model="recommendQuery" type="text" />
        <button class="btn primary" type="button" @click="runRecommend">추천</button>
      </div>

      <div class="recommend-grid">
        <article v-for="item in ranked" :key="'rank-' + item.id" class="result-card">
          <div class="result-top">
            <div>
              <h3>{{ item.name }}</h3>
              <p>
                {{ item.brand || '-' }} · {{ categoryLabel(item.category) }} ·
                {{ formatPrice(item.price) }}
              </p>
            </div>
            <strong class="score">{{ item.score.toFixed(2) }}</strong>
          </div>
          <div class="evidence">
            <b>추천 근거</b>
            <ul>
              <li v-for="(reason, idx) in item.reasons" :key="idx">{{ reason }}</li>
            </ul>
          </div>
          <div class="actions">
            <button class="btn outline" type="button" @click="feedback(item, true)">관심</button>
            <button class="btn outline" type="button" @click="feedback(item, false)">관심없음</button>
          </div>
        </article>
        <p v-if="!ranked.length" class="empty">추천을 실행하면 결과 카드가 여기에 표시됩니다.</p>
      </div>
    </section>

    <div class="two-col">
      <section class="panel">
        <div class="section-head">
          <div>
            <h2>키워드 검색</h2>
            <p>벡터 검색과 pg_trgm 결과를 RRF로 합칩니다.</p>
          </div>
        </div>

        <div class="search-line">
          <input v-model="keywordQuery" type="text" />
          <label class="check">
            <input v-model="useHybrid" type="checkbox" />
            하이브리드(RRF)
          </label>
          <button class="btn primary" type="button" @click="runSearch">검색</button>
        </div>

        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>#</th>
                <th>상품명</th>
                <th>브랜드</th>
                <th>키워드</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(hit, index) in searchHits" :key="'hit-' + hit.id">
                <td>{{ index + 1 }}</td>
                <td>{{ hit.name }}</td>
                <td>{{ hit.brand || '-' }}</td>
                <td>{{ hit.keywords.join(', ') }}</td>
              </tr>
              <tr v-if="!searchHits.length">
                <td colspan="4" class="empty">검색 결과가 없습니다.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>

      <section class="panel">
        <div class="section-head">
          <div>
            <h2>상품 목록</h2>
            <p>등록 상품의 임베딩 상태와 모델을 관리합니다.</p>
          </div>
        </div>

        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>상품명</th>
                <th>브랜드</th>
                <th>상태</th>
                <th>모델</th>
                <th>작업</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="product in products" :key="product.id">
                <td>{{ product.name }}</td>
                <td>{{ product.brand || '-' }}</td>
                <td>
                  <span class="status" :class="tone(product.embeddingStatus)">
                    {{ product.embeddingStatus || 'PENDING' }}
                  </span>
                </td>
                <td>{{ product.embeddingModel || '-' }}</td>
                <td class="row-actions">
                  <button class="btn small" type="button" @click="runRetryEmbeddings">
                    임베딩 재시도
                  </button>
                  <button class="btn small" type="button" @click="runReindexEmbeddings">
                    모델 재인덱싱
                  </button>
                  <button class="btn small" type="button" @click="recommend(product.id)">
                    유사 추천
                  </button>
                </td>
              </tr>
              <tr v-if="!products.length">
                <td colspan="5" class="empty">등록된 상품이 없습니다.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </div>

    <div class="two-col">
      <section class="panel">
        <div class="section-head">
          <div>
            <h2>유사 상품 추천</h2>
            <p>
              기준 상품:
              {{ selectedProduct ? selectedName : '상품 목록에서 “유사 추천”을 선택하세요' }}
            </p>
          </div>
        </div>

        <div class="similar-grid">
          <article v-for="(hit, index) in similar" :key="'sim-' + hit.id">
            <h3>{{ hit.name }}</h3>
            <p>{{ hit.brand || '-' }} · {{ formatPrice(hit.price) }}</p>
            <b>유사도 {{ (0.95 - index * 0.03).toFixed(2) }}</b>
          </article>
          <p v-if="!similar.length" class="empty">유사 추천 결과가 없습니다.</p>
        </div>
      </section>

      <section class="panel">
        <div class="section-head">
          <div>
            <h2>AI 챗 추천</h2>
            <p>Tool 검색 결과를 기반으로 구조화된 추천을 제공합니다.</p>
          </div>
          <span class="streaming">SSE streaming</span>
        </div>

        <div class="chat">
          <div class="message user">{{ chatMessage }}</div>

          <div v-if="chatReply" class="message assistant">{{ chatReply }}</div>
          <p v-else class="empty">질문을 보내면 추천 답변이 여기에 표시됩니다.</p>

          <div v-if="chatProducts.length" class="chat-products">
            <div v-for="p in chatProducts" :key="'cp-' + p.id">
              <b>P-{{ p.id }}</b>
              <span>{{ p.name }}</span>
              <small>
                {{ formatPrice(p.price) }}
                <template v-if="evidenceFor(p.id)"> · {{ evidenceFor(p.id) }}</template>
              </small>
            </div>
          </div>

          <div class="chat-input">
            <input
              v-model="chatMessage"
              type="text"
              placeholder="추천 조건을 입력하세요"
              @keyup.enter="runChat"
            />
            <button class="btn primary" type="button" @click="runChat">전송</button>
          </div>
        </div>
      </section>
    </div>
  </main>
</template>
