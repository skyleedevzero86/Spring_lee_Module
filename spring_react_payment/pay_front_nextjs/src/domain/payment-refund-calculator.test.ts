import { PaymentRefundCalculator } from './payment-refund-calculator';
import { PaymentStatus } from './types/payment.types';
import type {
  RefundRequest,
  PaymentAmountBreakdown,
  RefundPolicy,
} from './types/payment-calculation.types';

describe('PaymentRefundCalculator', () => {
  let calculator: PaymentRefundCalculator;
  let defaultBreakdown: PaymentAmountBreakdown;

  beforeEach(() => {
    calculator = new PaymentRefundCalculator();
    defaultBreakdown = {
      subtotal: 10000,
      discountAmount: 0,
      taxableAmount: 10000,
      taxFreeAmount: 0,
      vatAmount: 1000,
      serviceFeeAmount: 300,
      disposableCupDeposit: 0,
      totalAmount: 11300,
    };
  });

  describe('calculateRefund', () => {
    it('APPROVED 상태에 대한 전체 환불 계산', () => {
      const request: RefundRequest = {
        paymentId: 1,
      };

      const result = calculator.calculateRefund(
        request,
        PaymentStatus.APPROVED,
        11300,
        0,
        defaultBreakdown
      );

      expect(result.refundableAmount).toBe(11300);
      expect(result.refundedAmount).toBeGreaterThan(0);
      expect(result.refundedAmount).toBeLessThan(11300);
    });

    it('부분 환불 계산', () => {
      const request: RefundRequest = {
        paymentId: 1,
        refundAmount: 5000,
      };

      const result = calculator.calculateRefund(
        request,
        PaymentStatus.APPROVED,
        11300,
        0,
        defaultBreakdown
      );

      expect(result.refundedAmount).toBeLessThan(5000);
      expect(result.remainingAmount).toBeGreaterThan(0);
    });

    it('세금 내역 포함 환불 계산', () => {
      const breakdown: PaymentAmountBreakdown = {
        ...defaultBreakdown,
        taxFreeAmount: 2000,
        taxableAmount: 8000,
      };

      const request: RefundRequest = {
        paymentId: 1,
        refundTaxFree: 2000,
        refundTaxable: 8000,
      };

      const result = calculator.calculateRefund(
        request,
        PaymentStatus.APPROVED,
        11300,
        0,
        breakdown
      );

      expect(result.refundableTaxFree).toBe(2000);
      expect(result.refundableTaxable).toBe(8000);
    });

    it('취소 수수료 적용', () => {
      const request: RefundRequest = {
        paymentId: 1,
        refundAmount: 10000,
      };

      const result = calculator.calculateRefund(
        request,
        PaymentStatus.APPROVED,
        11300,
        0,
        defaultBreakdown
      );

      expect(result.refundedAmount).toBeLessThan(10000);
    });

    it('환불 불가 상태에 대한 에러 발생', () => {
      const request: RefundRequest = {
        paymentId: 1,
      };

      expect(() =>
        calculator.calculateRefund(
          request,
          PaymentStatus.PENDING,
          11300,
          0,
          defaultBreakdown
        )
      ).toThrow('환불이 허용되지 않습니다');
    });

    it('환불 금액이 환불 가능 금액 초과 시 에러 발생', () => {
      const request: RefundRequest = {
        paymentId: 1,
        refundAmount: 15000,
      };

      expect(() =>
        calculator.calculateRefund(
          request,
          PaymentStatus.APPROVED,
          11300,
          0,
          defaultBreakdown
        )
      ).toThrow('환불 가능 금액을 초과할 수 없습니다');
    });

    it('환불 가능 금액 없을 시 에러 발생', () => {
      const request: RefundRequest = {
        paymentId: 1,
      };

      expect(() =>
        calculator.calculateRefund(
          request,
          PaymentStatus.APPROVED,
          11300,
          11300,
          defaultBreakdown
        )
      ).toThrow('환불 가능한 금액이 없습니다');
    });

    it('최소 환불 금액 정책 준수', () => {
      const policy: RefundPolicy = {
        allowPartialRefund: true,
        allowFullRefund: true,
        refundDeadlineDays: 7,
        cancellationFeeRate: 0.05,
        minRefundAmount: 1000,
      };

      const request: RefundRequest = {
        paymentId: 1,
        refundAmount: 500,
      };

      expect(() =>
        calculator.calculateRefund(
          request,
          PaymentStatus.APPROVED,
          11300,
          0,
          defaultBreakdown,
          policy
        )
      ).toThrow('최소');
    });

    it('부분 환불 불가 시 에러 발생', () => {
      const policy: RefundPolicy = {
        allowPartialRefund: false,
        allowFullRefund: true,
        refundDeadlineDays: 7,
        cancellationFeeRate: 0.05,
        minRefundAmount: 100,
      };

      const request: RefundRequest = {
        paymentId: 1,
        refundAmount: 5000,
      };

      expect(() =>
        calculator.calculateRefund(
          request,
          PaymentStatus.APPROVED,
          11300,
          0,
          defaultBreakdown,
          policy
        )
      ).toThrow('부분 환불이 허용되지 않습니다');
    });
  });

  describe('calculateCancellationFee', () => {
    it('취소 수수료 정확히 계산', () => {
      const result = calculator.calculateCancellationFee(10000, 0.05);
      expect(result).toBe(500);
    });

    it('취소 수수료 반올림', () => {
      const result = calculator.calculateCancellationFee(3333, 0.05);
      expect(result).toBe(167);
    });

    it('음수 환불 금액에 대한 에러 발생', () => {
      expect(() => calculator.calculateCancellationFee(-1000, 0.05)).toThrow();
    });

    it('유효하지 않은 수수료율에 대한 에러 발생', () => {
      expect(() => calculator.calculateCancellationFee(10000, -0.1)).toThrow();
      expect(() => calculator.calculateCancellationFee(10000, 1.5)).toThrow();
    });
  });

  describe('isWithinRefundDeadline', () => {
    it('기한 내에 true 반환', () => {
      const paymentDate = new Date();
      paymentDate.setDate(paymentDate.getDate() - 3);

      const result = calculator.isWithinRefundDeadline(paymentDate, 7);
      expect(result).toBe(true);
    });

    it('기한 경과 시 false 반환', () => {
      const paymentDate = new Date();
      paymentDate.setDate(paymentDate.getDate() - 10);

      const result = calculator.isWithinRefundDeadline(paymentDate, 7);
      expect(result).toBe(false);
    });

    it('기한 당일에 true 반환', () => {
      const paymentDate = new Date();
      paymentDate.setDate(paymentDate.getDate() - 7);

      const result = calculator.isWithinRefundDeadline(paymentDate, 7);
      expect(result).toBe(true);
    });
  });

  describe('validateRefundRequest', () => {
    it('유효한 환불 요청 검증', () => {
      const request: RefundRequest = {
        paymentId: 1,
        refundAmount: 5000,
      };

      const paymentDate = new Date();
      paymentDate.setDate(paymentDate.getDate() - 1);

      const result = calculator.validateRefundRequest(
        request,
        PaymentStatus.APPROVED,
        11300,
        0,
        paymentDate
      );

      expect(result.isValid).toBe(true);
      expect(result.errors).toHaveLength(0);
    });

    it('유효하지 않은 상태에 대한 에러 반환', () => {
      const request: RefundRequest = {
        paymentId: 1,
      };

      const paymentDate = new Date();

      const result = calculator.validateRefundRequest(
        request,
        PaymentStatus.PENDING,
        11300,
        0,
        paymentDate
      );

      expect(result.isValid).toBe(false);
      expect(result.errors.length).toBeGreaterThan(0);
    });

    it('기한 경과 시 에러 반환', () => {
      const request: RefundRequest = {
        paymentId: 1,
      };

      const paymentDate = new Date();
      paymentDate.setDate(paymentDate.getDate() - 10);

      const result = calculator.validateRefundRequest(
        request,
        PaymentStatus.APPROVED,
        11300,
        0,
        paymentDate
      );

      expect(result.isValid).toBe(false);
      expect(result.errors).toContain('환불 기한이 지났습니다');
    });

    it('환불 금액이 너무 작을 시 에러 반환', () => {
      const request: RefundRequest = {
        paymentId: 1,
        refundAmount: 50,
      };

      const paymentDate = new Date();

      const result = calculator.validateRefundRequest(
        request,
        PaymentStatus.APPROVED,
        11300,
        0,
        paymentDate
      );

      expect(result.isValid).toBe(false);
      expect(result.errors.length).toBeGreaterThan(0);
    });

    it('환불 금액이 환불 가능 금액 초과 시 에러 반환', () => {
      const request: RefundRequest = {
        paymentId: 1,
        refundAmount: 15000,
      };

      const paymentDate = new Date();

      const result = calculator.validateRefundRequest(
        request,
        PaymentStatus.APPROVED,
        11300,
        0,
        paymentDate
      );

      expect(result.isValid).toBe(false);
      expect(result.errors).toContain(
        '환불 금액은 환불 가능 금액을 초과할 수 없습니다'
      );
    });

    it('환불 가능 금액 없을 시 에러 반환', () => {
      const request: RefundRequest = {
        paymentId: 1,
      };

      const paymentDate = new Date();

      const result = calculator.validateRefundRequest(
        request,
        PaymentStatus.APPROVED,
        11300,
        11300,
        paymentDate
      );

      expect(result.isValid).toBe(false);
      expect(result.errors).toContain('환불 가능한 금액이 없습니다');
    });
  });
});
