<script setup lang="ts">
import { computed, ref } from 'vue'

type Mode = 'SEQUENTIAL' | 'STRUCTURED'

interface LookupResponse {
  customerId: number
  mode: Mode
  modeLabel: string
  profile: { name: string; grade: string }
  orders: Array<{ orderId: string; itemName: string; amount: number }>
  recommendations: Array<{ productId: string; title: string; score: number }>
  timings: {
    profileMillis: number
    ordersMillis: number
    recommendationsMillis: number
    sequentialEstimateMillis: number
  }
  totalElapsedMillis: number
}

const customerId = ref('1001')
const loading = ref<Mode | null>(null)
const error = ref('')
const sequentialResult = ref<LookupResponse | null>(null)
const structuredResult = ref<LookupResponse | null>(null)

const active = computed(() => structuredResult.value ?? sequentialResult.value)

const maxBar = computed(() => {
  const current = active.value
  if (!current) {
    return 1
  }
  return Math.max(
    current.timings.profileMillis,
    current.timings.ordersMillis,
    current.timings.recommendationsMillis,
    1,
  )
})

async function run(mode: Mode) {
  loading.value = mode
  error.value = ''
  try {
    const response = await fetch(`/api/customers/${customerId.value}/lookup?mode=${mode}`, {
      method: 'POST',
    })
    if (!response.ok) {
      const body = (await response.json().catch(() => null)) as { message?: string } | null
      throw new Error(body?.message ?? `요청 실패 (${response.status})`)
    }
    const payload = (await response.json()) as LookupResponse
    if (mode === 'SEQUENTIAL') {
      sequentialResult.value = payload
    } else {
      structuredResult.value = payload
    }
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : '조회에 실패했습니다'
  } finally {
    loading.value = null
  }
}

function width(value: number) {
  return `${Math.min(100, (value / maxBar.value) * 100)}%`
}

function formatMillis(value: number) {
  return `${value.toLocaleString('ko-KR')}ms`
}
</script>

<template>
  <main class="shell">
    <p class="eyebrow">Concurrent API Lab</p>
    <h1>고객 통합조회</h1>
    <p class="lead">
      서로 의존하지 않는 프로필·주문·추천 API를 순차 실행과 Structured Concurrency로 비교합니다.
    </p>

    <section class="panel">
      <div class="field">
        <label for="customerId">고객 ID</label>
        <input id="customerId" v-model="customerId" inputmode="numeric" />
      </div>

      <div class="actions">
        <button class="seq" :disabled="loading !== null" @click="run('SEQUENTIAL')">
          {{ loading === 'SEQUENTIAL' ? '순차 실행 중…' : 'Sequential 실행' }}
        </button>
        <button class="str" :disabled="loading !== null" @click="run('STRUCTURED')">
          {{ loading === 'STRUCTURED' ? '구조화 실행 중…' : 'Structured 실행' }}
        </button>
      </div>

      <p v-if="error" class="error">{{ error }}</p>

      <div v-if="active" class="grid">
        <article class="card">
          <h2>API 지연 시뮬레이션</h2>
          <div class="bars">
            <div class="bar-row">
              <span><b>프로필</b><em>{{ formatMillis(active.timings.profileMillis) }}</em></span>
              <div class="track"><i :style="{ width: width(active.timings.profileMillis) }"></i></div>
            </div>
            <div class="bar-row">
              <span><b>주문내역</b><em>{{ formatMillis(active.timings.ordersMillis) }}</em></span>
              <div class="track"><i :style="{ width: width(active.timings.ordersMillis) }"></i></div>
            </div>
            <div class="bar-row">
              <span><b>추천상품</b><em>{{ formatMillis(active.timings.recommendationsMillis) }}</em></span>
              <div class="track"><i :style="{ width: width(active.timings.recommendationsMillis) }"></i></div>
            </div>
          </div>
          <p class="total">총 처리시간 {{ formatMillis(active.totalElapsedMillis) }}</p>
          <p class="muted">{{ active.modeLabel }} · 고객 {{ active.customerId }}</p>
        </article>

        <article class="card">
          <h2>방식 비교</h2>
          <table class="table">
            <thead>
              <tr>
                <th>방식</th>
                <th>처리시간</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td>Sequential</td>
                <td>{{ sequentialResult ? formatMillis(sequentialResult.totalElapsedMillis) : '-' }}</td>
              </tr>
              <tr>
                <td>Structured</td>
                <td>{{ structuredResult ? formatMillis(structuredResult.totalElapsedMillis) : '-' }}</td>
              </tr>
            </tbody>
          </table>
          <p class="muted">
            순차 합계 추정 {{ active ? formatMillis(active.timings.sequentialEstimateMillis) : '-' }}
          </p>
        </article>
      </div>

      <div v-if="active" class="lists">
        <article>
          <h3>프로필</h3>
          <p>{{ active.profile.name }} · {{ active.profile.grade }}</p>
        </article>
        <article>
          <h3>주문내역</h3>
          <ul>
            <li v-for="order in active.orders" :key="order.orderId">
              {{ order.itemName }} ({{ order.amount.toLocaleString('ko-KR') }}원)
            </li>
          </ul>
        </article>
        <article>
          <h3>추천상품</h3>
          <ul>
            <li v-for="item in active.recommendations" :key="item.productId">
              {{ item.title }} · 점수 {{ item.score }}
            </li>
          </ul>
        </article>
      </div>
    </section>
  </main>
</template>
