import { useState, useEffect } from 'react';
import { paymentService } from '@/lib/services/paymentService';
import type { OrderResponse } from '@/types/api';
import styles from './MyOrdersPage.module.css';
import { downloadReceipt } from './utils';

export default function MyOrdersPage() {
  const [orders, setOrders] = useState<OrderResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadOrders();
  }, []);

  const loadOrders = async () => {
    try {
      setLoading(true);
      const data = await paymentService.getUserOrders();
      setOrders(data);
    } catch (err: any) {
      setError(err.response?.data?.message || '주문 목록을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleDownloadReceipt = async (orderId: string) => {
    try {
      await downloadReceipt(orderId);
    } catch (err: any) {
      alert(err.response?.data?.message || '영수증 다운로드에 실패했습니다.');
    }
  };

  if (loading) {
    return <div className={styles.loadingContainer}>로딩 중...</div>;
  }

  if (error) {
    return <div className={styles.errorContainer}>{error}</div>;
  }

  return (
    <div className={styles.container}>
      <h1 className={styles.title}>내 결제 내역</h1>
      {orders.length === 0 ? (
        <p className={styles.emptyMessage}>결제 내역이 없습니다.</p>
      ) : (
        <div className={styles.tableWrapper}>
          <table className={styles.table}>
            <thead>
              <tr className={styles.tableHeader}>
                <th>주문번호</th>
                <th>주문명</th>
                <th>금액</th>
                <th>결제수단</th>
                <th>상태</th>
                <th>결제일시</th>
                <th>작업</th>
              </tr>
            </thead>
            <tbody>
              {orders.map((order) => (
                <tr key={order.id}>
                  <td>{order.orderId}</td>
                  <td>{order.orderName}</td>
                  <td>{order.amount.toLocaleString()}원</td>
                  <td>{order.paymentMethod || '-'}</td>
                  <td>{order.status}</td>
                  <td>{new Date(order.createdAt).toLocaleString('ko-KR')}</td>
                  <td>
                    {order.status === 'DONE' && (
                      <button
                        onClick={() => handleDownloadReceipt(order.orderId)}
                        className={styles.downloadButton}
                      >
                        영수증 다운로드
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

