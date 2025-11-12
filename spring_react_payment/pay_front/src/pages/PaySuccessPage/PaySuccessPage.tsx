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
      try {
        const result = await paymentService.confirmPurchase({
          paymentKey,
          orderId,
          orderName: '티켓 예매',
          amount,
        });
        return result;
      } catch (err: any) {
        const errorMessage =
          err.response?.data?.message || '결제 승인에 실패했습니다.';
        setError(errorMessage);
        throw err;
      }
    },
    []
  );

  useEffect(() => {
    const paymentKey = searchParams.get('paymentKey');
    const orderId = searchParams.get('orderId');
    const amount = searchParams.get('amount');

    if (!paymentKey || !orderId || !amount) {
      setStatus('error');
      setError('필수 결제 정보가 누락되었습니다.');
      return;
    }

    const confirm = async () => {
      try {
        await confirmPayment(paymentKey, orderId, parseInt(amount));
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

