import api from '../../api';
import { paymentService } from '../paymentService';
import type { PurchaseInitRequest, PurchaseConfirmRequest } from '@/types/api';

jest.mock('../../api');

describe('paymentService', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('initPurchase', () => {
    it('결제 초기화 요청이 성공하면 응답 데이터를 반환해야 함', async () => {
      // given
      const request: PurchaseInitRequest = {
        eventId: 1,
        amount: 50000,
      };
      const mockResponse = {
        data: {
          purchaseUUID: 'order-uuid-12345',
        },
      };
      (api.post as jest.Mock).mockResolvedValue({ data: mockResponse });

      // when
      const result = await paymentService.initPurchase(request);

      // then
      expect(api.post).toHaveBeenCalledWith('/api/v1/purchase/init', request);
      expect(result).toEqual(mockResponse);
    });

    it('결제 초기화 요청이 실패하면 에러를 throw해야 함', async () => {
      // given
      const request: PurchaseInitRequest = {
        eventId: 1,
        amount: 50000,
      };
      const error = new Error('결제 초기화 실패');
      (api.post as jest.Mock).mockRejectedValue(error);

      // when & then
      await expect(paymentService.initPurchase(request)).rejects.toThrow('결제 초기화 실패');
    });
  });

  describe('confirmPurchase', () => {
    it('결제 승인 요청이 성공해야 함', async () => {
      // given
      const request: PurchaseConfirmRequest = {
        paymentKey: 'payment-key-123',
        orderId: 'order-id-123',
        orderName: '예매 티켓',
        amount: 50000,
      };
      (api.post as jest.Mock).mockResolvedValue({ data: {} });

      // when
      await paymentService.confirmPurchase(request);

      // then
      expect(api.post).toHaveBeenCalledWith('/api/v1/purchase/confirm', request);
    });

    it('결제 승인 요청이 실패하면 에러를 throw해야 함', async () => {
      // given
      const request: PurchaseConfirmRequest = {
        paymentKey: 'invalid-key',
        orderId: 'order-id-123',
        orderName: '예매 티켓',
        amount: 50000,
      };
      const error = new Error('결제 승인 실패');
      (api.post as jest.Mock).mockRejectedValue(error);

      // when & then
      await expect(paymentService.confirmPurchase(request)).rejects.toThrow('결제 승인 실패');
    });
  });

  describe('getUserOrders', () => {
    it('주문 목록 조회 요청이 성공하면 주문 목록을 반환해야 함', async () => {
      // given
      const mockOrders = [
        {
          orderId: 'order-1',
          orderName: '예매 티켓',
          amount: 50000,
          status: 'DONE',
          createdAt: '2024-01-01T00:00:00Z',
        },
        {
          orderId: 'order-2',
          orderName: '예매 티켓',
          amount: 30000,
          status: 'DONE',
          createdAt: '2024-01-02T00:00:00Z',
        },
      ];
      (api.get as jest.Mock).mockResolvedValue({ data: mockOrders });

      // when
      const result = await paymentService.getUserOrders();

      // then
      expect(api.get).toHaveBeenCalledWith('/api/v1/purchase/orders');
      expect(result).toEqual(mockOrders);
    });

    it('주문 목록 조회 요청이 실패하면 에러를 throw해야 함', async () => {
      // given
      const error = new Error('주문 목록 조회 실패');
      (api.get as jest.Mock).mockRejectedValue(error);

      // when & then
      await expect(paymentService.getUserOrders()).rejects.toThrow('주문 목록 조회 실패');
    });
  });

  describe('downloadReceipt', () => {
    it('영수증 다운로드 요청이 성공하면 Blob을 반환해야 함', async () => {
      // given
      const orderId = 'order-123';
      const mockBlob = new Blob(['PDF content'], { type: 'application/pdf' });
      (api.get as jest.Mock).mockResolvedValue({ data: mockBlob });

      // when
      const result = await paymentService.downloadReceipt(orderId);

      // then
      expect(api.get).toHaveBeenCalledWith(`/api/v1/purchase/receipt/${orderId}`, {
        responseType: 'blob',
      });
      expect(result).toBe(mockBlob);
    });

    it('영수증 다운로드 요청이 실패하면 에러를 throw해야 함', async () => {
      // given
      const orderId = 'invalid-order';
      const error = new Error('영수증 다운로드 실패');
      (api.get as jest.Mock).mockRejectedValue(error);

      // when & then
      await expect(paymentService.downloadReceipt(orderId)).rejects.toThrow('영수증 다운로드 실패');
    });
  });
});






