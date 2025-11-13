import { paymentService } from './payment.service';
import { paymentApi } from '@/src/infrastructure/api/payment.api';
import { ApiError } from '@/src/domain/types/error.types';
import { PaymentStatus } from '@/src/domain/types/payment.types';
import type { CreatePaymentRequest, PaymentDetailResponse } from '@/src/domain/types/payment.types';
import type { Discount } from '@/src/domain/types/payment-calculation.types';

jest.mock('@/src/infrastructure/api/payment.api');
jest.mock('@/src/domain/payment-amount-calculator');
jest.mock('@/src/domain/payment-state-machine');
jest.mock('@/src/domain/payment-refund-calculator');
jest.mock('@/src/domain/payment-validator');

const mockPaymentApi = paymentApi as jest.Mocked<typeof paymentApi>;

describe('PaymentService', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('createPayment', () => {
    const mockRequest = {
      orderNo: 'ORDER-123',
      productDesc: '테스트 상품',
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

    it('결제 생성 성공', async () => {
      mockPaymentApi.createPayment.mockResolvedValue(mockResponse);

      const result = await paymentService.createPayment(mockRequest);

      expect(result).toEqual(mockResponse);
      expect(mockPaymentApi.createPayment).toHaveBeenCalledWith(mockRequest);
    });

    it('결제 생성 실패 시 ApiError 발생', async () => {
      const apiError = new ApiError('INVALID_AMOUNT', 400, '유효하지 않은 금액입니다.');
      mockPaymentApi.createPayment.mockRejectedValue(apiError);

      await expect(paymentService.createPayment(mockRequest)).rejects.toThrow(apiError);
    });

    it('결제 생성 시 검증 실패하면 에러 발생', async () => {
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

    it('결제 승인 성공', async () => {
      mockPaymentApi.approvePayment.mockResolvedValue(mockResponse);

      const result = await paymentService.approvePayment(mockRequest);

      expect(result).toEqual(mockResponse);
      expect(mockPaymentApi.approvePayment).toHaveBeenCalledWith(mockRequest);
    });

    it('결제 승인 시 현재 상태 검증', async () => {
      mockPaymentApi.approvePayment.mockResolvedValue(mockResponse);

      const result = await paymentService.approvePayment(mockRequest, PaymentStatus.PENDING);

      expect(result).toEqual(mockResponse);
    });

    it('잘못된 상태 전이 시 에러 발생', async () => {
      await expect(
        paymentService.approvePayment(mockRequest, PaymentStatus.COMPLETED)
      ).rejects.toThrow(ApiError);
    });

    it('이미 승인된 결제 승인 시도 시 에러', async () => {
      const apiError = new ApiError('ALREADY_APPROVED', 400, '이미 승인된 결제입니다.');
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

    it('결제 상태 조회 성공', async () => {
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

    it('결제 이력 조회 성공', async () => {
      mockPaymentApi.getPaymentHistory.mockResolvedValue(mockResponse);

      const result = await paymentService.getPaymentHistory();

      expect(result).toEqual(mockResponse);
      expect(result).toHaveLength(2);
      expect(mockPaymentApi.getPaymentHistory).toHaveBeenCalled();
    });

    it('결제 이력이 없을 때 빈 배열 반환', async () => {
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

    it('페이지네이션으로 결제 이력 조회 성공', async () => {
      mockPaymentApi.getPaymentHistoryPage.mockResolvedValue(mockResponse);

      const result = await paymentService.getPaymentHistoryPage(page, size);

      expect(result).toEqual(mockResponse);
      expect(result.content).toHaveLength(1);
      expect(mockPaymentApi.getPaymentHistoryPage).toHaveBeenCalledWith(page, size);
    });

    it('기본 페이지/사이즈 값 사용', async () => {
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
      productDesc: '테스트 상품',
      createdAt: '2024-01-01T00:00:00Z',
      approvedAt: '2024-01-01T00:01:00Z',
    };

    it('결제 상세 조회 성공', async () => {
      mockPaymentApi.getPaymentDetail.mockResolvedValue(mockResponse);

      const result = await paymentService.getPaymentDetail(paymentId);

      expect(result).toEqual(mockResponse);
      expect(mockPaymentApi.getPaymentDetail).toHaveBeenCalledWith(paymentId);
    });

    it('존재하지 않는 결제 조회 시 에러', async () => {
      const apiError = new ApiError('PAYMENT_NOT_FOUND', 404, '결제를 찾을 수 없습니다.');
      mockPaymentApi.getPaymentDetail.mockRejectedValue(apiError);

      await expect(paymentService.getPaymentDetail(paymentId)).rejects.toThrow(apiError);
    });
  });

  describe('refundPayment', () => {
    const paymentId = 1;
    const mockRequest = {
      refundNo: 'REFUND-123',
      reason: '고객 요청',
      amount: 10000,
    };

    const mockPaymentDetail: PaymentDetailResponse = {
      id: 1,
      userId: 1,
      orderNo: 'ORDER-123',
      productDesc: '테스트 상품',
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

    it('환불 처리 성공', async () => {
      mockPaymentApi.refundPayment.mockResolvedValue(mockResponse);

      const result = await paymentService.refundPayment(paymentId, mockRequest, mockPaymentDetail);

      expect(result).toEqual(mockResponse);
      expect(mockPaymentApi.refundPayment).toHaveBeenCalled();
    });

    it('환불 검증 실패 시 에러 발생', async () => {
      const invalidDetail = {
        ...mockPaymentDetail,
        status: PaymentStatus.PENDING,
      };

      await expect(
        paymentService.refundPayment(paymentId, mockRequest, invalidDetail)
      ).rejects.toThrow(ApiError);
    });

    it('이미 환불된 결제 환불 시도 시 에러', async () => {
      const apiError = new ApiError('ALREADY_REFUNDED', 400, '이미 환불된 결제입니다.');
      mockPaymentApi.refundPayment.mockRejectedValue(apiError);

      await expect(
        paymentService.refundPayment(paymentId, mockRequest, mockPaymentDetail)
      ).rejects.toThrow(apiError);
    });

    it('환불 가능 금액 초과 시 에러', async () => {
      const apiError = new ApiError('EXCEED_REFUNDABLE_AMOUNT', 400, '환불 가능 금액을 초과했습니다.');
      mockPaymentApi.refundPayment.mockRejectedValue(apiError);

      await expect(
        paymentService.refundPayment(paymentId, mockRequest, mockPaymentDetail)
      ).rejects.toThrow(apiError);
    });
  });

  describe('createPaymentWithDiscount', () => {
    const mockRequest: CreatePaymentRequest = {
      orderNo: 'ORDER-123',
      productDesc: '테스트 상품',
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
      productDesc: '테스트 상품',
      status: 'PENDING',
    };

    it('할인이 적용된 결제 생성 성공', async () => {
      mockPaymentApi.createPayment.mockResolvedValue(mockResponse);

      const result = await paymentService.createPaymentWithDiscount(mockRequest, discount);

      expect(result).toEqual(mockResponse);
      expect(mockPaymentApi.createPayment).toHaveBeenCalled();
    });
  });

  describe('calculatePaymentAmount', () => {
    it('결제 금액 계산 성공', () => {
      const result = paymentService.calculatePaymentAmount(10000, {
        taxFreeAmount: 2000,
      });

      expect(result.subtotal).toBe(10000);
      expect(result.taxFreeAmount).toBe(2000);
      expect(result.totalAmount).toBeGreaterThan(0);
    });
  });

  describe('validatePaymentStatusTransition', () => {
    it('유효한 상태 전이 검증', () => {
      const result = paymentService.validatePaymentStatusTransition(
        PaymentStatus.PENDING,
        PaymentStatus.APPROVED
      );

      expect(result).toBe(true);
    });

    it('무효한 상태 전이 검증', () => {
      const result = paymentService.validatePaymentStatusTransition(
        PaymentStatus.PENDING,
        PaymentStatus.COMPLETED
      );

      expect(result).toBe(false);
    });
  });

  describe('getAllowedStatusTransitions', () => {
    it('허용된 상태 전이 목록 조회', () => {
      const result = paymentService.getAllowedStatusTransitions(PaymentStatus.PENDING);

      expect(result).toContain(PaymentStatus.APPROVED);
      expect(result).toContain(PaymentStatus.CANCELLED);
      expect(result).toContain(PaymentStatus.FAILED);
    });
  });
});

