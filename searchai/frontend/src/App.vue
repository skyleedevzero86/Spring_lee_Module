<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue'
import { login, openSse, sendChat, uploadPdf } from './api/searchAiApi'
import AdminDashboard from './components/AdminDashboard.vue'
import {
  canUploadPdf,
  canUseInternetSearch,
  canUseKnowledge,
  placeholderFor,
  type AuthSession,
  type ChatMessage,
  type ChatMode,
} from './domain/chat'

const session = ref<AuthSession | null>(null)
const username = ref('user')
const password = ref('user123')
const loginError = ref('')
const toast = ref('')
const toastType = ref<'success' | 'error'>('success')
const messages = ref<ChatMessage[]>([])
const draft = ref('')
const mode = ref<ChatMode>('DIRECT')
const sending = ref(false)
const uploading = ref(false)
const routingInfo = ref('')
const routingMode = ref<'AUTO' | 'MANUAL'>('AUTO')
const forcedTier = ref('LUNA')
const adminTab = ref<'chat' | 'stats'>('chat')
const chatPane = ref<HTMLElement | null>(null)
const sessionId = `session-${Date.now()}-${Math.random().toString(36).slice(2, 9)}`
let eventSource: EventSource | null = null
let botBufferId: string | null = null

const isAdmin = computed(() => session.value?.role === 'ADMIN')
const placeholder = computed(() =>
  session.value ? placeholderFor(mode.value, session.value.role) : '질문을 입력하세요...',
)

const showToast = (message: string, type: 'success' | 'error' = 'success') => {
  toast.value = message
  toastType.value = type
  window.setTimeout(() => {
    toast.value = ''
  }, 2800)
}

const scrollBottom = async () => {
  await nextTick()
  if (chatPane.value) {
    chatPane.value.scrollTop = chatPane.value.scrollHeight
  }
}

const doLogin = async () => {
  loginError.value = ''
  try {
    session.value = await login(username.value.trim(), password.value)
    mode.value = 'DIRECT'
    adminTab.value = 'chat'
  } catch (cause) {
    loginError.value = cause instanceof Error ? cause.message : '로그인에 실패했습니다'
  }
}

const logout = () => {
  eventSource?.close()
  eventSource = null
  session.value = null
  messages.value = []
  draft.value = ''
  mode.value = 'DIRECT'
  adminTab.value = 'chat'
}

const connectSse = () => {
  if (!session.value) return
  eventSource?.close()
  botBufferId = null
  eventSource = openSse(session.value.token, sessionId)

  eventSource.addEventListener('routing', (event) => {
    try {
      const payload = JSON.parse((event as MessageEvent).data)
      const budget = payload.budget ? ` · budget ${payload.budget}` : ''
      const cached = payload.cached === 'true' ? ' · cache' : ''
      routingInfo.value = `${payload.tier} · ${payload.model} · ${(payload.confidence * 100).toFixed(0)}%${budget}${cached}`
    } catch {
      routingInfo.value = String((event as MessageEvent).data)
    }
  })

  eventSource.addEventListener('add', (event) => {
    const data = (event as MessageEvent).data
    if (!data || String(data).toLowerCase() === 'null') return
    if (!botBufferId) {
      botBufferId = `bot-${Date.now()}`
      messages.value.push({ id: botBufferId, role: 'bot', html: '' })
    }
    const target = messages.value.find((item) => item.id === botBufferId)
    if (target) {
      target.html += String(data).replace(/\n/g, '<br>')
      void scrollBottom()
    }
  })

  eventSource.addEventListener('finish', () => {
    botBufferId = null
    sending.value = false
    eventSource?.close()
    eventSource = null
  })

  eventSource.onerror = () => {
    if (botBufferId) {
      const target = messages.value.find((item) => item.id === botBufferId)
      if (target) {
        target.html += '<br><strong style="color:#c0392b">연결이 중단되었습니다. 다시 시도해 주세요.</strong>'
      }
    }
    sending.value = false
    eventSource?.close()
    eventSource = null
  }
}

const submitMessage = async () => {
  if (!session.value || sending.value) return
  const text = draft.value.trim()
  if (!text) return

  messages.value.push({
    id: `user-${Date.now()}`,
    role: 'user',
    html: text,
  })
  draft.value = ''
  sending.value = true
  await scrollBottom()
  connectSse()

  try {
    await sendChat(
      session.value.token,
      sessionId,
      text,
      mode.value,
      isAdmin.value ? routingMode.value : 'AUTO',
      isAdmin.value && routingMode.value === 'MANUAL' ? forcedTier.value : undefined,
    )
  } catch (cause) {
    showToast(cause instanceof Error ? cause.message : '전송 실패', 'error')
    sending.value = false
    eventSource?.close()
    eventSource = null
  }
}

const onUpload = async (event: Event) => {
  if (!session.value || !canUploadPdf(session.value.role)) return
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  uploading.value = true
  try {
    const msg = await uploadPdf(session.value.token, file)
    showToast(msg || `문서 "${file.name}" 업로드 성공!`, 'success')
    mode.value = 'KNOWLEDGE_BASE'
  } catch (cause) {
    showToast(cause instanceof Error ? cause.message : '업로드 실패', 'error')
  } finally {
    uploading.value = false
    input.value = ''
  }
}

