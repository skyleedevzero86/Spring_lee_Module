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
    <div class="table-wrap">
      <table>
        <thead>
          <tr>
            <th>회원</th>
            <th>상태</th>
            <th>연락처</th>
            <th>권한</th>
            <th>이력</th>
            <th>액션</th>
          </tr>
        </thead>
        <tbody>
          {#each users as user}
            <tr>
              <td>
                <div class="user-cell">
                  <strong>{user.displayName ?? user.email}</strong>
                  <span>ID: {user.loginId ?? "-"}</span>
                  <span>{user.email ?? user.providerUserId}</span>
                </div>
              </td>
              <td
                ><span class={`pill ${statusTone(user.status)}`}
                  >{user.status}</span
                ></td
              >
              <td>{user.contactNumber ?? "-"}</td>
              <td>
                <div class="role-grid">
                  {#each assignableRoles as role}
                    <label>
                      <input
                        type="checkbox"
                        checked={roleSelections[user.id]?.includes(role)}
                        on:change={() => onToggleRole(user.id, role)}
                      />
                      <span>{role}</span>
                    </label>
                  {/each}
                </div>
              </td>
              <td>
                <div class="history-cell">
                  <span>약관동의: {user.termsAgreedAt ?? "-"}</span>
                  <span>탈퇴시각: {user.withdrawnAt ?? "-"}</span>
                  <span>탈퇴사유: {user.withdrawalReason ?? "-"}</span>
                </div>
              </td>
              <td>
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
              </td>
            </tr>
          {/each}
        </tbody>
      </table>
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
  .table-wrap {
    margin-top: 18px;
    overflow: auto;
  }
  table {
    width: 100%;
    border-collapse: collapse;
  }
  th,
  td {
    padding: 16px 12px;
    text-align: left;
    vertical-align: top;
    border-bottom: 1px solid rgba(23, 33, 27, 0.08);
  }
  .user-cell,
  .history-cell {
    display: grid;
    gap: 4px;
  }
  .user-cell span,
  .history-cell span {
    color: #6d7c6e;
    font-size: 0.92rem;
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
  .role-grid {
    display: grid;
    gap: 8px;
  }
  .role-grid label {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    font-size: 0.92rem;
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
  }
</style>
