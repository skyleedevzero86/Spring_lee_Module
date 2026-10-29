<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import {
  deleteFile,
  fetchFile,
  listFiles,
  synthesize,
  TtsApiError,
  type Gender,
  type SavedAudio,
  type VoiceStyle,
} from './api'
import { LOCALES, TTS_LANGUAGE, type LocaleId } from './i18n'

const LOCALE_LABEL: Record<LocaleId, string> = {
  ko: '한국어',
  ja: '日本語',
  zh: '中文',
  en: 'English',
}

const SPEECH_LANGUAGES = [
  { code: 'ko-KR', label: '한국어' },
  { code: 'ja-JP', label: '日本語' },
  { code: 'cmn-CN', label: '中文' },
  { code: 'en-US', label: 'English' },
]

const { t, locale } = useI18n()

const text = ref(String(t('sample')))
const filename = ref('speech')
const speechLanguage = ref(TTS_LANGUAGE[currentLocale()])
const gender = ref<Gender>('FEMALE')
const voiceStyle = ref<VoiceStyle>('NEURAL2')
const speakingRate = ref(1)
const pitch = ref(0)
const loading = ref(false)
const errorCode = ref<string | null>(null)
const errorDetail = ref('')
const savedFiles = ref<SavedAudio[]>([])
const listFailed = ref(false)
const audioUrl = ref<string | null>(null)
const resultName = ref('')
const player = ref<HTMLAudioElement | null>(null)

const localeItems = LOCALES.map((id) => ({ id, label: LOCALE_LABEL[id] }))
const bytes = computed(() => new TextEncoder().encode(text.value).length)
const tooLong = computed(() => bytes.value > 5000)
const languageLabel = computed(
  () => SPEECH_LANGUAGES.find((item) => item.code === speechLanguage.value)?.label ?? speechLanguage.value,
)
const genderLabel = computed(() => (gender.value === 'FEMALE' ? String(t('female')) : String(t('male'))))
const styleLabel = computed(() => (voiceStyle.value === 'NEURAL2' ? String(t('neural')) : String(t('standard'))))
const localeLabel = computed(() => LOCALE_LABEL[currentLocale()])
const rateLabel = computed(() => `${speakingRate.value.toFixed(2)}×`)
const pitchLabel = computed(() => `${pitch.value > 0 ? '+' : ''}${pitch.value.toFixed(1)}`)
const errorText = computed(() => {
  if (!errorCode.value) {
    return ''
  }
  const key = `error.${errorCode.value}`
  const translated = String(t(key))
  return translated === key ? String(t('error.TTS_FAILED')) : translated
})
const showDetail = computed(() =>
  errorDetail.value !== '' &&
  (errorCode.value === 'TTS_FAILED' || errorCode.value === 'CREDENTIALS_MISSING' || errorCode.value === 'NETWORK'),
)

watch(locale, () => {
  document.title = String(t('title'))
  document.documentElement.lang = locale.value === 'zh' ? 'zh-Hans' : locale.value
}, { immediate: true })

onMounted(() => {
  void refresh()
})

onBeforeUnmount(() => {
  if (audioUrl.value) {
    URL.revokeObjectURL(audioUrl.value)
  }
})

function currentLocale(): LocaleId {
  const value = locale.value
  if (value === 'ja' || value === 'zh' || value === 'en' || value === 'ko') {
    return value
  }
  return 'ko'
}

function setLocale(next: LocaleId) {
  const previousSample = String(t('sample'))
  locale.value = next
  localStorage.setItem('tts-locale', next)
  speechLanguage.value = TTS_LANGUAGE[next]
  if (!text.value.trim() || text.value === previousSample) {
    text.value = String(t('sample'))
  }
}

function formatSize(size: number): string {
  if (size < 1024) {
    return `${size} B`
  }
  return `${(size / 1024).toFixed(1)} KB`
}

function formatTime(value: string): string {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }
  const tag = locale.value === 'zh' ? 'zh-CN' : locale.value
  return date.toLocaleString(tag)
}

async function refresh() {
  try {
    savedFiles.value = await listFiles()
    listFailed.value = false
  } catch {
    listFailed.value = true
  }
}

function playBlob(blob: Blob) {
  const next = URL.createObjectURL(blob)
  const previous = audioUrl.value
  audioUrl.value = next
  if (previous) {
    URL.revokeObjectURL(previous)
  }
  void nextTick(() => {
    void player.value?.play().catch(() => undefined)
  })
}

function showError(error: unknown) {
  if (error instanceof TtsApiError) {
    errorCode.value = error.code
    errorDetail.value = error.message
    return
  }
  errorCode.value = 'NETWORK'
  errorDetail.value = error instanceof Error ? error.message : ''
}

async function generate() {
  errorCode.value = null
  errorDetail.value = ''
  if (!text.value.trim() || tooLong.value) {
    errorCode.value = 'INVALID_TEXT'
    return
  }
  loading.value = true
  try {
    const result = await synthesize({
      text: text.value.trim(),
      filename: filename.value,
      language: speechLanguage.value,
      gender: gender.value,
      voiceStyle: voiceStyle.value,
      speakingRate: speakingRate.value,
      pitch: pitch.value,
    })
    resultName.value = result.filename
    playBlob(result.blob)
    await refresh()
  } catch (error) {
    showError(error)
  } finally {
    loading.value = false
  }
}

async function downloadSaved(name: string) {
  errorCode.value = null
  errorDetail.value = ''
  try {
    const blob = await fetchFile(name)
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = name
    link.click()
    window.setTimeout(() => URL.revokeObjectURL(url), 1000)
  } catch (error) {
    showError(error)
  }
}

