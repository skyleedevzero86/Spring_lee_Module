import { useState, useEffect } from 'react';
import { paymentService } from '@/lib/services/paymentService';
import type { OrderResponse } from '@/types/api';
import styles from './MyOrdersPage.module.css';
import { downloadReceipt } from './utils';
import { useAuthStore } from '@/store/authStore';

export default function MyOrdersPage() {
  const { isAdmin } = useAuthStore();
  const [orders, setOrders] = useState<OrderResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [receiptError, setReceiptError] = useState<string | null>(null);
  const [refundError, setRefundError] = useState<string | null>(null);
  const [refundingOrderId, setRefundingOrderId] = useState<string | null>(null);
  const isUserAdmin = isAdmin();

  useEffect(() => {
    loadOrders();
  }, []);

  const loadOrders = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await paymentService.getUserOrders();
      setOrders(data);
    } catch (err: unknown) {
      let errorMessage = '주문 목록을 불러오는데 실패했습니다.';
      if (err instanceof Error && 'response' in err) {
        const axiosError = err as { response?: { data?: { message?: string } } };
        errorMessage = axiosError.response?.data?.message || errorMessage;
      }
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const handleDownloadReceipt = async (orderId: string) => {
    try {
      setReceiptError(null);
      await downloadReceipt(orderId);
    } catch (err: unknown) {
      let errorMessage = '영수증 다운로드에 실패했습니다.';
      if (err instanceof Error && 'response' in err) {
        const axiosError = err as { response?: { data?: { message?: string } } };
        errorMessage = axiosError.response?.data?.message || errorMessage;
      }
      setReceiptError(errorMessage);
    }
  };

  const handleRefund = async (order: OrderResponse) => {
    if (!order.paymentKey || !order.orderId) {
      setRefundError('환불 정보가 없습니다.');
      return;
    }

    if (!confirm('정말 환불하시겠습니까?')) {
      return;
    }

    try {
      setRefundError(null);
      setRefundingOrderId(order.orderId);
      await paymentService.refundOrder({
        paymentKey: order.paymentKey,
        orderId: order.orderId,
        refundReason: '고객 요청',
        paidAmount: order.amount,
      });
      await loadOrders();
      alert('환불이 완료되었습니다.');
    } catch (err: unknown) {
      let errorMessage = '환불 처리에 실패했습니다.';
      if (err instanceof Error && 'response' in err) {
        const axiosError = err as { response?: { data?: { message?: string } } };
        errorMessage = axiosError.response?.data?.message || errorMessage;
      }
      setRefundError(errorMessage);
    } finally {
      setRefundingOrderId(null);
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
      {receiptError && (
        <div className={styles.errorContainer}>{receiptError}</div>
      )}
      {refundError && (
        <div className={styles.errorContainer}>{refundError}</div>
      )}
      {orders.length === 0 ? (
        <p className={styles.emptyMessage}>결제 내역이 없습니다.</p>
      ) : (
        <div className={styles.tableWrapper}>
          <table className={styles.table}>
            <thead>
              <tr className={styles.tableHeader}>
                <th>주문번호</th>
                {isUserAdmin && <th>원본 주문번호</th>}
                <th>주문명</th>
                <th>금액</th>
                <th>결제수단</th>
                <th>상태</th>
                <th>결제일시</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {orders.map((order) => (
                <tr key={order.id}>
                  <td>{order.orderId || '-'}</td>
                  {isUserAdmin && <td>{order.originalOrderId || '-'}</td>}
                  <td>{order.orderName}</td>
                  <td>{order.amount.toLocaleString()}원</td>
                  <td>{order.paymentMethodDisplay}</td>
                  <td>{order.statusDisplay}</td>
                  <td>{new Date(order.createdAt).toLocaleString('ko-KR')}</td>
                  <td>
                    <div className={styles.actionButtons}>
                      {order.status === 'DONE' && (
                        <>
                          <button
                            onClick={() => handleDownloadReceipt(order.orderId || order.originalOrderId || '')}
                            className={styles.downloadButton}
                          >
                            영수증
                          </button>
                          <button
                            onClick={() => handleRefund(order)}
                            className={styles.refundButton}
                            disabled={refundingOrderId === order.orderId}
                          >
                            {refundingOrderId === order.orderId ? '처리중...' : '환불'}
                          </button>
                        </>
                      )}
                    </div>
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

