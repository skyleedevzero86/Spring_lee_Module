import { PaymentAmountCalculator } from '../payment-amount-calculator';
import type { Discount } from '../types/payment-calculation.types';

describe('PaymentAmountCalculator', () => {
  let calculator: PaymentAmountCalculator;

  beforeEach(() => {
    calculator = new PaymentAmountCalculator();
  });

  describe('calculateTotal', () => {
    it('옵션 없이 총액 계산', () => {
      const result = calculator.calculateTotal(10000);

      expect(result.subtotal).toBe(10000);
      expect(result.discountAmount).toBe(0);
      expect(result.taxableAmount).toBe(10000);
      expect(result.taxFreeAmount).toBe(0);
      expect(result.vatAmount).toBe(1000);
      expect(result.serviceFeeAmount).toBe(300);
      expect(result.disposableCupDeposit).toBe(0);
      expect(result.totalAmount).toBe(11300);
    });

    it('면세 금액 포함 총액 계산', () => {
      const result = calculator.calculateTotal(10000, {
        taxFreeAmount: 2000,
      });

      expect(result.subtotal).toBe(10000);
      expect(result.taxFreeAmount).toBe(2000);
      expect(result.taxableAmount).toBe(8000);
      expect(result.vatAmount).toBe(800);
      expect(result.totalAmount).toBe(11100);
    });

    it('퍼센트 할인 포함 총액 계산', () => {
      const discount: Discount = {
        type: 'PERCENTAGE',
        value: 10,
      };

      const result = calculator.calculateTotal(10000, { discount });

      expect(result.subtotal).toBe(10000);
      expect(result.discountAmount).toBe(1000);
      expect(result.taxableAmount).toBe(9000);
      expect(result.vatAmount).toBe(900);
      expect(result.serviceFeeAmount).toBe(270);
      expect(result.totalAmount).toBe(10170);
    });

    it('고정 할인 포함 총액 계산', () => {
      const discount: Discount = {
        type: 'FIXED',
        value: 2000,
      };

      const result = calculator.calculateTotal(10000, { discount });

      expect(result.discountAmount).toBe(2000);
      expect(result.taxableAmount).toBe(8000);
      expect(result.vatAmount).toBe(800);
    });

    it('할인 최대 금액 적용 총액 계산', () => {
      const discount: Discount = {
        type: 'PERCENTAGE',
        value: 50,
        maxAmount: 3000,
      };

      const result = calculator.calculateTotal(10000, { discount });

      expect(result.discountAmount).toBe(3000);
    });

    it('할인 최소 구매 금액 적용 총액 계산', () => {
      const discount: Discount = {
        type: 'PERCENTAGE',
        value: 10,
        minPurchaseAmount: 5000,
      };

      const result1 = calculator.calculateTotal(3000, { discount });
      expect(result1.discountAmount).toBe(0);

      const result2 = calculator.calculateTotal(6000, { discount });
      expect(result2.discountAmount).toBe(600);
    });

    it('커스텀 부가세율 포함 총액 계산', () => {
      const result = calculator.calculateTotal(10000, {
        vatRate: 0.2,
      });

      expect(result.vatAmount).toBe(2000);
    });

    it('커스텀 수수료율 포함 총액 계산', () => {
      const result = calculator.calculateTotal(10000, {
        serviceFeeRate: 0.05,
      });

      expect(result.serviceFeeAmount).toBe(500);
    });

    it('일회용 컵 보증금 포함 총액 계산', () => {
      const result = calculator.calculateTotal(10000, {
        disposableCupDeposit: 300,
      });

      expect(result.disposableCupDeposit).toBe(300);
      expect(result.totalAmount).toBe(11600);
    });

    it('소계를 초과하는 할인 처리', () => {
      const discount: Discount = {
        type: 'FIXED',
        value: 15000,
      };

      const result = calculator.calculateTotal(10000, { discount });

      expect(result.discountAmount).toBe(10000);
      expect(result.taxableAmount).toBe(0);
      expect(result.vatAmount).toBe(0);
    });

    it('유효하지 않은 금액에 대한 에러 발생', () => {
      expect(() => calculator.calculateTotal(50)).toThrow();
      expect(() => calculator.calculateTotal(100000001)).toThrow();
    });
  });

  describe('calculateDiscount', () => {
    it('퍼센트 할인 계산', () => {
      const discount: Discount = {
        type: 'PERCENTAGE',
        value: 20,
      };

      const result = calculator.calculateDiscount(10000, discount);
      expect(result).toBe(2000);
    });

    it('고정 할인 계산', () => {
      const discount: Discount = {
        type: 'FIXED',
        value: 3000,
      };

      const result = calculator.calculateDiscount(10000, discount);
      expect(result).toBe(3000);
    });

    it('캐시백 할인 계산', () => {
      const discount: Discount = {
        type: 'CASHBACK',
        value: 1000,
      };

      const result = calculator.calculateDiscount(10000, discount);
      expect(result).toBe(1000);
    });

    it('최대 금액 적용', () => {
      const discount: Discount = {
        type: 'PERCENTAGE',
        value: 50,
        maxAmount: 2000,
      };

      const result = calculator.calculateDiscount(10000, discount);
      expect(result).toBe(2000);
    });

    it('최소 구매 금액 미달 시 0 반환', () => {
      const discount: Discount = {
        type: 'PERCENTAGE',
        value: 10,
        minPurchaseAmount: 5000,
      };

      const result = calculator.calculateDiscount(3000, discount);
      expect(result).toBe(0);
    });

    it('소계를 초과하지 않음', () => {
      const discount: Discount = {
        type: 'FIXED',
        value: 15000,
      };

      const result = calculator.calculateDiscount(10000, discount);
      expect(result).toBe(10000);
    });

    it('음수 소계에 대한 에러 발생', () => {
      const discount: Discount = {
        type: 'PERCENTAGE',
        value: 10,
      };

      expect(() => calculator.calculateDiscount(-1000, discount)).toThrow();
    });

    it('알 수 없는 할인 유형에 대한 에러 발생', () => {
      const discount = {
        type: 'UNKNOWN',
        value: 1000,
      } as unknown as Discount;

      expect(() => calculator.calculateDiscount(10000, discount)).toThrow();
    });
  });

  describe('calculateTax', () => {
    it('세금 정확히 계산', () => {
      const result = calculator.calculateTax(2000, 8000, 0.1);

      expect(result.taxFreeAmount).toBe(2000);
      expect(result.taxableAmount).toBe(8000);
      expect(result.vatRate).toBe(0.1);
      expect(result.vatAmount).toBe(800);
    });

    it('세금 금액 반올림', () => {
      const result = calculator.calculateTax(0, 3333, 0.1);

      expect(result.vatAmount).toBe(333);
    });

    it('음수 면세 금액에 대한 에러 발생', () => {
      expect(() => calculator.calculateTax(-1000, 8000, 0.1)).toThrow();
    });

    it('음수 과세 금액에 대한 에러 발생', () => {
      expect(() => calculator.calculateTax(2000, -1000, 0.1)).toThrow();
    });

    it('유효하지 않은 부가세율에 대한 에러 발생', () => {
      expect(() => calculator.calculateTax(2000, 8000, -0.1)).toThrow();
      expect(() => calculator.calculateTax(2000, 8000, 1.5)).toThrow();
    });
  });

  describe('calculateServiceFee', () => {
    it('수수료 정확히 계산', () => {
      const result = calculator.calculateServiceFee(10000, 0.03);
      expect(result).toBe(300);
    });

    it('수수료 반올림', () => {
      const result = calculator.calculateServiceFee(3333, 0.03);
      expect(result).toBe(100);
    });

    it('음수 금액에 대한 에러 발생', () => {
      expect(() => calculator.calculateServiceFee(-1000, 0.03)).toThrow();
    });

    it('유효하지 않은 수수료율에 대한 에러 발생', () => {
      expect(() => calculator.calculateServiceFee(10000, -0.1)).toThrow();
      expect(() => calculator.calculateServiceFee(10000, 1.5)).toThrow();
    });
  });

  describe('validateAmount', () => {
    it('유효한 금액 통과', () => {
      expect(() => calculator.validateAmount(10000, 'amount')).not.toThrow();
    });

    it('최소 금액 미만에 대한 에러 발생', () => {
      expect(() => calculator.validateAmount(50, 'amount')).toThrow();
    });

    it('최대 금액 초과에 대한 에러 발생', () => {
      expect(() => calculator.validateAmount(100000001, 'amount')).toThrow();
    });

    it('NaN에 대한 에러 발생', () => {
      expect(() => calculator.validateAmount(NaN, 'amount')).toThrow();
    });

    it('숫자가 아닌 값에 대한 에러 발생', () => {
      expect(() =>
        calculator.validateAmount('10000' as unknown as number, 'amount')
      ).toThrow();
    });
  });

  describe('calculateRefundableAmount', () => {
    const breakdown = {
      subtotal: 10000,
      discountAmount: 0,
      taxableAmount: 10000,
      taxFreeAmount: 0,
      vatAmount: 1000,
      serviceFeeAmount: 300,
      disposableCupDeposit: 0,
      totalAmount: 11300,
    };

    it('환불 가능 금액 정확히 계산', () => {
      const result = calculator.calculateRefundableAmount(11300, 0, breakdown);
      expect(result).toBe(11300);
    });

    it('남은 환불 가능 금액 계산', () => {
      const result = calculator.calculateRefundableAmount(11300, 5000, breakdown);
      expect(result).toBe(6300);
    });

    it('전체 환불 시 0 반환', () => {
      const result = calculator.calculateRefundableAmount(11300, 11300, breakdown);
      expect(result).toBe(0);
    });

    it('음수 결제 금액에 대한 에러 발생', () => {
      expect(() =>
        calculator.calculateRefundableAmount(-1000, 0, breakdown)
      ).toThrow();
    });

    it('음수 환불 금액에 대한 에러 발생', () => {
      expect(() =>
        calculator.calculateRefundableAmount(11300, -1000, breakdown)
      ).toThrow();
    });

    it('환불 금액이 결제 금액 초과 시 에러 발생', () => {
      expect(() =>
        calculator.calculateRefundableAmount(11300, 15000, breakdown)
      ).toThrow();
    });
  });
});
