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
  import HeroSection from "./ui/components/HeroSection.svelte";
  import PendingPanel from "./ui/components/PendingPanel.svelte";
  import PolicyPanel from "./ui/components/PolicyPanel.svelte";
  import RegistrationPanel from "./ui/components/RegistrationPanel.svelte";
  import SessionPanel from "./ui/components/SessionPanel.svelte";
  import WithdrawnPanel from "./ui/components/WithdrawnPanel.svelte";

  const backendBaseUrl = (import.meta.env.VITE_BACKEND_BASE_URL ?? "http://localhost:9090").replace(/\/$/, "");
  const approvalConsole = createApprovalConsoleStore();
  let loginErrorMessage = "";

  const resolveLoginErrorMessage = () => {
    const params = new URLSearchParams(window.location.search);
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

  onMount(() => {
    loginErrorMessage = resolveLoginErrorMessage();
    if (loginErrorMessage) {
      window.history.replaceState({}, "", window.location.pathname);
    }
    approvalConsole.initialize();
  });

  $: state = $approvalConsole;
  $: prettyClaims = JSON.stringify(state.session?.oidcClaims ?? {}, null, 2);
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
  <div class="shell">
    <HeroSection
      session={state.session}
      flash={state.flash}
      error={state.error || loginErrorMessage}
      {statusTone}
      onLogin={login}
      onLogout={logout}
    />

    <main class="grid">
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

      {#if canManageUsers(state.session)}
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
      {/if}
    </main>
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

  .shell {
    max-width: 1320px;
    margin: 0 auto;
    padding: 32px 18px 48px;
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
    gap: 22px;
  }

  @media (max-width: 980px) {
    .grid {
      grid-template-columns: 1fr;
    }
  }
</style>
