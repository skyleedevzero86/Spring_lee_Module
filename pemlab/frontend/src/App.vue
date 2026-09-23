<script setup lang="ts">
import { ref } from 'vue'

type Tab = 'inspect' | 'generate'

interface Inspection {
  type: string
  algorithm: string
  format: string
  size: string
}

interface GenerateResponse {
  publicKeyPem: string
  privateKeyPem: string
  publicKey: Inspection
}

const tab = ref<Tab>('inspect')
const pem = ref('')
const inspection = ref<Inspection | null>(null)
const generated = ref<GenerateResponse | null>(null)
const loading = ref(false)
const error = ref('')

async function analyze() {
  loading.value = true
  error.value = ''
  inspection.value = null
  try {
    const response = await fetch('/api/pem/analyze', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ pem: pem.value }),
    })
    if (!response.ok) {
      const body = (await response.json().catch(() => null)) as { message?: string } | null
      throw new Error(body?.message ?? `분석 실패 (${response.status})`)
    }
    inspection.value = (await response.json()) as Inspection
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : 'PEM 분석에 실패했습니다'
  } finally {
    loading.value = false
  }
}

async function generate() {
  loading.value = true
  error.value = ''
  generated.value = null
  try {
    const response = await fetch('/api/pem/generate', { method: 'POST' })
    if (!response.ok) {
      const body = (await response.json().catch(() => null)) as { message?: string } | null
      throw new Error(body?.message ?? `키 생성 실패 (${response.status})`)
    }
    generated.value = (await response.json()) as GenerateResponse
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : '키 생성에 실패했습니다'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="shell">
    <p class="eyebrow">JEP 538</p>
    <h1>PEM Inspector</h1>
    <p class="lead">
      Java 27 PEMEncoder / PEMDecoder로 공개키·개인키를 분석하고 RSA 키 쌍을 생성합니다.
    </p>

    <section class="panel">
      <div class="tabs">
        <button type="button" :class="{ active: tab === 'inspect' }" @click="tab = 'inspect'">
          분석
        </button>
        <button type="button" :class="{ active: tab === 'generate' }" @click="tab = 'generate'">
          키 생성
        </button>
      </div>

      <div v-if="tab === 'inspect'">
        <textarea
          v-model="pem"
          spellcheck="false"
          placeholder="-----BEGIN PUBLIC KEY-----&#10;...&#10;-----END PUBLIC KEY-----"
        />
        <div class="actions">
          <button type="button" :disabled="loading" @click="analyze">
            {{ loading ? '분석 중…' : '분석' }}
          </button>
        </div>

        <dl v-if="inspection" class="result">
          <div class="row"><dt>Type</dt><dd>{{ inspection.type }}</dd></div>
          <div class="row"><dt>Algorithm</dt><dd>{{ inspection.algorithm }}</dd></div>
          <div class="row"><dt>Format</dt><dd>{{ inspection.format }}</dd></div>
          <div class="row"><dt>Size</dt><dd>{{ inspection.size }}</dd></div>
        </dl>
      </div>

      <div v-else class="generate">
        <button type="button" :disabled="loading" @click="generate">
          {{ loading ? '생성 중…' : '키 생성' }}
        </button>

        <div v-if="generated" class="keys">
          <h3>Public Key</h3>
          <pre>{{ generated.publicKeyPem }}</pre>
          <h3>Private Key</h3>
          <pre>{{ generated.privateKeyPem }}</pre>
          <dl class="result">
            <div class="row"><dt>Type</dt><dd>{{ generated.publicKey.type }}</dd></div>
            <div class="row"><dt>Algorithm</dt><dd>{{ generated.publicKey.algorithm }}</dd></div>
            <div class="row"><dt>Format</dt><dd>{{ generated.publicKey.format }}</dd></div>
            <div class="row"><dt>Size</dt><dd>{{ generated.publicKey.size }}</dd></div>
          </dl>
        </div>
      </div>

      <p v-if="error" class="error">{{ error }}</p>
    </section>
  </main>
</template>
