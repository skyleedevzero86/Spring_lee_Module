<script>
  export let claims = "{}";
</script>

<section class="panel">
  <div class="panel-head">
    <h2>ID Token Claims</h2>
    <p>OIDC 로그인 결과로 내려온 Claims를 확인할 수 있습니다.</p>
  </div>
  <pre>{claims}</pre>
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
  pre {
    margin: 18px 0 0;
    padding: 16px;
    border-radius: 18px;
    background: #17211b;
    color: #eaf6ec;
    overflow: auto;
    font-size: 0.88rem;
    line-height: 1.55;
  }
</style>
