<script>
  export let account;
  export let formatDate;
</script>

<section class="panel withdrawn-panel">
  <div class="panel-head">
    <h2>탈퇴 처리된 계정입니다</h2>
    <p>
      현재 계정은 `WITHDRAWN` 상태입니다. 동일 아이디 재사용이나 재가입 복구는
      자동 승인 대상이 아니며, 관리자에게 문의해야 합니다.
    </p>
  </div>
  <div class="detail-grid">
    <div><span>아이디</span><strong>{account.loginId ?? "-"}</strong></div>
    <div>
      <span>탈퇴 시각</span><strong>{formatDate(account.withdrawnAt)}</strong>
    </div>
  </div>
</section>

<style>
  .panel {
    padding: 24px;
    background: rgba(255, 246, 246, 0.96);
    border: 1px solid rgba(157, 32, 32, 0.12);
    border-radius: 28px;
    box-shadow: 0 24px 60px rgba(157, 32, 32, 0.08);
  }
  .panel-head h2 {
    margin: 0;
    font-size: 1.35rem;
    color: #9d2020;
  }
  .panel-head p {
    margin: 8px 0 0;
    color: #7d3030;
    line-height: 1.8;
  }
  .detail-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 14px;
    margin-top: 18px;
  }
  .detail-grid div {
    padding: 16px;
    border-radius: 18px;
    background: white;
  }
  span {
    display: block;
    font-size: 0.82rem;
    font-weight: 700;
    color: #8a4f4f;
  }
  strong {
    display: block;
    margin-top: 6px;
  }
  @media (max-width: 980px) {
    .detail-grid {
      grid-template-columns: 1fr;
    }
  }
</style>
