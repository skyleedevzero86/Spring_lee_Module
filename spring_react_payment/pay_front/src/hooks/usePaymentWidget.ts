import { useEffect, useRef, useState, useCallback } from 'react';
import {
  loadPaymentWidget,
  PaymentWidgetInstance,
  ANONYMOUS,
} from '@tosspayments/payment-widget-sdk';
import { nanoid } from 'nanoid';
import { AxiosError } from 'axios';
import { paymentService } from '@/lib/services/paymentService';

const CLIENT_KEY = import.meta.env.VITE_TOSS_CLIENT_KEY;
const CUSTOMER_KEY = import.meta.env.VITE_TOSS_CUSTOMER_KEY || ANONYMOUS;

if (!CLIENT_KEY) {
  throw new Error('VITE_TOSS_CLIENT_KEY 환경 변수가 설정되지 않았습니다.');
}

export const usePaymentWidget = () => {
  const paymentWidgetRef = useRef<PaymentWidgetInstance | null>(null);
  const paymentMethodsWidgetRef = useRef<
    ReturnType<PaymentWidgetInstance['renderPaymentMethods']> | null
  >(null);
  const [price, setPrice] = useState(50_000);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [isWidgetReady, setIsWidgetReady] = useState(false);

  useEffect(() => {
    let isMounted = true;

    const initWidget = async () => {
      try {
        setError(null);
        const paymentWidget = await loadPaymentWidget(CLIENT_KEY, CUSTOMER_KEY);
        
        if (!isMounted) return;

        const paymentMethodsWidget = paymentWidget.renderPaymentMethods(
          '#payment-widget',
          price
        );

        paymentWidgetRef.current = paymentWidget;
        paymentMethodsWidgetRef.current = paymentMethodsWidget;
        setIsWidgetReady(true);
        setError(null);
      } catch (err: unknown) {
        let errorMessage = '결제위젯을 불러올 수 없습니다.';
        
        if (err instanceof Error) {
          if (err.message?.includes('401') || err.message?.includes('Unauthorized')) {
            errorMessage = '인증에 실패했습니다. 클라이언트 키를 확인해주세요.';
          } else if (err.message?.includes('Network')) {
            errorMessage = '네트워크 오류가 발생했습니다. 인터넷 연결을 확인해주세요.';
          } else {
            errorMessage = `결제위젯 오류: ${err.message}`;
          }
        }
        
        setError(errorMessage);
        setIsWidgetReady(false);
      }
    };

    initWidget();

    return () => {
      isMounted = false;
    };
  }, []);

  useEffect(() => {
    const paymentMethodsWidget = paymentMethodsWidgetRef.current;

    if (paymentMethodsWidget == null || !isWidgetReady) {
      return;
    }

    paymentMethodsWidget.updateAmount(
      price,
      paymentMethodsWidget.UPDATE_REASON.COUPON
    );
  }, [price, isWidgetReady]);

  const requestPayment = useCallback(
    async (
      eventId: number,
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
          amount: price,
        });

        const purchaseId = initResponse.data.purchaseUUID;
        const orderId = nanoid();

        const paymentWidget = paymentWidgetRef.current;

        if (!paymentWidget) {
          throw new Error('결제위젯이 초기화되지 않았습니다.');
        }

        await paymentWidget.requestPayment({
          orderId,
          orderName: '예매 티켓',
          customerName: customerInfo.name,
          customerEmail: customerInfo.email,
          successUrl: `${window.location.origin}/success?paymentUUID=${purchaseId}&orderId=${orderId}`,
          failUrl: `${window.location.origin}/pay/fail`,
        });
      } catch (err: unknown) {
        let errorMessage = '결제를 시작할 수 없습니다.';

        if (err instanceof AxiosError) {
          errorMessage =
            err.response?.data?.message ||
            `서버 오류: ${err.response?.status} ${err.response?.statusText}`;
        } else if (err instanceof Error) {
          errorMessage = err.message || '결제 요청 중 오류가 발생했습니다.';
        }

        setError(errorMessage);
        throw err;
      } finally {
        setLoading(false);
      }
    },
    [price]
  );

  return {
    requestPayment,
    price,
    setPrice,
    loading,
    error,
    isWidgetReady,
  };
};

