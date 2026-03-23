<section class="panel">
  <div class="panel-head">
    <h2>가입 정책</h2>
    <p>회원가입은 OIDC 인증 이후 추가 가입정보를 받는 2단계 구조입니다.</p>
  </div>
  <ul>
    <li>1차 인증: 네이버 OIDC 로그인</li>
    <li>2차 가입: 아이디, 이름, 연락처, 약관동의 입력</li>
    <li>제출 후 상태: `PENDING`</li>
    <li>탈퇴 후 상태: `WITHDRAWN`</li>
    <li>탈퇴 아이디 재사용: 관리자 문의 필요</li>
  </ul>
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
  ul {
    margin: 18px 0 0;
    padding-left: 18px;
    line-height: 1.85;
    color: #526253;
  }
</style>
