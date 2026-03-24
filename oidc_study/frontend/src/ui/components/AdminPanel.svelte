<script>
  export let users = [];
  export let selectedStatus = "PENDING";
  export let statusTabs = [];
  export let assignableRoles = [];
  export let roleSelections = {};
  export let busy = false;
  export let statusTone;
  export let onSelectStatus;
  export let onToggleRole;
  export let onApprove;
  export let onReject;
</script>

<section class="panel admin">
  <div class="panel-head admin-head">
    <div>
      <h2>관리자 승인 화면</h2>
      <p>
        회원 상태별 목록을 보고 권한 부여, 승인, 반려, 탈퇴 이력 확인을
        처리합니다.
      </p>
    </div>
    <div class="tabs">
      {#each statusTabs as tab}
        <button
          class:selected={selectedStatus === tab}
          on:click={() => onSelectStatus(tab)}>{tab}</button
        >
      {/each}
    </div>
  </div>
  {#if users.length === 0}
    <p>현재 선택한 상태에 해당하는 회원이 없습니다.</p>
  {:else}
    <div class="user-grid">
      {#each users as user}
        <article class="user-card">
          <div class="user-card-head">
            <div class="user-cell">
              <strong>{user.displayName ?? user.email}</strong>
              <span>ID: {user.loginId ?? "-"}</span>
              <span>{user.email ?? user.providerUserId}</span>
            </div>
            <span class={`pill ${statusTone(user.status)}`}>{user.status}</span>
          </div>

          <div class="meta-grid">
            <div>
              <span>연락처</span>
              <strong>{user.contactNumber ?? "-"}</strong>
            </div>
            <div>
              <span>약관동의</span>
              <strong>{user.termsAgreedAt ?? "-"}</strong>
            </div>
            <div>
              <span>탈퇴시각</span>
              <strong>{user.withdrawnAt ?? "-"}</strong>
            </div>
            <div>
              <span>탈퇴사유</span>
              <strong>{user.withdrawalReason ?? "-"}</strong>
            </div>
          </div>

          <div class="roles-box">
            <h3>권한 선택</h3>
            <div class="role-grid">
              {#each assignableRoles as role}
                <label>
                  <input
                    type="checkbox"
                    checked={roleSelections[user.id]?.includes(role)}
                    disabled={busy}
                    on:change={() => onToggleRole(user.id, role)}
                  />
                  <span>{role}</span>
                </label>
              {/each}
            </div>
            <small class="role-help">권한은 최소 1개 이상 선택해야 승인됩니다.</small>
          </div>

          <div class="action-row">
            <button
              class="approve"
              disabled={busy || user.status === "WITHDRAWN"}
              on:click={() => onApprove(user.id)}>승인</button
            >
            <button
              class="reject"
              disabled={busy || user.status === "WITHDRAWN"}
              on:click={() => onReject(user.id)}>반려</button
            >
          </div>
        </article>
      {/each}
    </div>
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
  .admin {
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
  .admin-head {
    display: flex;
    justify-content: space-between;
    gap: 16px;
    align-items: flex-start;
  }
  .tabs {
    display: flex;
    gap: 10px;
    flex-wrap: wrap;
  }
  .tabs button {
    background: rgba(23, 33, 27, 0.06);
    color: #17211b;
    border: none;
    border-radius: 999px;
    padding: 10px 14px;
    font-weight: 700;
    cursor: pointer;
  }
  .tabs button.selected {
    background: #17211b;
    color: white;
  }
  .user-grid {
    margin-top: 18px;
    display: grid;
    gap: 14px;
  }
  .user-card {
    padding: 16px;
    border-radius: 20px;
    background: rgba(255, 255, 255, 0.88);
    border: 1px solid rgba(23, 33, 27, 0.08);
    display: grid;
    gap: 14px;
  }
  .user-card-head {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    gap: 12px;
  }
  .user-cell {
    display: grid;
    gap: 4px;
  }
  .user-cell span {
    color: #6d7c6e;
    font-size: 0.92rem;
  }
  .meta-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 10px;
  }
  .meta-grid div {
    padding: 12px;
    border-radius: 14px;
    background: rgba(248, 244, 235, 0.72);
    border: 1px solid rgba(23, 33, 27, 0.08);
  }
  .meta-grid span {
    display: block;
    font-size: 0.82rem;
    color: #667567;
    font-weight: 700;
  }
  .meta-grid strong {
    display: block;
    margin-top: 6px;
    font-size: 0.96rem;
    word-break: break-word;
  }
  .pill {
    display: inline-flex;
    align-items: center;
    padding: 8px 12px;
    border-radius: 999px;
    font-size: 0.82rem;
    font-weight: 800;
    color: white;
  }
  .pill.pending {
    background: #c78318;
  }
  .pill.active {
    background: #03c75a;
  }
  .pill.rejected {
    background: #4e5b53;
  }
  .pill.withdrawn {
    background: #9d2020;
  }
  .roles-box {
    padding: 12px;
    border-radius: 14px;
    background: rgba(248, 244, 235, 0.72);
    border: 1px solid rgba(23, 33, 27, 0.08);
  }
  .roles-box h3 {
    margin: 0 0 10px;
    font-size: 0.98rem;
  }
  .role-grid {
    display: grid;
    gap: 10px;
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .role-grid label {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    font-size: 0.92rem;
  }
  .role-help {
    display: inline-block;
    margin-top: 8px;
    color: #6d7c6e;
    font-size: 0.82rem;
  }
  .action-row {
    display: flex;
    gap: 10px;
    flex-wrap: wrap;
  }
  .approve,
  .reject {
    border: none;
    border-radius: 999px;
    padding: 10px 14px;
    font-weight: 700;
    color: white;
    cursor: pointer;
  }
  .approve {
    background: #03c75a;
  }
  .reject {
    background: #1e2b22;
  }
  @media (max-width: 980px) {
    .admin-head {
      flex-direction: column;
    }
    .meta-grid,
    .role-grid {
      grid-template-columns: 1fr;
    }
    .user-card-head {
      flex-direction: column;
    }
  }
</style>
