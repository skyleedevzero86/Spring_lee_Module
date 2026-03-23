<script>
  export let notifications = [];
  export let formatDate;
</script>

<section class="panel notification-panel">
  <div class="panel-head">
    <h2>관리자 알림</h2>
    <p>회원 탈퇴나 주요 상태 변경 알림을 최근 순서대로 보여줍니다.</p>
  </div>
  {#if notifications.length === 0}
    <p class="empty">현재 표시할 관리자 알림이 없습니다.</p>
  {:else}
    <div class="notification-list">
      {#each notifications as notification}
        <article>
          <strong>{notification.title}</strong>
          <p>{notification.message}</p>
          <span>{formatDate(notification.createdAt)}</span>
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
  .notification-panel {
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
  .notification-list {
    display: grid;
    gap: 12px;
    margin-top: 18px;
  }
  article {
    padding: 16px;
    border-radius: 20px;
    background: rgba(255, 255, 255, 0.88);
    border: 1px solid rgba(23, 33, 27, 0.08);
  }
  article strong {
    display: block;
    font-size: 1rem;
  }
  article p {
    margin: 8px 0 0;
    line-height: 1.7;
    color: #526253;
  }
  article span {
    display: inline-block;
    margin-top: 10px;
    font-size: 0.84rem;
    color: #708071;
  }
  .empty {
    margin: 18px 0 0;
    color: #526253;
  }
</style>