watch(mode, (next) => {
  if (!session.value) return
  if (next === 'KNOWLEDGE_BASE' && !canUseKnowledge(session.value.role)) mode.value = 'DIRECT'
  if (next === 'INTERNET_SEARCH' && !canUseInternetSearch(session.value.role)) mode.value = 'DIRECT'
})

onBeforeUnmount(() => {
  eventSource?.close()
})
</script>

<template>
  <main class="shell">
    <section v-if="!session" class="login">
      <p class="brand">SearchAI</p>
      <h1>RAG · 검색 스트리밍 채팅</h1>
      <p class="lead">일반 사용자는 채팅만, 관리자는 PDF 업로드·검색·통계·토큰/라우터 정책을 사용합니다.</p>
      <form class="login-form" @submit.prevent="doLogin">
        <label>
          아이디
          <input v-model="username" autocomplete="username" />
        </label>
        <label>
          비밀번호
          <input v-model="password" type="password" autocomplete="current-password" />
        </label>
        <button type="submit">로그인</button>
        <p v-if="loginError" class="error">{{ loginError }}</p>
        <p class="hint">demo — admin/admin123 · user/user123</p>
      </form>
    </section>

    <section v-else class="workspace" :class="{ admin: isAdmin }">
      <header v-if="isAdmin" class="admin-tabs">
        <button type="button" :class="{ active: adminTab === 'chat' }" @click="adminTab = 'chat'">채팅</button>
        <button type="button" :class="{ active: adminTab === 'stats' }" @click="adminTab = 'stats'">통계 · 정책</button>
        <div class="who">
          <span>{{ session.username }}</span>
          <em>{{ session.role }}</em>
          <button type="button" class="ghost" @click="logout">로그아웃</button>
        </div>
      </header>

      <section v-show="!isAdmin || adminTab === 'chat'" class="chat">
        <header class="header">
          <div>
            <p class="brand">SearchAI</p>
            <h1>스트리밍 대화</h1>
          </div>
          <div v-if="!isAdmin" class="who">
            <span>{{ session.username }}</span>
            <em>{{ session.role }}</em>
            <button type="button" class="ghost" @click="logout">로그아웃</button>
          </div>
        </header>

        <div ref="chatPane" class="messages">
          <p v-if="routingInfo" class="routing">Model Router → {{ routingInfo }}</p>
          <article
            v-for="item in messages"
            :key="item.id"
            class="bubble"
            :class="item.role"
            v-html="item.html"
          />
        </div>

        <div v-if="toast" class="toast" :class="toastType">{{ toast }}</div>

        <footer class="composer">
          <label
            v-if="isAdmin"
            class="upload"
            :class="{ loading: uploading }"
            title="관리자 PDF 업로드"
          >
            <input type="file" accept=".pdf" :disabled="uploading" @change="onUpload" />
            <span>PDF</span>
          </label>

          <select v-if="isAdmin" v-model="mode" title="대화 모드">
            <option value="DIRECT">직접 대화</option>
            <option value="KNOWLEDGE_BASE">지식 기반</option>
            <option value="INTERNET_SEARCH">인터넷 검색</option>
          </select>
          <select v-if="isAdmin" v-model="routingMode" title="라우팅 모드">
            <option value="AUTO">자동 라우팅</option>
            <option value="MANUAL">수동 지정</option>
          </select>
          <select v-if="isAdmin && routingMode === 'MANUAL'" v-model="forcedTier" title="강제 티어">
            <option value="LUNA">luna</option>
            <option value="TERRA">terra</option>
            <option value="SOL">sol</option>
            <option value="ASTRA">astra</option>
          </select>
          <p v-else-if="!isAdmin" class="user-mode">채팅 전용</p>

          <textarea
            v-model="draft"
            rows="1"
            :placeholder="placeholder"
            :disabled="sending"
            @keydown.enter.exact.prevent="submitMessage"
          />
          <button type="button" :disabled="sending" @click="submitMessage">
            {{ sending ? '응답 중…' : '전송' }}
          </button>
        </footer>
      </section>

      <section v-if="isAdmin && adminTab === 'stats'" class="stats-pane">
        <AdminDashboard :token="session.token" />
      </section>
    </section>
  </main>
</template>

<style scoped>
.shell {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 24px;
}

.brand {
  margin: 0;
  font-family: "Fraunces", "IBM Plex Serif", serif;
  font-size: 1.35rem;
  letter-spacing: 0.04em;
  color: #1f5f8b;
}

.login,
.chat,
.stats-pane {
  width: min(860px, 100%);
  background: rgba(255, 255, 255, 0.88);
  border: 1px solid rgba(31, 95, 139, 0.12);
  box-shadow: 0 24px 60px rgba(28, 54, 78, 0.12);
  backdrop-filter: blur(8px);
}

.workspace {
  width: min(860px, 100%);
  display: grid;
  gap: 12px;
}

