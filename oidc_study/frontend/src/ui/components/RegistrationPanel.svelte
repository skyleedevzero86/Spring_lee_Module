<script>
  export let form;
  export let busy = false;
  export let checkResult = null;
  export let checkTone;
  export let onChange;
  export let onCheckLoginId;
  export let onSubmit;
</script>

<section class="panel registration">
  <div class="panel-head">
    <h2>추가 회원가입 정보 입력</h2>
    <p>
      아이디 중복확인 후 이름, 연락처, 약관동의를 제출하면 승인 대기로
      전환됩니다.
    </p>
  </div>
  <div class="form-grid">
    <label>
      <span>로그인 아이디</span>
      <div class="inline-row">
        <input
          value={form.loginId}
          on:input={(event) => onChange("loginId", event.currentTarget.value)}
          placeholder="예: member001"
        />
        <button
          type="button"
          class="check"
          disabled={busy}
          on:click={onCheckLoginId}>중복확인</button
        >
      </div>
    </label>
    {#if checkResult}
      <p class={`check-message ${checkTone(checkResult)}`}>
        {checkResult.message}
      </p>
    {/if}
    <label>
      <span>이름</span>
      <input
        value={form.displayName}
        on:input={(event) => onChange("displayName", event.currentTarget.value)}
        placeholder="실명을 입력하세요"
      />
    </label>
    <label>
      <span>연락처</span>
      <input
        value={form.contactNumber}
        on:input={(event) =>
          onChange("contactNumber", event.currentTarget.value)}
        placeholder="010-1234-5678"
      />
    </label>
    <label class="terms">
      <input
        type="checkbox"
        checked={form.agreedToTerms}
        on:change={(event) =>
          onChange("agreedToTerms", event.currentTarget.checked)}
      />
      <span>서비스 이용약관과 개인정보 처리에 동의합니다.</span>
    </label>
    <button class="submit" disabled={busy} on:click={onSubmit}
      >가입 신청하기</button
    >
  </div>
</section>

<style>
  .panel {
    padding: 24px;
    background: rgba(255, 252, 245, 0.92);
    border: 1px solid rgba(23, 33, 27, 0.1);
    border-radius: 28px;
    box-shadow: 0 24px 60px rgba(42, 52, 33, 0.14);
  }
  .registration {
    grid-column: 1 / -1;
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
  .form-grid {
    display: grid;
    gap: 14px;
    margin-top: 18px;
  }
  label span {
    display: block;
    margin-bottom: 8px;
    font-size: 0.92rem;
    font-weight: 700;
    color: #526253;
  }
  input {
    width: 100%;
    box-sizing: border-box;
    padding: 12px 14px;
    border-radius: 16px;
    border: 1px solid rgba(23, 33, 27, 0.12);
    font: inherit;
  }
  .inline-row {
    display: grid;
    grid-template-columns: 1fr auto;
    gap: 12px;
  }
  .check,
  .submit {
    border: none;
    border-radius: 999px;
    padding: 12px 18px;
    font-weight: 700;
    cursor: pointer;
  }
  .check {
    background: #17211b;
    color: white;
  }
  .submit {
    justify-self: start;
    background: #03c75a;
    color: white;
  }
  .terms {
    display: flex;
    align-items: center;
    gap: 10px;
  }
  .terms span {
    margin: 0;
  }
  .terms input {
    width: auto;
  }
  .check-message {
    margin: -2px 0 0;
    padding: 10px 14px;
    border-radius: 14px;
    font-weight: 700;
  }
  .check-message.success {
    background: rgba(3, 199, 90, 0.12);
    color: #02753a;
  }
  .check-message.warning {
    background: rgba(199, 131, 24, 0.12);
    color: #9a5c00;
  }
  .check-message.error {
    background: rgba(197, 58, 58, 0.12);
    color: #9d2020;
  }
  .check-message.neutral {
    background: rgba(23, 33, 27, 0.06);
    color: #445046;
  }
  @media (max-width: 980px) {
    .inline-row {
      grid-template-columns: 1fr;
    }
  }
</style>
