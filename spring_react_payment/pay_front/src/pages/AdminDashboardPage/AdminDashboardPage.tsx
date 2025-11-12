import { useState, useEffect } from 'react';
import type { OrderResponse, PaymentLogResponse } from '@/types/api';
import styles from './AdminDashboardPage.module.css';
import { handleSearch, loadAllOrders } from './utils';
import { adminService } from '@/lib/services/adminService';
import { handleApiError } from '@/lib/errorHandler';

export default function AdminDashboardPage() {
  const [orders, setOrders] = useState<OrderResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchParams, setSearchParams] = useState({
    orderId: '',
    memberId: '',
    status: '',
    startDate: '',
    endDate: '',
  });
  const [selectedOrderId, setSelectedOrderId] = useState<string | null>(null);
  const [logs, setLogs] = useState<PaymentLogResponse[]>([]);
  const [logsLoading, setLogsLoading] = useState(false);
  const [showLogModal, setShowLogModal] = useState(false);

  useEffect(() => {
    loadAllOrders(setOrders, setLoading, setError);
  }, []);

  const onSearch = async () => {
    await handleSearch(searchParams, setOrders, setLoading, setError);
  };

  const onLoadAll = async () => {
    await loadAllOrders(setOrders, setLoading, setError);
  };

  const handleOrderNameClick = async (orderId: string | null) => {
    if (!orderId) return;
    
    setSelectedOrderId(orderId);
    setShowLogModal(true);
    setLogsLoading(true);
    setError(null);

    try {
      const orderLogs = await adminService.getOrderLogs(orderId);
      setLogs(orderLogs);
    } catch (err: unknown) {
      setError(handleApiError(err));
    } finally {
      setLogsLoading(false);
    }
  };

  const closeModal = () => {
    setShowLogModal(false);
    setLogs([]);
    setSelectedOrderId(null);
  };

  if (loading && orders.length === 0) {
    return <div className={styles.loadingContainer}>로딩 중...</div>;
  }

  return (
    <div className={styles.container}>
      <h1 className={styles.title}>관리자 대시보드</h1>

      <div className={styles.searchSection}>
        <h2 className={styles.searchTitle}>결제 검색</h2>
        <div className={styles.searchGrid}>
          <div className={styles.searchField}>
            <label className={styles.label}>주문번호</label>
            <input
              type="text"
              className={styles.input}
              value={searchParams.orderId}
              onChange={(e) =>
                setSearchParams({ ...searchParams, orderId: e.target.value })
              }
            />
          </div>
          <div className={styles.searchField}>
            <label className={styles.label}>사용자 ID</label>
            <input
              type="number"
              className={styles.input}
              value={searchParams.memberId}
              onChange={(e) =>
                setSearchParams({ ...searchParams, memberId: e.target.value })
              }
            />
          </div>
          <div className={styles.searchField}>
            <label className={styles.label}>상태</label>
            <select
              className={styles.input}
              value={searchParams.status}
              onChange={(e) =>
                setSearchParams({ ...searchParams, status: e.target.value })
              }
            >
              <option value="">전체</option>
              <option value="PENDING">대기</option>
              <option value="DONE">완료</option>
              <option value="REFUNDED">환불됨</option>
              <option value="ABORTED">취소됨</option>
            </select>
          </div>
          <div className={styles.searchField}>
            <label className={styles.label}>시작일</label>
            <input
              type="datetime-local"
              className={styles.input}
              value={searchParams.startDate}
              onChange={(e) =>
                setSearchParams({ ...searchParams, startDate: e.target.value })
              }
            />
          </div>
          <div className={styles.searchField}>
            <label className={styles.label}>종료일</label>
            <input
              type="datetime-local"
              className={styles.input}
              value={searchParams.endDate}
              onChange={(e) =>
                setSearchParams({ ...searchParams, endDate: e.target.value })
              }
            />
          </div>
        </div>
        <div className={styles.buttonGroup}>
          <button onClick={onSearch} className={styles.searchButton}>
            검색
          </button>
          <button onClick={onLoadAll} className={styles.loadAllButton}>
            전체 조회
          </button>
        </div>
      </div>

      {error && <div className={styles.errorMessage}>{error}</div>}

      {orders.length === 0 ? (
        <p className={styles.emptyMessage}>결제 내역이 없습니다.</p>
      ) : (
        <div className={styles.tableWrapper}>
          <table className={styles.table}>
            <thead>
              <tr className={styles.tableHeader}>
                <th>아이디</th>
                <th>주문번호</th>
                <th>원본 주문번호</th>
                <th>주문명</th>
                <th>사용자 ID</th>
                <th>금액</th>
                <th>결제수단</th>
                <th>상태</th>
                <th>결제일시</th>
              </tr>
            </thead>
            <tbody>
              {orders.map((order) => (
                <tr key={order.id}>
                  <td>{order.id}</td>
                  <td>{order.orderId || '-'}</td>
                  <td>{order.originalOrderId || '-'}</td>
                  <td>
                    <button
                      type="button"
                      onClick={() => handleOrderNameClick(order.orderId || order.originalOrderId)}
                      className={styles.orderNameButton}
                      disabled={!order.orderId && !order.originalOrderId}
                    >
                      {order.orderName}
                    </button>
                  </td>
                  <td>{order.memberId}</td>
                  <td>{order.amount.toLocaleString()}원</td>
                  <td>{order.paymentMethodDisplay}</td>
                  <td>{order.statusDisplay}</td>
                  <td>{new Date(order.createdAt).toLocaleString('ko-KR')}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {showLogModal && (
        <div className={styles.modalOverlay} onClick={closeModal}>
          <div className={styles.modalContent} onClick={(e) => e.stopPropagation()}>
            <div className={styles.modalHeader}>
              <h2 className={styles.modalTitle}>주문 로그</h2>
              <button type="button" className={styles.closeButton} onClick={closeModal}>
                ×
              </button>
            </div>
            <div className={styles.modalBody}>
              {logsLoading ? (
                <div className={styles.loadingText}>로딩 중...</div>
              ) : logs.length === 0 ? (
                <div className={styles.emptyText}>로그가 없습니다.</div>
              ) : (
                <table className={styles.logTable}>
                  <thead>
                    <tr>
                      <th>시간</th>
                      <th>메시지</th>
                    </tr>
                  </thead>
                  <tbody>
                    {logs.map((log) => (
                      <tr key={log.id}>
                        <td>{new Date(log.createdAt).toLocaleString('ko-KR')}</td>
                        <td>{log.message}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

