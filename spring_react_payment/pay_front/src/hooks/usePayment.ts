import { useState, useCallback } from 'react';
import { loadTossPayments } from '@tosspayments/tosspayments-sdk';
import { paymentService } from '@/lib/services/paymentService';
import { handleApiError } from '@/lib/errorHandler';

const TOSS_CLIENT_KEY = import.meta.env.VITE_TOSS_CLIENT_KEY;
const TOSS_CUSTOMER_KEY = import.meta.env.VITE_TOSS_CUSTOMER_KEY;

if (!TOSS_CLIENT_KEY) {
  throw new Error('VITE_TOSS_CLIENT_KEY 환경 변수가 설정되지 않았습니다.');
}

export const usePayment = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const requestPayment = useCallback(
    async (
      eventId: number,
      amount: number,
      customerInfo: {
        name: string;
        email: string;
        mobilePhone: string;
      }
    ) => {
      setLoading(true);
      setError(null);

      try {
        const initResponse = await paymentService.initPurchase({
          eventId,
          amount,
        });

        if (!initResponse?.data?.purchaseUUID) {
          throw new Error('주문 초기화 응답이 올바르지 않습니다.');
        }

        const purchaseId = initResponse.data.purchaseUUID;

        const tossPayments = await loadTossPayments(TOSS_CLIENT_KEY);
        const payment = tossPayments.payment({
          customerKey: TOSS_CUSTOMER_KEY || 'anonymous',
        });

        const paymentParams = {
          method: 'CARD' as const,
          amount: { currency: 'KRW' as const, value: amount },
          orderId: String(purchaseId),
          orderName: '예매 티켓',
          successUrl: `${window.location.origin}/success`,
          failUrl: `${window.location.origin}/pay/fail`,
          customerName: customerInfo.name,
          customerEmail: customerInfo.email,
          customerMobilePhone: customerInfo.mobilePhone,
        };

        await payment.requestPayment(paymentParams);
      } catch (err: unknown) {
        const errorMessage = handleApiError(err) || '결제를 시작할 수 없습니다.';
        setError(errorMessage);
        return;
      } finally {
        setLoading(false);
      }
    },
    []
  );

  const confirmPayment = useCallback(
    async (paymentKey: string, orderId: string, amount: number) => {
      setLoading(true);
      setError(null);

      try {
        await paymentService.confirmPurchase({
          paymentKey,
          orderId,
          orderName: '티켓 예매',
          amount,
        });
      } catch (err: unknown) {
        const errorMessage = handleApiError(err) || '결제 승인에 실패했습니다.';
        setError(errorMessage);
        return;
      } finally {
        setLoading(false);
      }
    },
    []
  );

  return {
    requestPayment,
    confirmPayment,
    loading,
    error,
  };
};

