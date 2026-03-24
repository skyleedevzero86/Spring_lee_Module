<script>
  export let session;
  export let flash = "";
  export let error = "";
  export let statusTone;
  export let onLogin;
  export let onLogout;

  $: account = session?.account;
</script>

<header class="hero">
  <div class="hero-copy">
    <h1>OIDC 회원가입, 승인, 탈퇴 콘솔</h1>
    <div class="hero-actions">
      {#if session?.authenticated}
        <button class="secondary" on:click={onLogout}>로그아웃</button>
      {:else}
        <button class="primary" on:click={onLogin}>네이버로 로그인</button>
      {/if}
    </div>
    {#if error}
      <p class="message error">{error}</p>
    {/if}
    {#if flash}
      <p class="message success">{flash}</p>
    {/if}
  </div>
  <div class="hero-side">
    <div class={`status-badge ${statusTone(account?.status ?? "PENDING")}`}>
      <span>현재 상태</span>
      <strong>{account?.status ?? "ANONYMOUS"}</strong>
    </div>
    <ul class="mini-list">
      <li>가입정보: 아이디, 이름, 연락처, 약관동의</li>
      <li>승인 프로세스: SIGNUP_REQUIRED → PENDING → ACTIVE</li>
      <li>탈퇴 처리: DB 삭제 없이 WITHDRAWN 상태 유지</li>
      <li>중복검사: 탈퇴 이력은 관리자 문의 안내</li>
    </ul>
  </div>
</header>

<style>
  .hero {
    display: grid;
    grid-template-columns: 1.2fr 0.8fr;
    gap: 20px;
    padding: 28px;
    margin-bottom: 22px;
    background: rgba(255, 252, 245, 0.92);
    border: 1px solid rgba(23, 33, 27, 0.1);
    border-radius: 28px;
    box-shadow: 0 24px 60px rgba(42, 52, 33, 0.14);
    backdrop-filter: blur(10px);
  }
  h1 {
    margin: 0;
    font-size: clamp(2.4rem, 4vw, 4.8rem);
    line-height: 0.96;
    letter-spacing: -0.05em;
  }
  .hero-actions {
    display: flex;
    gap: 12px;
    margin-top: 22px;
  }
  button {
    border: none;
    border-radius: 999px;
    padding: 12px 18px;
    cursor: pointer;
    font-weight: 700;
  }
  .primary {
    background: #03c75a;
    color: white;
  }
  .secondary {
    background: white;
    border: 1px solid rgba(23, 33, 27, 0.14);
  }
  .hero-side {
    display: grid;
    gap: 16px;
  }
  .status-badge {
    padding: 22px;
    border-radius: 24px;
    color: white;
  }
  .status-badge.pending {
    background: #c78318;
  }
  .status-badge.active {
    background: #03c75a;
  }
  .status-badge.rejected {
    background: #4e5b53;
  }
  .status-badge.withdrawn {
    background: #9d2020;
  }
  .status-badge span {
    display: block;
    font-size: 0.86rem;
    text-transform: uppercase;
    letter-spacing: 0.08em;
    opacity: 0.9;
  }
  .status-badge strong {
    display: block;
    margin-top: 12px;
    font-size: 2rem;
  }
  .mini-list {
    margin: 0;
    padding: 0 0 0 18px;
    line-height: 1.9;
    color: #526253;
  }
  .message {
    margin: 18px 0 0;
    padding: 12px 14px;
    border-radius: 16px;
    font-weight: 700;
  }
  .message.error {
    background: rgba(197, 58, 58, 0.12);
    color: #9d2020;
  }
  .message.success {
    background: rgba(3, 199, 90, 0.12);
    color: #02753a;
  }
  @media (max-width: 980px) {
    .hero {
      grid-template-columns: 1fr;
    }
  }
</style>
