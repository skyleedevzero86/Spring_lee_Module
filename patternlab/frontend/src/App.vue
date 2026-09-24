<script setup lang="ts">
import { computed, ref } from 'vue'
import { analyzeNumber } from './api/patternApi'
import { formatMatchLine, matchedLabel, type AnalyzeResponse } from './domain/analysis'

const value = ref('125.45')
const loading = ref(false)
const error = ref('')
const result = ref<AnalyzeResponse | null>(null)

const detectedTypeText = computed(() =>
  result.value ? `타입\n${result.value.detectedType}` : '타입\n-',
)

const runAnalyze = async () => {
  loading.value = true
  error.value = ''
  try {
    result.value = await analyzeNumber(value.value)
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : '분석에 실패했습니다'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="shell">
    <p class="eyebrow">JEP 532</p>
    <h1>Primitive Pattern Lab</h1>
    <p class="lead">
      switch와 instanceof의 primitive type pattern으로 숫자 값의 정확 변환 가능 여부를 확인합니다.
    </p>

    <section class="panel">
      <div class="field">
        <label for="value">값 입력</label>
        <input id="value" v-model="value" />
      </div>

      <pre class="type">{{ detectedTypeText }}</pre>

      <button type="button" :disabled="loading" @click="runAnalyze">
        {{ loading ? '분석 중…' : '분석' }}
      </button>

      <p v-if="error" class="error">{{ error }}</p>

      <div v-if="result" class="result">
        <div v-for="match in result.matches" :key="match.type" class="row">
          <span>{{ formatMatchLine(match) }}</span>
          <span class="badge" :class="match.matched ? 'on' : 'off'">
            {{ matchedLabel(match.matched) }}
          </span>
        </div>
      </div>
    </section>
  </main>
</template>
