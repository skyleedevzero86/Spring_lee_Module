<script>
  import { onMount } from "svelte";
  import { createApprovalConsoleStore } from "./application/createApprovalConsoleStore.js";
  import {
    ASSIGNABLE_ROLES,
    STATUS_TABS,
    canManageUsers,
    canOpenDashboard,
    canWithdraw,
    formatDate,
    isWithdrawn,
    loginIdCheckTone,
    needsRegistration,
    statusTone,
  } from "./domain/approvalConsole.js";
  import AdminNotificationPanel from "./ui/components/AdminNotificationPanel.svelte";
  import AdminPanel from "./ui/components/AdminPanel.svelte";
  import ClaimsPanel from "./ui/components/ClaimsPanel.svelte";
  import DashboardPanel from "./ui/components/DashboardPanel.svelte";
  import PendingPanel from "./ui/components/PendingPanel.svelte";
  import PolicyPanel from "./ui/components/PolicyPanel.svelte";
  import RegistrationPanel from "./ui/components/RegistrationPanel.svelte";
  import SessionPanel from "./ui/components/SessionPanel.svelte";
  import WithdrawnPanel from "./ui/components/WithdrawnPanel.svelte";

  const backendBaseUrl = (import.meta.env.VITE_BACKEND_BASE_URL ?? "").replace(/\/$/, "");
  const approvalConsole = createApprovalConsoleStore();
  let loginErrorMessage = "";
  let currentPage = "my";

  const resolveLoginErrorMessage = () => {
    const hashQuery = window.location.hash.includes("?")
      ? window.location.hash.slice(window.location.hash.indexOf("?"))
      : "";
    const params = new URLSearchParams(window.location.search || hashQuery);
    if (params.get("login") !== "error") {
      return "";
    }
    const reason = (params.get("reason") ?? "").toLowerCase();
    if (reason.includes("invalid_client")) {
      return "네이버 Client ID/Secret이 올바르지 않습니다. 개발자센터 설정과 application.yml 값을 다시 확인해 주세요.";
    }
    if (reason.includes("redirect_uri")) {
      return "리다이렉트 URI가 일치하지 않습니다. 네이버 Callback URL과 서버 redirect-uri를 동일하게 맞춰 주세요.";
    }
    if (reason.includes("invalid_scope")) {
      return "요청한 scope가 네이버 앱 설정과 맞지 않습니다. openid 사용 설정을 확인해 주세요.";
    }
    if (reason.includes("invalid_id_token") || reason.includes("missing (required) id token")) {
      return "네이버가 id_token을 반환하지 않았습니다. 네이버 개발자센터에서 OpenID Connect 사용을 활성화하고 openid 권한을 허용해 주세요.";
    }
    return "네이버 로그인에 실패했습니다. 콘솔 로그(reason)와 네이버 앱 설정을 확인해 주세요.";
  };

  const login = () => {
    window.location.href = `${backendBaseUrl}/oauth2/authorization/naver`;
  };
  const logout = async () => {
    try {
      await fetch("/api/logout", {
        method: "POST",
      });
    } finally {
      window.location.href = "/";
    }
  };
  const requestWithdrawal = () => {
    if (
      window.confirm(
        "정말 탈퇴하시겠습니까? 탈퇴 후 동일 아이디는 관리자 문의가 필요합니다.",
      )
    ) {
      approvalConsole.withdrawAccount();
    }
  };

  const normalizePageFromHash = () => {
    const hash = window.location.hash.replace(/^#\/?/, "");
    return hash === "admin" ? "admin" : "my";
  };

  const moveToPage = (page) => {
    currentPage = page;
    window.location.hash = `/${page}`;
  };

  onMount(() => {
    loginErrorMessage = resolveLoginErrorMessage();
    if (loginErrorMessage) {
      window.history.replaceState({}, "", window.location.pathname);
    }
    currentPage = normalizePageFromHash();
    const onHashChange = () => {
      currentPage = normalizePageFromHash();
    };
    window.addEventListener("hashchange", onHashChange);
    approvalConsole.initialize();
    return () => window.removeEventListener("hashchange", onHashChange);
  });

  $: state = $approvalConsole;
  $: prettyClaims = JSON.stringify(state.session?.oidcClaims ?? {}, null, 2);
  $: isAdminAvailable = canManageUsers(state.session);
  $: if (currentPage === "admin" && !isAdminAvailable) {
    currentPage = "my";
    if (window.location.hash !== "#/my") {
      window.location.hash = "/my";
    }
  }
</script>

<svelte:head>
  <title>OIDC Approval Console</title>
  <meta
    name="description"
    content="OIDC registration, approval, withdrawal console"
  />
</svelte:head>

{#if state.loading}
  <div class="shell shell-loading">
    <div class="loading-card">
      <p class="eyebrow">OIDC Approval Console</p>
      <h1>회원 상태와 승인 정보를 불러오는 중입니다</h1>
      <p>가입 상태, 관리자 알림, 권한 정보를 한 번에 조립하고 있습니다.</p>
    </div>
  </div>
{:else}
  <div class="dashboard-shell">
    <header class="topbar">
      <div class="brand">OIDC Console</div>
      <div class="top-actions">
        {#if state.session?.authenticated}
          <button class="secondary" on:click={logout}>로그아웃</button>
        {:else}
          <button class="primary" on:click={login}>네이버로 로그인</button>
        {/if}
      </div>
    </header>

    <div class="workspace">
      <aside class="sidebar">
        <button
          class:selected={currentPage === "my"}
          on:click={() => moveToPage("my")}>마이 페이지</button
        >
        {#if isAdminAvailable}
          <button
            class:selected={currentPage === "admin"}
            on:click={() => moveToPage("admin")}>관리자 페이지</button
          >
        {/if}
      </aside>

      <main class="content">
        {#if state.error || loginErrorMessage}
          <p class="message error">{state.error || loginErrorMessage}</p>
        {/if}
        {#if state.flash}
          <p class="message success">{state.flash}</p>
        {/if}

        {#if currentPage === "my"}
          <section class="summary-cards">
            <article>
              <span>현재 상태</span>
              <strong>{state.session?.account?.status ?? "ANONYMOUS"}</strong>
            </article>
            <article>
              <span>로그인</span>
              <strong>{state.session?.authenticated ? "인증됨" : "미인증"}</strong>
            </article>
            <article>
              <span>권한 수</span>
              <strong>{state.session?.account?.roles?.length ?? 0}</strong>
            </article>
          </section>

          <section class="grid">
            <SessionPanel
              session={state.session}
              {formatDate}
              withdrawReason={state.withdrawReason}
              busy={state.busy}
              canWithdraw={canWithdraw(state.session)}
              onWithdrawReasonChange={approvalConsole.updateWithdrawReason}
              onWithdraw={requestWithdrawal}
            />
            <PolicyPanel />
            <ClaimsPanel claims={prettyClaims} />

            {#if needsRegistration(state.session)}
              <RegistrationPanel
                form={state.registrationForm}
                busy={state.busy}
                checkResult={state.loginIdCheck}
                checkTone={loginIdCheckTone}
                onChange={approvalConsole.updateRegistration}
                onCheckLoginId={approvalConsole.checkLoginId}
                onSubmit={approvalConsole.submitRegistration}
              />
            {/if}

            {#if isWithdrawn(state.session)}
              <WithdrawnPanel account={state.session.account} {formatDate} />
            {/if}

            {#if state.session?.account?.status === "PENDING" || state.session?.account?.status === "REJECTED"}
              <PendingPanel status={state.session.account.status} />
            {/if}

            {#if canOpenDashboard(state.session) && state.dashboard}
              <DashboardPanel dashboard={state.dashboard} />
            {/if}
          </section>
        {:else}
          <section class="summary-cards">
            <article>
              <span>관리 대상 상태</span>
              <strong>{state.selectedStatus}</strong>
            </article>
            <article>
              <span>표시 회원 수</span>
              <strong>{state.users.length}</strong>
            </article>
            <article>
              <span>최근 알림 수</span>
              <strong>{state.notifications.length}</strong>
            </article>
          </section>

          <section class="grid admin-grid">
            <AdminNotificationPanel
              notifications={state.notifications}
              {formatDate}
            />
            <AdminPanel
              users={state.users}
              selectedStatus={state.selectedStatus}
              statusTabs={STATUS_TABS}
              assignableRoles={ASSIGNABLE_ROLES}
              roleSelections={state.roleSelections}
              busy={state.busy}
              {statusTone}
              onSelectStatus={approvalConsole.selectStatus}
              onToggleRole={approvalConsole.toggleRole}
              onApprove={approvalConsole.approveUser}
              onReject={approvalConsole.rejectUser}
            />
          </section>
        {/if}
      </main>
    </div>
  </div>
{/if}

<style>
  :global(body) {
    margin: 0;
    font-family: "Pretendard", "Apple SD Gothic Neo", "Noto Sans KR", sans-serif;
    background: radial-gradient(
        circle at top left,
        rgba(255, 196, 88, 0.22),
        transparent 28%
      ),
      radial-gradient(
        circle at top right,
        rgba(3, 199, 90, 0.18),
        transparent 24%
      ),
      linear-gradient(180deg, #f8f4eb 0%, #efe7d6 100%);
    color: #17211b;
  }

  .dashboard-shell {
    max-width: 1400px;
    margin: 0 auto;
    padding: 16px 18px 24px;
  }
  .topbar {
    height: 58px;
    background: #1f2640;
    color: white;
    border-radius: 12px;
    padding: 0 16px;
    display: flex;
    align-items: center;
    justify-content: space-between;
  }
  .brand {
    font-weight: 800;
    letter-spacing: 0.02em;
  }
  .top-actions button {
    border: none;
    border-radius: 999px;
    padding: 8px 14px;
    font-weight: 700;
    cursor: pointer;
  }
  .top-actions .primary {
    background: #03c75a;
    color: white;
  }
  .top-actions .secondary {
    background: white;
    color: #17211b;
  }
  .workspace {
    display: grid;
    grid-template-columns: 220px 1fr;
    gap: 16px;
    margin-top: 16px;
  }
  .sidebar {
    background: rgba(255, 252, 245, 0.92);
    border: 1px solid rgba(23, 33, 27, 0.1);
    border-radius: 16px;
    padding: 12px;
    display: grid;
    align-content: start;
    gap: 8px;
    height: fit-content;
  }
  .sidebar button {
    border: none;
    border-radius: 10px;
    padding: 10px 12px;
    text-align: left;
    font-weight: 700;
    background: rgba(23, 33, 27, 0.06);
    color: #17211b;
    cursor: pointer;
  }
  .sidebar button.selected {
    background: #3657ff;
    color: white;
  }
  .content {
    min-width: 0;
  }
  .summary-cards {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 12px;
    margin-bottom: 12px;
  }
  .summary-cards article {
    background: rgba(255, 252, 245, 0.92);
    border: 1px solid rgba(23, 33, 27, 0.1);
    border-radius: 14px;
    padding: 14px;
  }
  .summary-cards span {
    display: block;
    color: #667567;
    font-size: 0.82rem;
    font-weight: 700;
  }
  .summary-cards strong {
    display: block;
    margin-top: 8px;
    font-size: 1.2rem;
  }

  .shell-loading {
    min-height: 100vh;
    display: grid;
    place-items: center;
  }

  .loading-card {
    padding: 32px;
    max-width: 620px;
    background: rgba(255, 252, 245, 0.92);
    border: 1px solid rgba(23, 33, 27, 0.1);
    border-radius: 28px;
    box-shadow: 0 24px 60px rgba(42, 52, 33, 0.14);
  }

  .eyebrow {
    margin: 0 0 14px;
    text-transform: uppercase;
    letter-spacing: 0.08em;
    font-size: 0.86rem;
    font-weight: 800;
    color: #028742;
  }

  h1 {
    margin: 0;
    font-size: clamp(2.4rem, 4vw, 4.8rem);
    line-height: 0.96;
    letter-spacing: -0.05em;
  }

  .grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 14px;
  }
  .admin-grid {
    grid-template-columns: 1fr;
  }
  .message {
    margin: 0 0 12px;
    padding: 12px 14px;
    border-radius: 12px;
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
    .workspace {
      grid-template-columns: 1fr;
    }
    .summary-cards {
      grid-template-columns: 1fr;
    }
    .grid {
      grid-template-columns: 1fr;
    }
  }
</style>
