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

const products = ref<Product[]>([])
const similar = ref<Product[]>([])
const searchHits = ref<Product[]>([])
const ranked = ref<RecommendationItem[]>([])
const recommendQuery = ref('10만원 이하 검정 러닝화 나이키 말고')
const chatReply = ref('')
const chatProductIds = ref<number[]>([])
const error = ref('')
const loading = ref(false)
const keywordQuery = ref('노트북, 게이밍, 고성능')
const useHybrid = ref(true)
const chatMessage = ref('가성비 러닝화 추천해줘')
const selectedId = ref<number | null>(null)

const form = reactive({
  name: '',
  keywords: '러닝, 운동화, 경량',
  category: 'RUNNING_SHOES',
  brand: 'ADIDAS',
  color: 'BLACK',
  price: 99000,
  stock: 10
})

const pendingCount = computed(
  () =>
    products.value.filter(
      (p) => p.embeddingStatus === 'PENDING' || p.embeddingStatus === 'FAILED' || !p.embeddingStatus
    ).length
)

async function refresh(showLoading = true) {
  if (showLoading) {
    loading.value = true
  }
  error.value = ''
  try {
    products.value = await listProducts()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '목록 조회 실패'
  } finally {
    if (showLoading) {
      loading.value = false
    }
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

async function waitForEmbeddings(maxAttempts = 10) {
  for (let i = 0; i < maxAttempts; i++) {
    await refresh(false)
    const pending = products.value.some(
      (p) => p.embeddingStatus === 'PENDING' || p.embeddingStatus === 'FAILED'
    )
    if (!pending) {
      return
    }
    await new Promise((resolve) => setTimeout(resolve, 500))
  }
}

async function recommend(id: number) {
  selectedId.value = id
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
      .split(',')
      .map((t) => t.trim())
      .filter(Boolean)
    searchHits.value = await searchProducts(keywords, 5, useHybrid.value)
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
}

async function runChat() {
  loading.value = true
  error.value = ''
  chatReply.value = ''
  chatProductIds.value = []
  try {
    const result = await chatRecommend(chatMessage.value)
    chatReply.value = result.message
    chatProductIds.value = result.recommendation?.productIds ?? []
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
    if (result.queued === 0) {
      error.value = '재시도할 임베딩이 없습니다'
    }
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
    if (result.queued === 0) {
      error.value = '재인덱싱 대상이 없습니다'
    }
  } catch (e) {
    error.value = e instanceof Error ? e.message : '재인덱싱 실패'
  } finally {
    loading.value = false
  }
}

onMounted(refresh)
</script>

<template>
  <main class="page">
    <header class="hero">
      <p class="brand">VectorShelf</p>
      <h1>추천 엔진 상품 검색</h1>
      <p class="lead">조건부 벡터검색 · 개인화 랭킹 · MMR · Evidence · Feedback Loop</p>
    </header>

    <p v-if="error" class="error">{{ error }}</p>
    <p v-if="loading" class="muted">처리 중…</p>

    <section class="panel">
      <h2>상품 등록</h2>
      <form class="form" @submit.prevent="submit">
        <input v-model="form.name" placeholder="상품명" required />
        <input v-model="form.keywords" placeholder="키워드" required />
        <input v-model="form.category" placeholder="카테고리" />
        <input v-model="form.brand" placeholder="브랜드" />
        <input v-model="form.color" placeholder="색상" />
        <input v-model.number="form.price" type="number" placeholder="가격" />
        <button type="submit">등록</button>
      </form>
    </section>

    <section class="panel">
      <h2>개인화 추천</h2>
      <div class="row">
        <input v-model="recommendQuery" placeholder="자연어 추천 질의" />
        <button type="button" @click="runRecommend">추천</button>
      </div>
      <ul class="cards">
        <li v-for="item in ranked" :key="'r-' + item.id">
          <div>
            <strong>{{ item.name }}</strong>
            <span>{{ item.brand }} · {{ item.category }} · {{ item.price?.toLocaleString() }}원</span>
            <small>score {{ item.score.toFixed(3) }} · {{ item.reasons.join(' / ') }}</small>
          </div>
          <div class="actions">
            <button type="button" class="ghost" @click="feedback(item, true)">관심</button>
            <button type="button" class="ghost" @click="feedback(item, false)">관심없음</button>
          </div>
        </li>
      </ul>
    </section>

    <section class="panel">
      <h2>키워드 검색</h2>
      <div class="row">
        <input v-model="keywordQuery" placeholder="키워드 (쉼표 구분)" />
        <label class="check">
          <input v-model="useHybrid" type="checkbox" />
          하이브리드(RRF)
        </label>
        <button type="button" @click="runSearch">검색</button>
      </div>
      <ul class="cards">
        <li v-for="hit in searchHits" :key="'s-' + hit.id">
          <strong>{{ hit.name }}</strong>
          <span>{{ hit.keywords.join(', ') }}</span>
        </li>
      </ul>
    </section>

    <section class="panel">
      <div class="panel-head">
        <h2>상품 목록</h2>
        <div class="actions">
          <small v-if="pendingCount">미완료 {{ pendingCount }}건</small>
          <button type="button" class="ghost" @click="runRetryEmbeddings">임베딩 재시도</button>
          <button type="button" class="ghost" @click="runReindexEmbeddings">모델 재인덱싱</button>
        </div>
      </div>
      <ul class="cards">
        <li v-for="product in products" :key="product.id">
          <div>
            <strong>{{ product.name }}</strong>
            <span>{{ product.brand }} · {{ product.category }} · {{ product.keywords.join(', ') }}</span>
            <small>
              {{ product.embeddingStatus ?? 'UNKNOWN' }}
              <template v-if="product.embeddingModel"> · {{ product.embeddingModel }}</template>
            </small>
          </div>
          <button type="button" @click="recommend(product.id)">유사 추천</button>
        </li>
      </ul>
    </section>

    <section class="panel" v-if="similar.length">
      <h2>유사 상품 <small v-if="selectedId">(ID {{ selectedId }})</small></h2>
      <ul class="cards">
        <li v-for="hit in similar" :key="'sim-' + hit.id">
          <strong>{{ hit.name }}</strong>
          <span>{{ hit.keywords.join(', ') }}</span>
        </li>
      </ul>
    </section>

    <section class="panel">
      <h2>AI 챗봇 추천</h2>
      <div class="row">
        <input v-model="chatMessage" placeholder="질문을 입력하세요" />
        <button type="button" @click="runChat">질문</button>
      </div>
      <p v-if="chatReply" class="chat">{{ chatReply }}</p>
      <ul v-if="chatProductIds.length" class="cards">
        <li v-for="id in chatProductIds" :key="'c-' + id">
          <strong>#{{ id }}</strong>
          <span>{{ products.find((p) => p.id === id)?.name ?? '추천 상품' }}</span>
        </li>
      </ul>
    </section>
  </main>
</template>

<style scoped>
.page {
  width: min(980px, calc(100% - 2rem));
  margin: 0 auto;
  padding: 2.5rem 0 4rem;
}
.hero { margin-bottom: 2rem; }
.brand {
  margin: 0;
  font-size: 2rem;
  font-weight: 800;
  letter-spacing: -0.04em;
}
h1 {
  margin: 0.35rem 0;
  font-size: 1.4rem;
  font-weight: 600;
}
.lead, .muted { color: #486581; }
.panel {
  margin-top: 1.25rem;
  padding: 1.25rem 0;
  border-top: 1px solid rgba(16, 42, 67, 0.12);
}
.panel-head {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  align-items: center;
  flex-wrap: wrap;
}
.panel-head h2 { margin: 0; }
.actions {
  display: flex;
  gap: 0.5rem;
  align-items: center;
  flex-wrap: wrap;
}
.form, .row {
  display: grid;
  gap: 0.75rem;
}
.form { grid-template-columns: repeat(3, 1fr) auto; }
.row {
  grid-template-columns: 1fr auto auto;
  align-items: center;
}
.check {
  display: inline-flex;
  gap: 0.35rem;
  align-items: center;
  white-space: nowrap;
  color: #486581;
  font-size: 0.9rem;
}
input, button {
  border: 1px solid #bcccdc;
  border-radius: 0.4rem;
  padding: 0.7rem 0.85rem;
  background: rgba(255, 255, 255, 0.8);
}
button {
  cursor: pointer;
  background: #102a43;
  color: #fff;
  border-color: #102a43;
}
button.ghost {
  background: transparent;
  color: #102a43;
}
.cards {
  list-style: none;
  padding: 0;
  margin: 1rem 0 0;
  display: grid;
  gap: 0.75rem;
}
.cards li {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  align-items: center;
  padding: 0.85rem 0;
  border-bottom: 1px solid rgba(16, 42, 67, 0.08);
}
.cards span, small {
  display: block;
  color: #627d98;
  margin: 0.25rem 0 0;
}
.chat {
  margin-top: 1rem;
  white-space: pre-wrap;
  color: #243b53;
}
.error {
  color: #9b1c1c;
  background: #ffe3e3;
  padding: 0.75rem 1rem;
  border-radius: 0.4rem;
}
</style>
