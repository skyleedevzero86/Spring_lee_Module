import { useEffect, useState, useCallback } from 'react';
import { useSearchParams } from 'react-router-dom';
import { paymentService } from '@/lib/services/paymentService';
import styles from './PaySuccessPage.module.css';

type Status = 'loading' | 'success' | 'error';

export default function PaySuccessPage() {
  const [searchParams] = useSearchParams();
  const [status, setStatus] = useState<Status>('loading');
  const [error, setError] = useState<string | null>(null);

  const confirmPayment = useCallback(
    async (paymentKey: string, orderId: string, amount: number) => {
      if (!paymentKey?.trim() || !orderId?.trim() || amount <= 0) {
        const errorMessage = '결제 정보가 올바르지 않습니다.';
        setError(errorMessage);
        throw new Error(errorMessage);
      }

      try {
        await paymentService.confirmPurchase({
          paymentKey,
          orderId,
          orderName: '티켓 예매',
          amount,
        });
      } catch (err: unknown) {
        const errorMessage =
          (err as { response?: { data?: { message?: string } } })?.response?.data?.message || 
          '결제 승인에 실패했습니다.';
        setError(errorMessage);
        throw err;
      }
    },
    []
  );

  useEffect(() => {
    const paymentKey = searchParams.get('paymentKey');
    const orderId = searchParams.get('orderId');
    const amountStr = searchParams.get('amount');

    if (!paymentKey || !orderId || !amountStr) {
      setStatus('error');
      setError('필수 결제 정보가 누락되었습니다.');
      return;
    }

    const amount = Number.parseInt(amountStr, 10);
    if (Number.isNaN(amount) || amount <= 0) {
      setStatus('error');
      setError('결제 금액이 올바르지 않습니다.');
      return;
    }

    const confirm = async () => {
      try {
        await confirmPayment(paymentKey, orderId, amount);
        setStatus('success');
      } catch (err) {
        setStatus('error');
      }
    };

    confirm();
  }, [searchParams, confirmPayment]);

  return (
    <div className={styles.container}>
      {status === 'loading' && (
        <p className={styles.loadingText}>결제 승인 처리 중...</p>
      )}
      {status === 'success' && (
        <div className={styles.successContent}>
          <h1 className={styles.successTitle}>결제가 완료되었습니다</h1>
          <p className={styles.successMessage}>티켓이 정상적으로 발급되었습니다.</p>
        </div>
      )}
      {status === 'error' && (
        <div className={styles.errorContent}>
          <h1 className={styles.errorTitle}>결제 승인 실패</h1>
          <p className={styles.errorMessage}>결제 처리가 정상적으로 완료되지 않았습니다.</p>
          {error && <p className={styles.errorDetail}>{error}</p>}
        </div>
      )}
    </div>
  );
}

