import { paymentService } from '../payment.service';
import { paymentApi } from '@/infrastructure/api/payment.api';
import { ApiError } from '@/domain/types/error.types';
import { PaymentStatus } from '@/domain/types/payment.types';
import type { CreatePaymentRequest, PaymentDetailResponse } from '@/domain/types/payment.types';
import type { Discount } from '@/domain/types/payment-calculation.types';

jest.mock('@/infrastructure/api/payment.api');
jest.mock('@/domain/payment-amount-calculator');
jest.mock('@/domain/payment-state-machine');
jest.mock('@/domain/payment-refund-calculator');
jest.mock('@/domain/payment-validator');

const mockPaymentApi = paymentApi as jest.Mocked<typeof paymentApi>;

describe('PaymentService', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('createPayment', () => {
    const mockRequest = {
      orderNo: 'ORDER-123',
      productDesc: '?ŒìŠ¤???í’ˆ',
      amount: 10000,
      amountTaxFree: 0,
      retUrl: 'http://localhost:3000/success',
      retCancelUrl: 'http://localhost:3000/cancel',
    };

    const mockResponse = {
      paymentId: 1,
      checkoutPage: 'http://checkout.example.com',
      orderNo: 'ORDER-123',
    };

    it('ê²°ì œ ?ì„± ?±ê³µ', async () => {
      mockPaymentApi.createPayment.mockResolvedValue(mockResponse);

      const result = await paymentService.createPayment(mockRequest);

      expect(result).toEqual(mockResponse);
      expect(mockPaymentApi.createPayment).toHaveBeenCalledWith(mockRequest);
    });

    it('ê²°ì œ ?ì„± ?¤íŒ¨ ??ApiError ë°œìƒ', async () => {
      const apiError = new ApiError('INVALID_AMOUNT', 400, '? íš¨?˜ì? ?Šì? ê¸ˆì•¡?…ë‹ˆ??');
      mockPaymentApi.createPayment.mockRejectedValue(apiError);

      await expect(paymentService.createPayment(mockRequest)).rejects.toThrow(apiError);
    });

    it('ê²°ì œ ?ì„± ??ê²€ì¦??¤íŒ¨?˜ë©´ ?ëŸ¬ ë°œìƒ', async () => {
      const invalidRequest = {
        ...mockRequest,
        orderNo: '',
      };

      await expect(paymentService.createPayment(invalidRequest)).rejects.toThrow(ApiError);
    });
  });

  describe('approvePayment', () => {
    const mockRequest = {
      paymentId: 1,
      paymentKey: 'payment-key-123',
      orderNo: 'ORDER-123',
      amount: 10000,
    };

    const mockResponse = {
      paymentId: 1,
      status: 'APPROVED',
      approvedAt: '2024-01-01T00:00:00Z',
    };

    it('ê²°ì œ ?¹ì¸ ?±ê³µ', async () => {
      mockPaymentApi.approvePayment.mockResolvedValue(mockResponse);

      const result = await paymentService.approvePayment(mockRequest);

      expect(result).toEqual(mockResponse);
      expect(mockPaymentApi.approvePayment).toHaveBeenCalledWith(mockRequest);
    });

    it('ê²°ì œ ?¹ì¸ ???„ìž¬ ?íƒœ ê²€ì¦?, async () => {
      mockPaymentApi.approvePayment.mockResolvedValue(mockResponse);

      const result = await paymentService.approvePayment(mockRequest, PaymentStatus.PENDING);

      expect(result).toEqual(mockResponse);
    });

    it('?˜ëª»???íƒœ ?„ì´ ???ëŸ¬ ë°œìƒ', async () => {
      await expect(
        paymentService.approvePayment(mockRequest, PaymentStatus.COMPLETED)
      ).rejects.toThrow(ApiError);
    });

    it('?´ë? ?¹ì¸??ê²°ì œ ?¹ì¸ ?œë„ ???ëŸ¬', async () => {
      const apiError = new ApiError('ALREADY_APPROVED', 400, '?´ë? ?¹ì¸??ê²°ì œ?…ë‹ˆ??');
      mockPaymentApi.approvePayment.mockRejectedValue(apiError);

      await expect(paymentService.approvePayment(mockRequest)).rejects.toThrow(apiError);
    });
  });

  describe('getPaymentStatus', () => {
    const mockRequest = {
      paymentId: 1,
    };

    const mockResponse = {
      paymentId: 1,
      status: 'APPROVED',
      amount: 10000,
      orderNo: 'ORDER-123',
    };

    it('ê²°ì œ ?íƒœ ì¡°íšŒ ?±ê³µ', async () => {
      mockPaymentApi.getPaymentStatus.mockResolvedValue(mockResponse);

      const result = await paymentService.getPaymentStatus(mockRequest);

      expect(result).toEqual(mockResponse);
      expect(mockPaymentApi.getPaymentStatus).toHaveBeenCalledWith(mockRequest);
    });
  });

  describe('getPaymentHistory', () => {
    const mockResponse = [
      {
        paymentId: 1,
        orderNo: 'ORDER-123',
        amount: 10000,
        status: 'APPROVED',
        createdAt: '2024-01-01T00:00:00Z',
      },
      {
        paymentId: 2,
        orderNo: 'ORDER-456',
        amount: 20000,
        status: 'PENDING',
        createdAt: '2024-01-02T00:00:00Z',
      },
    ];

    it('ê²°ì œ ?´ë ¥ ì¡°íšŒ ?±ê³µ', async () => {
      mockPaymentApi.getPaymentHistory.mockResolvedValue(mockResponse);

      const result = await paymentService.getPaymentHistory();

      expect(result).toEqual(mockResponse);
      expect(result).toHaveLength(2);
      expect(mockPaymentApi.getPaymentHistory).toHaveBeenCalled();
    });

    it('ê²°ì œ ?´ë ¥???†ì„ ??ë¹?ë°°ì—´ ë°˜í™˜', async () => {
      mockPaymentApi.getPaymentHistory.mockResolvedValue([]);

      const result = await paymentService.getPaymentHistory();

      expect(result).toEqual([]);
      expect(result).toHaveLength(0);
    });
  });

  describe('getPaymentHistoryPage', () => {
    const page = 0;
    const size = 20;
    const mockResponse = {
      content: [
        {
          paymentId: 1,
          orderNo: 'ORDER-123',
          amount: 10000,
          status: 'APPROVED',
        },
      ],
      page: 0,
      size: 20,
      totalElements: 1,
      totalPages: 1,
    };

    it('?˜ì´ì§€?¤ì´?˜ìœ¼ë¡?ê²°ì œ ?´ë ¥ ì¡°íšŒ ?±ê³µ', async () => {
      mockPaymentApi.getPaymentHistoryPage.mockResolvedValue(mockResponse);

      const result = await paymentService.getPaymentHistoryPage(page, size);

      expect(result).toEqual(mockResponse);
      expect(result.content).toHaveLength(1);
      expect(mockPaymentApi.getPaymentHistoryPage).toHaveBeenCalledWith(page, size);
    });

    it('ê¸°ë³¸ ?˜ì´ì§€/?¬ì´ì¦?ê°??¬ìš©', async () => {
      mockPaymentApi.getPaymentHistoryPage.mockResolvedValue(mockResponse);

      await paymentService.getPaymentHistoryPage();

      expect(mockPaymentApi.getPaymentHistoryPage).toHaveBeenCalledWith(0, 20);
    });
  });

  describe('getPaymentDetail', () => {
    const paymentId = 1;
    const mockResponse = {
      paymentId: 1,
      orderNo: 'ORDER-123',
      amount: 10000,
      status: 'APPROVED',
      productDesc: '?ŒìŠ¤???í’ˆ',
      createdAt: '2024-01-01T00:00:00Z',
      approvedAt: '2024-01-01T00:01:00Z',
    };

    it('ê²°ì œ ?ì„¸ ì¡°íšŒ ?±ê³µ', async () => {
      mockPaymentApi.getPaymentDetail.mockResolvedValue(mockResponse);

      const result = await paymentService.getPaymentDetail(paymentId);

      expect(result).toEqual(mockResponse);
      expect(mockPaymentApi.getPaymentDetail).toHaveBeenCalledWith(paymentId);
    });

    it('ì¡´ìž¬?˜ì? ?ŠëŠ” ê²°ì œ ì¡°íšŒ ???ëŸ¬', async () => {
      const apiError = new ApiError('PAYMENT_NOT_FOUND', 404, 'ê²°ì œë¥?ì°¾ì„ ???†ìŠµ?ˆë‹¤.');
      mockPaymentApi.getPaymentDetail.mockRejectedValue(apiError);

      await expect(paymentService.getPaymentDetail(paymentId)).rejects.toThrow(apiError);
    });
  });

  describe('refundPayment', () => {
    const paymentId = 1;
    const mockRequest = {
      refundNo: 'REFUND-123',
      reason: 'ê³ ê° ?”ì²­',
      amount: 10000,
    };

    const mockPaymentDetail: PaymentDetailResponse = {
      id: 1,
      userId: 1,
      orderNo: 'ORDER-123',
      productDesc: '?ŒìŠ¤???í’ˆ',
      amount: 10000,
      amountTaxFree: 0,
      amountTaxable: 10000,
      amountVat: 1000,
      amountServiceFee: 300,
      status: PaymentStatus.APPROVED,
      paidAmount: 11300,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
      expiredTime: new Date().toISOString(),
    };

    const mockResponse = {
      paymentId: 1,
      refundNo: 'REFUND-123',
      refundableAmount: 11300,
      refundedAmount: 10000,
    };

    it('?˜ë¶ˆ ì²˜ë¦¬ ?±ê³µ', async () => {
      mockPaymentApi.refundPayment.mockResolvedValue(mockResponse);

      const result = await paymentService.refundPayment(paymentId, mockRequest, mockPaymentDetail);

      expect(result).toEqual(mockResponse);
      expect(mockPaymentApi.refundPayment).toHaveBeenCalled();
    });

    it('?˜ë¶ˆ ê²€ì¦??¤íŒ¨ ???ëŸ¬ ë°œìƒ', async () => {
      const invalidDetail = {
        ...mockPaymentDetail,
        status: PaymentStatus.PENDING,
      };

      await expect(
        paymentService.refundPayment(paymentId, mockRequest, invalidDetail)
      ).rejects.toThrow(ApiError);
    });

    it('?´ë? ?˜ë¶ˆ??ê²°ì œ ?˜ë¶ˆ ?œë„ ???ëŸ¬', async () => {
      const apiError = new ApiError('ALREADY_REFUNDED', 400, '?´ë? ?˜ë¶ˆ??ê²°ì œ?…ë‹ˆ??');
      mockPaymentApi.refundPayment.mockRejectedValue(apiError);

      await expect(
        paymentService.refundPayment(paymentId, mockRequest, mockPaymentDetail)
      ).rejects.toThrow(apiError);
    });

    it('?˜ë¶ˆ ê°€??ê¸ˆì•¡ ì´ˆê³¼ ???ëŸ¬', async () => {
      const apiError = new ApiError('EXCEED_REFUNDABLE_AMOUNT', 400, '?˜ë¶ˆ ê°€??ê¸ˆì•¡??ì´ˆê³¼?ˆìŠµ?ˆë‹¤.');
      mockPaymentApi.refundPayment.mockRejectedValue(apiError);

      await expect(
        paymentService.refundPayment(paymentId, mockRequest, mockPaymentDetail)
      ).rejects.toThrow(apiError);
    });
  });

  describe('createPaymentWithDiscount', () => {
    const mockRequest: CreatePaymentRequest = {
      orderNo: 'ORDER-123',
      productDesc: '?ŒìŠ¤???í’ˆ',
      amount: 10000,
      amountTaxFree: 0,
      retUrl: 'http://localhost:3000/success',
      retCancelUrl: 'http://localhost:3000/cancel',
    };

    const discount: Discount = {
      type: 'PERCENTAGE',
      value: 10,
    };

    const mockResponse = {
      id: 1,
      orderNo: 'ORDER-123',
      payToken: 'token-123',
      checkoutPage: 'http://checkout.example.com',
      productDesc: '?ŒìŠ¤???í’ˆ',
      status: 'PENDING',
    };

    it('? ì¸???ìš©??ê²°ì œ ?ì„± ?±ê³µ', async () => {
      mockPaymentApi.createPayment.mockResolvedValue(mockResponse);

      const result = await paymentService.createPaymentWithDiscount(mockRequest, discount);

      expect(result).toEqual(mockResponse);
      expect(mockPaymentApi.createPayment).toHaveBeenCalled();
    });
  });

  describe('calculatePaymentAmount', () => {
    it('ê²°ì œ ê¸ˆì•¡ ê³„ì‚° ?±ê³µ', () => {
      const result = paymentService.calculatePaymentAmount(10000, {
        taxFreeAmount: 2000,
      });

      expect(result.subtotal).toBe(10000);
      expect(result.taxFreeAmount).toBe(2000);
      expect(result.totalAmount).toBeGreaterThan(0);
    });
  });

  describe('validatePaymentStatusTransition', () => {
    it('? íš¨???íƒœ ?„ì´ ê²€ì¦?, () => {
      const result = paymentService.validatePaymentStatusTransition(
        PaymentStatus.PENDING,
        PaymentStatus.APPROVED
      );

      expect(result).toBe(true);
    });

    it('ë¬´íš¨???íƒœ ?„ì´ ê²€ì¦?, () => {
      const result = paymentService.validatePaymentStatusTransition(
        PaymentStatus.PENDING,
        PaymentStatus.COMPLETED
      );

      expect(result).toBe(false);
    });
  });

  describe('getAllowedStatusTransitions', () => {
    it('?ˆìš©???íƒœ ?„ì´ ëª©ë¡ ì¡°íšŒ', () => {
      const result = paymentService.getAllowedStatusTransitions(PaymentStatus.PENDING);

      expect(result).toContain(PaymentStatus.APPROVED);
      expect(result).toContain(PaymentStatus.CANCELLED);
      expect(result).toContain(PaymentStatus.FAILED);
    });
  });
});

