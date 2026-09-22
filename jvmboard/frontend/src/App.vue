<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'

interface SystemSnapshot {
  javaVersion: string
  jvm: string
  cpu: string
  heapUsed: string
  heapMax: string
  garbageCollector: string
  uptime: string
  heapUsedBytes: number
  heapMaxBytes: number
}

const snapshot = ref<SystemSnapshot | null>(null)
const error = ref('')
let timer = 0

const rows = computed(() => {
  if (!snapshot.value) {
    return []
  }
  const current = snapshot.value
  return [
    ['Java 버전', current.javaVersion],
    ['JVM', current.jvm],
    ['CPU', current.cpu],
    ['힙 사용량', current.heapUsed],
    ['힙 최대', current.heapMax],
    ['가비지 컬렉터', current.garbageCollector],
    ['가동 시간', current.uptime],
  ]
})

const heapPercent = computed(() => {
  if (!snapshot.value || snapshot.value.heapMaxBytes <= 0) {
    return 0
  }
  return Math.min(100, (snapshot.value.heapUsedBytes / snapshot.value.heapMaxBytes) * 100)
})

async function load() {
  try {
    const response = await fetch('/api/system')
    if (!response.ok) {
      throw new Error(`응답 상태 코드: ${response.status}`)
    }
    snapshot.value = (await response.json()) as SystemSnapshot
    error.value = ''
  } catch {
    error.value = '시스템 API(/api/system)에 연결하지 못했습니다.'
  }
}

onMounted(() => {
  void load()
  timer = window.setInterval(() => {
    void load()
  }, 2000)
})

onUnmounted(() => {
  window.clearInterval(timer)
})
</script>

<template>
  <main class="shell">
    <header class="heading">
      <div>
        <p class="eyebrow">런타임</p>
        <h1>대시보드</h1>
      </div>
      <p class="live"><i></i> 실시간</p>
    </header>

    <p v-if="error" class="error">{{ error }}</p>

    <section class="card" aria-live="polite">
      <dl>
        <div v-for="[label, value] in rows" :key="label" class="row">
          <dt>{{ label }}</dt>
          <dd>{{ value }}</dd>
        </div>
      </dl>
      <div v-if="snapshot" class="meter" :aria-label="`힙 사용률 ${heapPercent.toFixed(0)}퍼센트`">
        <span :style="{ width: `${heapPercent}%` }"></span>
      </div>
    </section>

    <footer class="foot">
      <span>시스템 API · /api/system</span>
      <span>2초</span>
    </footer>
  </main>
</template>
