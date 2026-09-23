<script setup lang="ts">
import { onMounted, ref } from 'vue'

interface StatusResponse {
  status: 'NOT_INITIALIZED' | 'INITIALIZED'
  statusLabel: string
}

interface AccessResponse {
  status: 'NOT_INITIALIZED' | 'INITIALIZED'
  statusLabel: string
  firstAccess: boolean
  loadTimeMillis: number
  config: {
    modelName: string
    provider: string
    temperature: number
    maxTokens: number
    endpoint: string
  }
}

const status = ref<StatusResponse | null>(null)
const access = ref<AccessResponse | null>(null)
const loading = ref(false)
const error = ref('')

async function refreshStatus() {
  const response = await fetch('/api/lazy-config/status')
  if (!response.ok) {
    throw new Error(`상태 조회 실패 (${response.status})`)
  }
  status.value = (await response.json()) as StatusResponse
}

async function useConfig() {
  loading.value = true
  error.value = ''
  try {
    const response = await fetch('/api/lazy-config/use', { method: 'POST' })
    if (!response.ok) {
      const body = (await response.json().catch(() => null)) as { message?: string } | null
      throw new Error(body?.message ?? `설정 사용 실패 (${response.status})`)
    }
    access.value = (await response.json()) as AccessResponse
    await refreshStatus()
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : '설정 사용에 실패했습니다'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  void refreshStatus().catch((cause) => {
    error.value = cause instanceof Error ? cause.message : '상태 조회에 실패했습니다'
  })
})
</script>

<template>
  <main class="shell">
    <p class="eyebrow">JEP 531</p>
    <h1>Lazy Constants</h1>
    <p class="lead">
      무거운 AI 모델 설정을 시작할 때 만들지 않고, 필요할 때 최초 한 번만 초기화합니다.
    </p>

    <section class="panel">
      <article class="card">
        <h2>AI Model Config</h2>
        <p class="status" :class="status?.status ?? 'NOT_INITIALIZED'">
          상태 : {{ status?.status ?? 'NOT_INITIALIZED' }}
        </p>
      </article>

      <div class="actions">
        <button type="button" :disabled="loading" @click="useConfig">
          {{ loading ? '로딩 중…' : access ? 'Config 다시 사용' : 'Config 사용' }}
        </button>
      </div>

      <p v-if="error" class="error">{{ error }}</p>

      <article v-if="access" class="result">
        <p>{{ access.firstAccess ? '초기화 완료' : '캐시된 설정 사용' }}</p>
        <p class="load">Load time : {{ access.loadTimeMillis.toLocaleString('ko-KR') }}ms</p>
        <p class="meta">
          {{ access.config.modelName }} · {{ access.config.provider }} · temp
          {{ access.config.temperature }}
        </p>
        <p class="meta">{{ access.config.endpoint }}</p>
      </article>
    </section>
  </main>
</template>
