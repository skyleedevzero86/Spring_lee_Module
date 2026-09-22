<script setup lang="ts">
import { reactive, ref } from 'vue'

interface ServiceConfig {
  delayMillis: number
  failure: boolean
}

interface TaskView {
  service: string
  displayName: string
  outcome: 'SUCCESS' | 'FAILED' | 'CANCELLED'
  outcomeLabel: string
  elapsedMillis: number
  failureReason: string | null
}

interface Report {
  tasks: TaskView[]
  totalElapsedMillis: number
  reason: string | null
}

const form = reactive({
  profile: { delayMillis: 500, failure: false } as ServiceConfig,
  orders: { delayMillis: 800, failure: true } as ServiceConfig,
  recommendation: { delayMillis: 3000, failure: false } as ServiceConfig,
  timeoutMillis: 1500,
})

const loading = ref(false)
const error = ref('')
const report = ref<Report | null>(null)

const services = [
  { key: 'profile' as const, title: 'Profile' },
  { key: 'orders' as const, title: 'Orders' },
  { key: 'recommendation' as const, title: 'Recommendation' },
]

async function run() {
  loading.value = true
  error.value = ''
  try {
    const response = await fetch('/api/simulations/run', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(form),
    })
    if (!response.ok) {
      const body = (await response.json().catch(() => null)) as { message?: string } | null
      throw new Error(body?.message ?? `요청 실패 (${response.status})`)
    }
    report.value = (await response.json()) as Report
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : '시뮬레이션 실행에 실패했습니다'
  } finally {
    loading.value = false
  }
}

function formatMillis(value: number) {
  return `${value.toLocaleString('ko-KR')} ms`
}
</script>

<template>
  <main class="shell">
    <p class="eyebrow">Failure / Timeout Lab</p>
    <h1>장애 시뮬레이션</h1>
    <p class="lead">
      Structured Concurrency로 하위 작업 실패·타임아웃 시 취소와 예외 전파를 확인합니다.
    </p>

    <section class="panel">
      <div class="services">
        <article v-for="item in services" :key="item.key" class="service">
          <h2>{{ item.title }}</h2>
          <div class="field">
            <label :for="`${item.key}-delay`">Delay (ms)</label>
            <input
              :id="`${item.key}-delay`"
              v-model.number="form[item.key].delayMillis"
              type="number"
              min="0"
            />
          </div>
          <button
            class="toggle"
            :class="form[item.key].failure ? 'on' : 'off'"
            type="button"
            @click="form[item.key].failure = !form[item.key].failure"
          >
            Failure: {{ form[item.key].failure ? 'ON' : 'OFF' }}
          </button>
        </article>
      </div>

      <div class="timeout">
        <label for="timeout">Timeout</label>
        <input id="timeout" v-model.number="form.timeoutMillis" type="number" min="1" />
      </div>

      <div class="actions">
        <button type="button" :disabled="loading" @click="run">
          {{ loading ? '실행 중…' : '실행' }}
        </button>
      </div>

      <p v-if="error" class="error">{{ error }}</p>

      <div v-if="report" class="results">
        <article class="card">
          <h2>결과</h2>
          <div v-for="task in report.tasks" :key="task.service" class="row">
            <span>{{ task.displayName }}</span>
            <span class="badge" :class="task.outcome">{{ task.outcome }}</span>
          </div>
          <p class="total">Total {{ formatMillis(report.totalElapsedMillis) }}</p>
        </article>

        <article class="card">
          <h2>Reason</h2>
          <p class="reason">{{ report.reason ?? '-' }}</p>
          <p class="muted">실패 시 나머지 작업은 CANCELLED로 종료됩니다.</p>
        </article>
      </div>
    </section>
  </main>
</template>
