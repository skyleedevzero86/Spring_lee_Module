import { useState, useCallback } from 'react';
import { loadTossPayments } from '@tosspayments/tosspayments-sdk';
import { paymentService } from '@/lib/services/paymentService';

const TOSS_CLIENT_KEY = import.meta.env.VITE_TOSS_CLIENT_KEY || 'test_ck_Z61JOxRQVEY6lZeGL4zgVW0X9bAq';

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

        const purchaseId = initResponse.data.purchaseUUID;

        const tossPayments = await loadTossPayments(TOSS_CLIENT_KEY);
        const payment = tossPayments.payment({
          customerKey: 'tvivarepublica',
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
    } catch (err: any) {
      let errorMessage = '결제를 시작할 수 없습니다.';
      
      if (err.response) {
        errorMessage = err.response.data?.message || 
          `서버 오류: ${err.response.status} ${err.response.statusText}`;
      } else if (err.request) {
        errorMessage = '서버에 연결할 수 없습니다. 서버가 실행 중인지 확인해주세요.';
      } else {
        errorMessage = err.message || '결제 요청 중 오류가 발생했습니다.';
      }
      
      setError(errorMessage);
      throw err;
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

