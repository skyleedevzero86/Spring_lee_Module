import { PaymentValidator } from '../payment-validator';
import { PaymentStatus } from '../types/payment.types';
import type {
  CreatePaymentRequest,
  PaymentAmountBreakdown,
} from '../types/payment.types';

describe('PaymentValidator', () => {
  let validator: PaymentValidator;

  beforeEach(() => {
    validator = new PaymentValidator();
  });

  describe('validatePaymentRequest', () => {
    const createValidRequest = (): CreatePaymentRequest => ({
      orderNo: 'ORDER-123',
      productDesc: '테스트 상품',
      amount: 10000,
      amountTaxFree: 0,
      retUrl: 'https://example.com/return',
      retCancelUrl: 'https://example.com/cancel',
    });

    it('유효한 결제 요청 검증', () => {
      const request = createValidRequest();
      const result = validator.validatePaymentRequest(request);

      expect(result.isValid).toBe(true);
      expect(result.errors).toHaveLength(0);
    });

    it('빈 주문번호에 대한 에러 반환', () => {
      const request = createValidRequest();
      request.orderNo = '';

      const result = validator.validatePaymentRequest(request);

      expect(result.isValid).toBe(false);
      expect(result.errors).toContain('주문번호는 필수입니다');
    });

    it('주문번호 길이 초과 시 에러 반환', () => {
      const request = createValidRequest();
      request.orderNo = 'A'.repeat(101);

      const result = validator.validatePaymentRequest(request);

      expect(result.isValid).toBe(false);
      expect(result.errors).toContain('주문번호는 100자를 초과할 수 없습니다');
    });

    it('빈 상품 설명에 대한 에러 반환', () => {
      const request = createValidRequest();
      request.productDesc = '';

      const result = validator.validatePaymentRequest(request);

      expect(result.isValid).toBe(false);
      expect(result.errors).toContain('상품 설명은 필수입니다');
    });

    it('상품 설명 길이 초과 시 에러 반환', () => {
      const request = createValidRequest();
      request.productDesc = 'A'.repeat(501);

      const result = validator.validatePaymentRequest(request);

      expect(result.isValid).toBe(false);
      expect(result.errors).toContain(
        '상품 설명은 500자를 초과할 수 없습니다'
      );
    });

    it('0원 금액에 대한 에러 반환', () => {
      const request = createValidRequest();
      request.amount = 0;

      const result = validator.validatePaymentRequest(request);

      expect(result.isValid).toBe(false);
      expect(result.errors).toContain('금액은 0보다 커야 합니다');
    });

    it('음수 면세 금액에 대한 에러 반환', () => {
      const request = createValidRequest();
      request.amountTaxFree = -1000;

      const result = validator.validatePaymentRequest(request);

      expect(result.isValid).toBe(false);
      expect(result.errors).toContain('면세 금액은 0 이상이어야 합니다');
    });

    it('면세 금액이 총액 초과 시 에러 반환', () => {
      const request = createValidRequest();
      request.amountTaxFree = 15000;

      const result = validator.validatePaymentRequest(request);

      expect(result.isValid).toBe(false);
      expect(result.errors).toContain(
        '면세 금액은 총 금액을 초과할 수 없습니다'
      );
    });

    it('음수 과세 금액에 대한 에러 반환', () => {
      const request = createValidRequest();
      request.amountTaxable = -1000;

      const result = validator.validatePaymentRequest(request);

      expect(result.isValid).toBe(false);
      expect(result.errors).toContain('과세 금액은 0 이상이어야 합니다');
    });

    it('음수 부가세 금액에 대한 에러 반환', () => {
      const request = createValidRequest();
      request.amountVat = -1000;

      const result = validator.validatePaymentRequest(request);

      expect(result.isValid).toBe(false);
      expect(result.errors).toContain('부가세 금액은 0 이상이어야 합니다');
    });

    it('부가세 불일치에 대한 경고 반환', () => {
      const request = createValidRequest();
      request.amountTaxable = 8000;
      request.amountVat = 500;

      const result = validator.validatePaymentRequest(request);

      expect(result.warnings.length).toBeGreaterThan(0);
    });

    it('금액 불일치에 대한 경고 반환', () => {
      const request = createValidRequest();
      request.amountTaxFree = 2000;
      request.amountTaxable = 7000;

      const result = validator.validatePaymentRequest(request);

      expect(result.warnings.length).toBeGreaterThan(0);
    });

    it('유효하지 않은 리턴 URL에 대한 에러 반환', () => {
      const request = createValidRequest();
      request.retUrl = 'invalid-url';

      const result = validator.validatePaymentRequest(request);

      expect(result.isValid).toBe(false);
      expect(result.errors).toContain('유효한 리턴 URL이 필요합니다');
    });

    it('유효하지 않은 취소 리턴 URL에 대한 에러 반환', () => {
      const request = createValidRequest();
      request.retCancelUrl = 'not-a-url';

      const result = validator.validatePaymentRequest(request);

      expect(result.isValid).toBe(false);
      expect(result.errors).toContain('유효한 취소 리턴 URL이 필요합니다');
    });

    it('과거 만료 시간에 대한 에러 반환', () => {
      const request = createValidRequest();
      const pastDate = new Date();
      pastDate.setDate(pastDate.getDate() - 1);
      request.expiredTime = pastDate.toISOString();

      const result = validator.validatePaymentRequest(request);

      expect(result.isValid).toBe(false);
      expect(result.errors).toContain('만료 시간은 미래여야 합니다');
    });

    it('유효하지 않은 만료 시간 형식에 대한 에러 반환', () => {
      const request = createValidRequest();
      request.expiredTime = 'invalid-date';

      const result = validator.validatePaymentRequest(request);

      expect(result.isValid).toBe(false);
      expect(result.errors).toContain('유효하지 않은 만료 시간 형식입니다');
    });
  });

  describe('validatePaymentStatus', () => {
    it('유효한 상태 전이 검증', () => {
      const result = validator.validatePaymentStatus(
        PaymentStatus.PENDING,
        PaymentStatus.APPROVED
      );

      expect(result.isValid).toBe(true);
      expect(result.errors).toHaveLength(0);
    });

    it('유효하지 않은 상태 전이에 대한 에러 반환', () => {
      const result = validator.validatePaymentStatus(
        PaymentStatus.PENDING,
        PaymentStatus.COMPLETED
      );

      expect(result.isValid).toBe(false);
      expect(result.errors.length).toBeGreaterThan(0);
    });

    it('터미널 상태에서 전이 시 에러 반환', () => {
      const result = validator.validatePaymentStatus(
        PaymentStatus.COMPLETED,
        PaymentStatus.APPROVED
      );

      expect(result.isValid).toBe(false);
      expect(result.errors.some(e => e.includes('터미널 상태'))).toBe(true);
    });
  });

  describe('validateAmountBreakdown', () => {
    const createValidBreakdown = (): PaymentAmountBreakdown => ({
      subtotal: 10000,
      discountAmount: 0,
      taxableAmount: 10000,
      taxFreeAmount: 0,
      vatAmount: 1000,
      serviceFeeAmount: 300,
      disposableCupDeposit: 0,
      totalAmount: 11300,
    });

    it('유효한 금액 내역 검증', () => {
      const breakdown = createValidBreakdown();
      const result = validator.validateAmountBreakdown(breakdown);

      expect(result.isValid).toBe(true);
      expect(result.errors).toHaveLength(0);
    });

    it('음수 소계에 대한 에러 반환', () => {
      const breakdown = createValidBreakdown();
      breakdown.subtotal = -1000;

      const result = validator.validateAmountBreakdown(breakdown);

      expect(result.isValid).toBe(false);
      expect(result.errors).toContain('소계는 0 이상이어야 합니다');
    });

    it('음수 할인 금액에 대한 에러 반환', () => {
      const breakdown = createValidBreakdown();
      breakdown.discountAmount = -1000;

      const result = validator.validateAmountBreakdown(breakdown);

      expect(result.isValid).toBe(false);
      expect(result.errors).toContain('할인 금액은 0 이상이어야 합니다');
    });

    it('할인이 소계 초과 시 에러 반환', () => {
      const breakdown = createValidBreakdown();
      breakdown.discountAmount = 15000;

      const result = validator.validateAmountBreakdown(breakdown);

      expect(result.isValid).toBe(false);
      expect(result.errors).toContain('할인 금액은 소계를 초과할 수 없습니다');
    });

    it('음수 과세 금액에 대한 에러 반환', () => {
      const breakdown = createValidBreakdown();
      breakdown.taxableAmount = -1000;

      const result = validator.validateAmountBreakdown(breakdown);

      expect(result.isValid).toBe(false);
      expect(result.errors).toContain('과세 금액은 0 이상이어야 합니다');
    });

    it('음수 면세 금액에 대한 에러 반환', () => {
      const breakdown = createValidBreakdown();
      breakdown.taxFreeAmount = -1000;

      const result = validator.validateAmountBreakdown(breakdown);

      expect(result.isValid).toBe(false);
      expect(result.errors).toContain('면세 금액은 0 이상이어야 합니다');
    });

    it('음수 부가세 금액에 대한 에러 반환', () => {
      const breakdown = createValidBreakdown();
      breakdown.vatAmount = -1000;

      const result = validator.validateAmountBreakdown(breakdown);

      expect(result.isValid).toBe(false);
      expect(result.errors).toContain('부가세 금액은 0 이상이어야 합니다');
    });

    it('음수 수수료에 대한 에러 반환', () => {
      const breakdown = createValidBreakdown();
      breakdown.serviceFeeAmount = -1000;

      const result = validator.validateAmountBreakdown(breakdown);

      expect(result.isValid).toBe(false);
      expect(result.errors).toContain('수수료 금액은 0 이상이어야 합니다');
    });

    it('음수 총 금액에 대한 에러 반환', () => {
      const breakdown = createValidBreakdown();
      breakdown.totalAmount = -1000;

      const result = validator.validateAmountBreakdown(breakdown);

      expect(result.isValid).toBe(false);
      expect(result.errors).toContain('총 금액은 0 이상이어야 합니다');
    });

    it('총 금액 불일치에 대한 경고 반환', () => {
      const breakdown = createValidBreakdown();
      breakdown.totalAmount = 20000;

      const result = validator.validateAmountBreakdown(breakdown);

      expect(result.warnings.length).toBeGreaterThan(0);
    });
  });
});
