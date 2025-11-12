import { renderHook, waitFor } from '@testing-library/react';
import { usePayment } from '../usePayment';
import { paymentService } from '@/lib/services/paymentService';
import { handleApiError } from '@/lib/errorHandler';
import { loadTossPayments } from '@tosspayments/tosspayments-sdk';

jest.mock('@/lib/services/paymentService');
jest.mock('@/lib/errorHandler');
jest.mock('@tosspayments/tosspayments-sdk');

Object.defineProperty(globalThis, 'import', {
  value: {
    meta: {
      env: {
        VITE_TOSS_CLIENT_KEY: 'test-client-key',
        VITE_TOSS_CUSTOMER_KEY: 'test-customer-key',
      },
    },
  },
  writable: true,
});

describe('usePayment', () => {
  const mockRequestPayment = jest.fn();
  const mockPayment = jest.fn(() => ({
    requestPayment: mockRequestPayment,
  }));
  const mockTossPayments = {
    payment: mockPayment,
  };

  beforeEach(() => {
    jest.clearAllMocks();
    (loadTossPayments as jest.Mock).mockResolvedValue(mockTossPayments);
  });

  describe('requestPayment', () => {
    it('결제 초기화가 성공하면 토스 페이먼츠 결제를 요청해야 함', async () => {
      // given
      const eventId = 1;
      const amount = 50000;
      const customerInfo = {
        name: '테스트 사용자',
        email: 'test@example.com',
        mobilePhone: '010-1234-5678',
      };
      const mockInitResponse = {
        data: {
          purchaseUUID: 'order-uuid-12345',
        },
      };
      (paymentService.initPurchase as jest.Mock).mockResolvedValue(mockInitResponse);
      mockRequestPayment.mockResolvedValue(undefined);

      // when
      const { result } = renderHook(() => usePayment());
      await result.current.requestPayment(eventId, amount, customerInfo);

      // then
      await waitFor(() => {
        expect(paymentService.initPurchase).toHaveBeenCalledWith({
          eventId,
          amount,
        });
        expect(loadTossPayments).toHaveBeenCalledWith('test-client-key');
        expect(mockPayment).toHaveBeenCalledWith({
          customerKey: 'test-customer-key',
        });
        expect(mockRequestPayment).toHaveBeenCalledWith({
          method: 'CARD',
          amount: { currency: 'KRW', value: amount },
          orderId: 'order-uuid-12345',
          orderName: '예매 티켓',
          successUrl: 'http://localhost:3000/success',
          failUrl: 'http://localhost:3000/pay/fail',
          customerName: customerInfo.name,
          customerEmail: customerInfo.email,
          customerMobilePhone: customerInfo.mobilePhone,
        });
      });
    });

    it('결제 초기화 응답에 purchaseUUID가 없으면 에러를 설정해야 함', async () => {
      // given
      const eventId = 1;
      const amount = 50000;
      const customerInfo = {
        name: '테스트 사용자',
        email: 'test@example.com',
        mobilePhone: '010-1234-5678',
      };
      const mockInitResponse = {
        data: {},
      };
      (paymentService.initPurchase as jest.Mock).mockResolvedValue(mockInitResponse);

      // when
      const { result } = renderHook(() => usePayment());
      await result.current.requestPayment(eventId, amount, customerInfo);

      // then
      await waitFor(() => {
        expect(result.current.error).toBe('주문 초기화 응답이 올바르지 않습니다.');
      });
    });

    it('결제 초기화가 실패하면 에러를 설정해야 함', async () => {
      // given
      const eventId = 1;
      const amount = 50000;
      const customerInfo = {
        name: '테스트 사용자',
        email: 'test@example.com',
        mobilePhone: '010-1234-5678',
      };
      const error = new Error('결제 초기화 실패');
      const errorMessage = '결제 초기화에 실패했습니다.';
      (paymentService.initPurchase as jest.Mock).mockRejectedValue(error);
      (handleApiError as jest.Mock).mockReturnValue(errorMessage);

      // when
      const { result } = renderHook(() => usePayment());
      await result.current.requestPayment(eventId, amount, customerInfo);

      // then
      await waitFor(() => {
        expect(result.current.error).toBe(errorMessage);
      });
    });

    it('결제 요청 중에는 loading 상태가 true여야 함', async () => {
      // given
      const eventId = 1;
      const amount = 50000;
      const customerInfo = {
        name: '테스트 사용자',
        email: 'test@example.com',
        mobilePhone: '010-1234-5678',
      };
      let resolvePromise: (value: any) => void;
      const promise = new Promise((resolve) => {
        resolvePromise = resolve;
      });
      (paymentService.initPurchase as jest.Mock).mockReturnValue(promise);

      // when
      const { result } = renderHook(() => usePayment());
      result.current.requestPayment(eventId, amount, customerInfo);

      // then
      expect(result.current.loading).toBe(true);
      resolvePromise!({
        data: {
          purchaseUUID: 'order-uuid-12345',
        },
      });
      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });
    });

    it('TOSS_CUSTOMER_KEY가 없으면 anonymous를 사용해야 함', async () => {
      // given
      (globalThis as any).import.meta.env.VITE_TOSS_CUSTOMER_KEY = undefined;
      const eventId = 1;
      const amount = 50000;
      const customerInfo = {
        name: '테스트 사용자',
        email: 'test@example.com',
        mobilePhone: '010-1234-5678',
      };
      const mockInitResponse = {
        data: {
          purchaseUUID: 'order-uuid-12345',
        },
      };
      (paymentService.initPurchase as jest.Mock).mockResolvedValue(mockInitResponse);
      mockRequestPayment.mockResolvedValue(undefined);

      // when
      const { result } = renderHook(() => usePayment());
      await result.current.requestPayment(eventId, amount, customerInfo);

      // then
      await waitFor(() => {
        expect(mockPayment).toHaveBeenCalledWith({
          customerKey: 'anonymous',
        });
      });
      (globalThis as any).import.meta.env.VITE_TOSS_CUSTOMER_KEY = 'test-customer-key';
    });
  });

  describe('confirmPayment', () => {
    it('결제 승인이 성공해야 함', async () => {
      // given
      const paymentKey = 'payment-key-123';
      const orderId = 'order-id-123';
      const amount = 50000;
      (paymentService.confirmPurchase as jest.Mock).mockResolvedValue(undefined);

      // when
      const { result } = renderHook(() => usePayment());
      await result.current.confirmPayment(paymentKey, orderId, amount);

      // then
      await waitFor(() => {
        expect(paymentService.confirmPurchase).toHaveBeenCalledWith({
          paymentKey,
          orderId,
          orderName: '티켓 예매',
          amount,
        });
      });
    });

    it('결제 승인이 실패하면 에러를 설정해야 함', async () => {
      // given
      const paymentKey = 'invalid-key';
      const orderId = 'order-id-123';
      const amount = 50000;
      const error = new Error('결제 승인 실패');
      const errorMessage = '결제 승인에 실패했습니다.';
      (paymentService.confirmPurchase as jest.Mock).mockRejectedValue(error);
      (handleApiError as jest.Mock).mockReturnValue(errorMessage);

      // when
      const { result } = renderHook(() => usePayment());
      await result.current.confirmPayment(paymentKey, orderId, amount);

      // then
      await waitFor(() => {
        expect(result.current.error).toBe(errorMessage);
      });
    });

    it('결제 승인 중에는 loading 상태가 true여야 함', async () => {
      // given
      const paymentKey = 'payment-key-123';
      const orderId = 'order-id-123';
      const amount = 50000;
      let resolvePromise: (value: any) => void;
      const promise = new Promise((resolve) => {
        resolvePromise = resolve;
      });
      (paymentService.confirmPurchase as jest.Mock).mockReturnValue(promise);

      // when
      const { result } = renderHook(() => usePayment());
      result.current.confirmPayment(paymentKey, orderId, amount);

      // then
      expect(result.current.loading).toBe(true);
      resolvePromise!(undefined);
      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });
    });
  });
});

