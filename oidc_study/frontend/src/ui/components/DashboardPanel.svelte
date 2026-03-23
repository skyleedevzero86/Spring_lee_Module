<script>
  export let dashboard;
</script>

<section class="panel">
  <div class="panel-head">
    <h2>운영 대시보드</h2>
    <p>승인 완료 회원만 접근할 수 있는 집계 화면입니다.</p>
  </div>
  <div class="metric-grid">
    <div><span>전체 회원</span><strong>{dashboard.totalUsers}</strong></div>
    <div><span>활성 회원</span><strong>{dashboard.activeUsers}</strong></div>
    <div><span>대기 회원</span><strong>{dashboard.pendingUsers}</strong></div>
    <div><span>반려 회원</span><strong>{dashboard.rejectedUsers}</strong></div>
    <div><span>탈퇴 회원</span><strong>{dashboard.withdrawnUsers}</strong></div>
    <div><span>내 상태</span><strong>{dashboard.myStatus}</strong></div>
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
  .panel-head h2 {
    margin: 0;
    font-size: 1.35rem;
  }
  .panel-head p {
    margin: 8px 0 0;
    color: #526253;
    line-height: 1.65;
  }
  .metric-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 14px;
    margin-top: 20px;
  }
  .metric-grid div {
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
  }
  @media (max-width: 980px) {
    .metric-grid {
      grid-template-columns: 1fr;
    }
  }
</style>
