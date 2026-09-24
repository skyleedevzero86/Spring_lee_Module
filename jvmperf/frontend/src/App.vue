<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { allocateMemoryLoad, fetchSnapshot } from './api/jvmApi'
import { barWidth, formatGcTime, type MemoryLoadResponse, type Snapshot } from './domain/monitor'

const snapshot = ref<Snapshot | null>(null)
const loadResult = ref<MemoryLoadResponse | null>(null)
const loading = ref(false)
const error = ref('')

const refresh = async () => {
  snapshot.value = await fetchSnapshot()
}

const runLoad = async () => {
  loading.value = true
  error.value = ''
  try {
    loadResult.value = await allocateMemoryLoad()
    snapshot.value = loadResult.value.snapshot
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : '메모리 부하 실행에 실패했습니다'
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  void refresh().catch((cause) => {
    error.value = cause instanceof Error ? cause.message : '스냅샷 조회에 실패했습니다'
  })
})
</script>

<template>
  <main class="shell">
    <p class="eyebrow">JVM Performance</p>
    <h1>JVM Monitor</h1>
    <p class="lead">힙·G1 GC·스레드를 관찰하고 100MB 할당으로 전후 변화를 비교합니다.</p>

    <section class="panel" v-if="snapshot">
      <article class="card">
        <h2>Heap</h2>
        <div class="track" :aria-label="`힙 ${snapshot.heap.usagePercent}퍼센트`">
          <span :style="{ width: barWidth(snapshot.heap.usagePercent) }"></span>
        </div>
        <p class="meta">{{ snapshot.heap.usedLabel }} / {{ snapshot.heap.maxLabel }}</p>
      </article>

      <article class="card">
        <h2>{{ snapshot.gc.name }}</h2>
        <div class="row"><span>Count</span><span>{{ snapshot.gc.count }}</span></div>
        <div class="row"><span>Time</span><span>{{ formatGcTime(snapshot.gc.timeMillis) }}</span></div>
      </article>

      <article class="card">
        <h2>Threads</h2>
        <div class="row"><span>Platform</span><span>{{ snapshot.threads.platform }}</span></div>
        <div class="row"><span>Virtual</span><span>{{ snapshot.threads.virtualLabel }}</span></div>
      </article>

      <button type="button" :disabled="loading" @click="runLoad">
        {{ loading ? '할당 중…' : '메모리 부하 발생' }}
      </button>

      <p v-if="error" class="error">{{ error }}</p>

      <article v-if="loadResult" class="compare">
        <h3>Before</h3>
        <div class="row"><span>Heap</span><span>{{ loadResult.before.heapLabel }}</span></div>
        <div class="row"><span>GC</span><span>{{ loadResult.before.gcCount }}</span></div>
        <p class="arrow">↓ {{ loadResult.allocatedMegabytes }}MB object allocation ↓</p>
        <h3>After</h3>
        <div class="row"><span>Heap</span><span>{{ loadResult.after.heapLabel }}</span></div>
        <div class="row"><span>GC</span><span>{{ loadResult.after.gcCount }}</span></div>
      </article>
    </section>
  </main>
</template>
