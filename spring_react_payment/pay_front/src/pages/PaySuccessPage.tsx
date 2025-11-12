import { useEffect, useState, useCallback } from 'react';
import { useSearchParams } from 'react-router-dom';
import { paymentService } from '@/lib/services/paymentService';

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
    <div className="max-w-lg mx-auto mt-12 p-4 border rounded text-center">
      {status === 'loading' && (
        <p className="text-gray-600">결제 승인 처리 중...</p>
      )}
      {status === 'success' && (
        <>
          <h1 className="text-xl font-bold mb-2">결제가 완료되었습니다 🎉</h1>
          <p>티켓이 정상적으로 발급되었습니다.</p>
        </>
      )}
      {status === 'error' && (
        <>
          <h1 className="text-xl font-bold mb-2 text-red-600">
            결제 승인 실패
          </h1>
          <p>결제 처리가 정상적으로 완료되지 않았습니다.</p>
          {error && <p className="mt-2 text-sm text-red-500">{error}</p>}
        </>
      )}
    </div>
  );
}