.workspace.admin {
  width: min(1180px, 100%);
}

.admin-tabs {
  display: flex;
  gap: 8px;
  align-items: center;
  padding: 8px 10px;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(31, 95, 139, 0.12);
  border-radius: 14px;
}

.admin-tabs button {
  border: 0;
  border-radius: 999px;
  padding: 8px 14px;
  background: transparent;
  color: #445566;
  cursor: pointer;
}

.admin-tabs button.active {
  background: #1f5f8b;
  color: #fff;
}

.admin-tabs .who {
  margin-left: auto;
  color: #1f5f8b;
}

.admin-tabs .ghost {
  border-color: #1f5f8b;
  color: #1f5f8b;
}

.stats-pane {
  width: 100%;
  padding: 18px;
  max-height: min(92vh, 960px);
  overflow: auto;
}

.login {
  padding: 40px 36px;
}

.login h1,
.header h1 {
  margin: 8px 0 0;
  font-family: "Fraunces", "IBM Plex Serif", serif;
  font-weight: 600;
  font-size: clamp(1.6rem, 3vw, 2.1rem);
}

.lead,
.hint {
  color: #5b6b7c;
  line-height: 1.55;
}

.login-form {
  display: grid;
  gap: 14px;
  margin-top: 28px;
}

.login-form label {
  display: grid;
  gap: 6px;
  font-size: 0.92rem;
}

.login-form input,
.composer textarea,
.composer select {
  border: 1px solid #c9d5e1;
  border-radius: 14px;
  padding: 12px 14px;
  background: #fff;
}

.login-form button,
.composer button {
  border: 0;
  border-radius: 999px;
  padding: 12px 22px;
  background: #1f5f8b;
  color: #fff;
  cursor: pointer;
}

.login-form button:hover,
.composer button:hover {
  background: #174a6d;
}

.error {
  color: #b42318;
}

.chat {
  width: 100%;
  height: min(90vh, 820px);
  display: grid;
  grid-template-rows: auto 1fr auto;
  position: relative;
  overflow: hidden;
}

.header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: end;
  padding: 20px 24px;
  border-bottom: 1px solid #e3ebf2;
  background: linear-gradient(120deg, #1f5f8b, #2a8f7b);
  color: #fff;
}

.header .brand,
.header h1 {
  color: #fff;
}

.who {
  display: flex;
  gap: 10px;
  align-items: center;
  font-size: 0.92rem;
}

.who em {
  font-style: normal;
  background: rgba(255, 255, 255, 0.18);
  padding: 4px 10px;
  border-radius: 999px;
}

.ghost {
  border: 1px solid rgba(255, 255, 255, 0.45);
  background: transparent;
  color: #fff;
  border-radius: 999px;
  padding: 6px 12px;
  cursor: pointer;
}

.messages {
  overflow: auto;
  padding: 22px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  background: linear-gradient(180deg, rgba(248, 251, 253, 0.9), rgba(241, 246, 250, 0.95));
}

.routing {
  margin: 0 0 4px;
  align-self: center;
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(31, 95, 139, 0.1);
  color: #1f5f8b;
  font-size: 0.82rem;
  font-weight: 600;
}

.bubble {
  max-width: 76%;
  padding: 12px 16px;
  border-radius: 18px;
  line-height: 1.55;
  word-break: break-word;
}

.bubble.user {
  align-self: flex-end;
  background: #d9f2e4;
  border-bottom-right-radius: 4px;
}

.bubble.bot {
  align-self: flex-start;
  background: #e8eef5;
  border-bottom-left-radius: 4px;
}

.composer {
  display: flex;
  gap: 10px;
  align-items: center;
  padding: 14px;
  border-top: 1px solid #e3ebf2;
  background: #f8fbfd;
}

.composer textarea {
  flex: 1;
  resize: none;
  min-height: 44px;
}

.composer button:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.upload {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  border: 1px solid #c9d5e1;
  display: grid;
  place-items: center;
  background: #fff;
  cursor: pointer;
  flex-shrink: 0;
}

.upload input {
  display: none;
}

.upload.loading {
  animation: spin 1s linear infinite;
}

.user-mode {
  margin: 0;
  padding: 10px 12px;
  border-radius: 999px;
  background: #e8eef5;
  color: #445566;
  font-size: 0.86rem;
  white-space: nowrap;
}

.toast {
  position: absolute;
  top: 88px;
  left: 50%;
  transform: translateX(-50%);
  padding: 10px 18px;
  border-radius: 10px;
  color: #fff;
  font-weight: 600;
  z-index: 2;
}

.toast.success {
  background: #1f7a4d;
}

.toast.error {
  background: #b42318;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 720px) {
  .header {
    flex-direction: column;
    align-items: start;
  }

  .composer {
    flex-wrap: wrap;
  }

  .composer textarea {
    order: 3;
    width: 100%;
  }

  .admin-tabs {
    flex-wrap: wrap;
  }

  .admin-tabs .who {
    margin-left: 0;
    width: 100%;
  }
}
</style>
