<script>
  export let session;
  export let formatDate;
  export let withdrawReason = "";
  export let busy = false;
  export let canWithdraw = false;
  export let onWithdrawReasonChange;
  export let onWithdraw;

  $: account = session?.account;
  const deriveLoginId = (value, claims) => {
    if (value) {
      return value;
    }
    const email = String(claims?.email ?? "").trim().toLowerCase();
    if (!email.includes("@")) {
      return "-";
    }
    const localPart = email.split("@")[0].replace(/[^a-z0-9_-]/g, "");
    return localPart || "-";
  };
  const deriveContactNumber = (value, claims) => {
    return value || claims?.mobile || claims?.mobile_e164 || "-";
  };
  $: claims = session?.oidcClaims ?? {};
</script>

<section class="panel">
  <div class="panel-head">
    <h2>세션 정보</h2>
    <p>
      현재 로그인 상태, 가입정보, 승인 시각, 탈퇴 가능 여부를 확인할 수
      있습니다.
    </p>
  </div>
  {#if session?.authenticated && account}
    <div class="profile-grid">
      <div><span>이름</span><strong>{account.displayName ?? "-"}</strong></div>
      <div><span>이메일</span><strong>{account.email ?? "-"}</strong></div>
      <div>
        <span>로그인 아이디</span><strong
          >{deriveLoginId(account.loginId, claims)}</strong
        >
      </div>
      <div>
        <span>연락처</span><strong
          >{deriveContactNumber(account.contactNumber, claims)}</strong
        >
      </div>
      <div>
        <span>Provider Sub</span><strong>{account.providerUserId}</strong>
      </div>
      <div>
        <span>Roles</span><strong>{account.roles?.join(", ") || "-"}</strong>
      </div>
      <div>
        <span>약관동의 시각</span><strong
          >{formatDate(account.termsAgreedAt)}</strong
        >
      </div>
      <div>
        <span>승인 시각</span><strong>{formatDate(account.approvedAt)}</strong>
      </div>
      <div>
        <span>마지막 로그인</span><strong
          >{formatDate(account.lastLoginAt)}</strong
        >
      </div>
      <div>
        <span>탈퇴 시각</span><strong>{formatDate(account.withdrawnAt)}</strong>
      </div>
    </div>
    {#if canWithdraw}
      <div class="withdraw-box">
        <h3>회원 탈퇴</h3>
        <p>
          탈퇴 시 데이터는 삭제되지 않으며 계정 상태만 `WITHDRAWN` 으로
          변경됩니다.
        </p>
        <textarea
          rows="3"
          placeholder="선택 입력: 탈퇴 사유"
          value={withdrawReason}
          on:input={(event) =>
            onWithdrawReasonChange(event.currentTarget.value)}
        ></textarea>
        <button class="withdraw" disabled={busy} on:click={onWithdraw}
          >탈퇴 요청</button
        >
      </div>
    {/if}
  {:else if session?.authenticated}
    <p>로그인은 되었지만 애플리케이션 계정 정보가 아직 연결되지 않았습니다.</p>
  {:else}
    <p>
      로그인 이전 상태입니다. 네이버 로그인 후 가입 또는 승인 상태를 확인할 수
      있습니다.
    </p>
  {/if}
</section>

<style>
  .panel {
    padding: 24px;
    background: rgba(255, 252, 245, 0.92);
    border: 1px solid rgba(23, 33, 27, 0.1);
    border-radius: 28px;
    box-shadow: 0 24px 60px rgba(42, 52, 33, 0.14);
  }
  .panel-head h2 {
    margin: 0;
    font-size: 1.35rem;
  }
  .panel-head p {
    margin: 8px 0 0;
    color: #526253;
    line-height: 1.65;
  }
  .profile-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 14px;
    margin-top: 20px;
  }
  .profile-grid div,
  .withdraw-box {
    padding: 16px;
    border-radius: 20px;
    background: rgba(255, 255, 255, 0.88);
    border: 1px solid rgba(23, 33, 27, 0.08);
  }
  span {
    display: block;
    font-size: 0.82rem;
    color: #667567;
    font-weight: 700;
  }
  strong {
    display: block;
    margin-top: 6px;
    font-size: 1rem;
    word-break: break-word;
  }
  .withdraw-box {
    grid-column: 1 / -1;
    display: grid;
    gap: 12px;
    margin-top: 14px;
  }
  .withdraw-box h3 {
    margin: 0;
    font-size: 1.05rem;
  }
  .withdraw-box p {
    margin: 0;
    color: #526253;
    line-height: 1.65;
  }
  textarea {
    resize: vertical;
    min-height: 88px;
    padding: 12px 14px;
    border-radius: 16px;
    border: 1px solid rgba(23, 33, 27, 0.12);
    font: inherit;
  }
  .withdraw {
    justify-self: start;
    border: none;
    border-radius: 999px;
    padding: 10px 16px;
    background: #9d2020;
    color: white;
    font-weight: 700;
    cursor: pointer;
  }
  @media (max-width: 980px) {
    .profile-grid {
      grid-template-columns: 1fr;
    }
  }
</style>