async function openSaved(name: string) {
  errorCode.value = null
  errorDetail.value = ''
  try {
    const blob = await fetchFile(name)
    resultName.value = name
    playBlob(blob)
  } catch (error) {
    showError(error)
  }
}

async function removeSaved(name: string) {
  if (!window.confirm(String(t('confirmDelete')))) {
    return
  }
  try {
    await deleteFile(name)
    if (resultName.value === name) {
      resultName.value = ''
    }
    await refresh()
  } catch (error) {
    showError(error)
  }
}
</script>

<template>
  <div class="page">
    <header class="site-header">
      <div class="shell bar">
        <p class="brand">음성 생성기</p>
        <nav class="locales" :aria-label="String(t('uiLanguage'))">
          <button
            v-for="item in localeItems"
            :key="item.id"
            type="button"
            :class="{ active: locale === item.id }"
            :aria-pressed="locale === item.id"
            @click="setLocale(item.id)"
          >
            {{ item.label }}<span v-if="locale === item.id" class="star" aria-hidden="true">★</span>
          </button>
        </nav>
      </div>
    </header>

    <main class="shell board">
      <article>
        <form id="speech-form" @submit.prevent="generate">
          <section>
            <label class="field" for="text">
              <span class="label-row">
                <span class="sr">{{ t('text') }}</span>
                <span class="bytes" :class="{ over: tooLong }">{{ bytes.toLocaleString() }} / 5,000 {{ t('bytes') }}</span>
              </span>
              <textarea id="text" v-model="text" rows="8" required />
            </label>
          </section>

          <section>
            <div class="split">
              <label class="field" for="filename">
                <span>{{ t('filename') }}</span>
                <input id="filename" v-model="filename" type="text" autocomplete="off" />
              </label>
              <label class="field" for="language">
                <span>{{ t('speechLanguage') }}</span>
                <select id="language" v-model="speechLanguage">
                  <option v-for="item in SPEECH_LANGUAGES" :key="item.code" :value="item.code">
                    {{ item.label }}
                  </option>
                </select>
              </label>
            </div>

            <fieldset>
              <legend>{{ t('gender') }}</legend>
              <div class="choices">
                <label><input v-model="gender" type="radio" value="FEMALE" /> {{ t('female') }}</label>
                <label><input v-model="gender" type="radio" value="MALE" /> {{ t('male') }}</label>
              </div>
            </fieldset>

            <fieldset>
              <legend>{{ t('voice') }}</legend>
              <div class="choices">
                <label><input v-model="voiceStyle" type="radio" value="NEURAL2" /> {{ t('neural') }}</label>
                <label><input v-model="voiceStyle" type="radio" value="STANDARD" /> {{ t('standard') }}</label>
              </div>
            </fieldset>

            <label class="field" for="rate">
              <span class="label-row">
                <span>{{ t('rate') }}</span>
                <span>{{ rateLabel }}</span>
              </span>
              <input id="rate" v-model.number="speakingRate" type="range" min="0.25" max="2" step="0.05" />
            </label>

            <label class="field" for="pitch">
              <span class="label-row">
                <span>{{ t('pitch') }}</span>
                <span>{{ pitchLabel }}</span>
              </span>
              <input id="pitch" v-model.number="pitch" type="range" min="-10" max="10" step="0.5" />
            </label>
          </section>

          <section class="saved">
            <h2>{{ t('saved') }}</h2>
            <p v-if="listFailed" class="error">{{ t('error.NETWORK') }}</p>
            <p v-else-if="savedFiles.length === 0" class="muted">{{ t('emptySaved') }}</p>
            <ul v-else>
              <li v-for="file in savedFiles" :key="file.filename">
                <button type="button" class="file-name" @click="openSaved(file.filename)">
                  <strong>{{ file.filename }}</strong>
                </button>
                <span class="file-meta">{{ formatSize(file.size) }} · {{ formatTime(file.modifiedAt) }}</span>
                <div class="row-actions">
                  <button type="button" @click="downloadSaved(file.filename)">{{ t('download') }}</button>
                  <button type="button" @click="removeSaved(file.filename)">{{ t('remove') }}</button>
                </div>
              </li>
            </ul>
          </section>
        </form>
      </article>

      <aside class="aside">
        <div class="spec">
          <dl>
            <div class="spec-row">
              <dt>{{ t('uiLanguage') }}</dt>
              <dd>{{ localeLabel }}</dd>
            </div>
            <div class="spec-row">
              <dt>{{ t('speechLanguage') }}</dt>
              <dd>{{ languageLabel }}</dd>
            </div>
            <div class="spec-row">
              <dt>{{ t('gender') }}</dt>
              <dd>{{ genderLabel }}</dd>
            </div>
            <div class="spec-row">
              <dt>{{ t('voice') }}</dt>
              <dd>{{ styleLabel }}</dd>
            </div>
            <div class="spec-row">
              <dt>{{ t('filename') }}</dt>
              <dd>{{ filename || 'speech' }}</dd>
            </div>
          </dl>

          <div class="preview">
            <p v-if="errorText" class="error" role="alert">
              {{ errorText }}
              <span v-if="showDetail">{{ errorDetail }}</span>
            </p>
            <p v-if="!audioUrl" class="muted">{{ t('emptyResult') }}</p>
            <template v-else>
              <p class="filename">{{ resultName }}</p>
              <audio ref="player" :src="audioUrl" controls />
            </template>
          </div>

          <button class="apply" type="submit" form="speech-form" :disabled="loading || tooLong">
            {{ loading ? t('generating') : t('generate') }}
          </button>
        </div>
      </aside>
    </main>
  </div>
</template>
