<script>
  export let status = "PENDING";
  $: message =
    status === "REJECTED"
      ? "가입 신청이 반려되었습니다. 다시 승인 받으려면 관리자에게 문의해 주세요."
      : "가입 신청이 접수되었습니다. 관리자 승인 전까지 보호된 메뉴는 열리지 않습니다.";
</script>

<section class="panel pending-panel">
  <div class="panel-head">
    <h2>{status === "REJECTED" ? "가입 반려 상태" : "승인 대기 중"}</h2>
    <p>{message}</p>
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
    line-height: 1.8;
  }
</style>
