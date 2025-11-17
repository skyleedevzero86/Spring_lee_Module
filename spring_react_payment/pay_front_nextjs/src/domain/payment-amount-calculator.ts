import type {
  PaymentAmountBreakdown,
  Discount,
  TaxCalculation,
} from './types/payment-calculation.types';

const DEFAULT_VAT_RATE = 0.1;
const DEFAULT_SERVICE_FEE_RATE = 0.03;
const MAX_AMOUNT = 100000000;
const MIN_AMOUNT = 100;

export class PaymentAmountCalculator {
  calculateTotal(
    subtotal: number,
    options: {
      discount?: Discount;
      taxFreeAmount?: number;
      vatRate?: number;
      serviceFeeRate?: number;
      disposableCupDeposit?: number;
    } = {}
  ): PaymentAmountBreakdown {
    this.validateAmount(subtotal, 'subtotal');

    const {
      discount,
      taxFreeAmount = 0,
      vatRate = DEFAULT_VAT_RATE,
      serviceFeeRate = DEFAULT_SERVICE_FEE_RATE,
      disposableCupDeposit = 0,
    } = options;

    let discountAmount = 0;
    if (discount) {
      discountAmount = this.calculateDiscount(subtotal, discount);
    }

    const discountedSubtotal = Math.max(0, subtotal - discountAmount);
    const taxableAmount = Math.max(0, discountedSubtotal - taxFreeAmount);

    const taxCalculation = this.calculateTax(
      taxFreeAmount,
      taxableAmount,
      vatRate
    );

    const serviceFeeAmount = this.calculateServiceFee(
      discountedSubtotal,
      serviceFeeRate
    );

    const totalAmount =
      discountedSubtotal +
      taxCalculation.vatAmount +
      serviceFeeAmount +
      disposableCupDeposit;

    return {
      subtotal,
      discountAmount,
      taxableAmount,
      taxFreeAmount,
      vatAmount: taxCalculation.vatAmount,
      serviceFeeAmount,
      disposableCupDeposit,
      totalAmount: Math.round(totalAmount),
    };
  }

  calculateDiscount(subtotal: number, discount: Discount): number {
    if (subtotal < 0) {
      throw new Error('소계는 0 이상이어야 합니다');
    }

    if (discount.minPurchaseAmount && subtotal < discount.minPurchaseAmount) {
      return 0;
    }

    let discountAmount = 0;

    switch (discount.type) {
      case 'PERCENTAGE':
        discountAmount = subtotal * (discount.value / 100);
        break;
      case 'FIXED':
        discountAmount = discount.value;
        break;
      case 'CASHBACK':
        discountAmount = discount.value;
        break;
      default:
        throw new Error(`알 수 없는 할인 유형: ${discount.type}`);
    }

    if (discount.maxAmount) {
      discountAmount = Math.min(discountAmount, discount.maxAmount);
    }

    return Math.min(discountAmount, subtotal);
  }

  calculateTax(
    taxFreeAmount: number,
    taxableAmount: number,
    vatRate: number
  ): TaxCalculation {
    if (taxFreeAmount < 0 || taxableAmount < 0) {
      throw new Error('세금 금액은 0 이상이어야 합니다');
    }

    if (vatRate < 0 || vatRate > 1) {
      throw new Error('부가세율은 0과 1 사이여야 합니다');
    }

    const vatAmount = Math.round(taxableAmount * vatRate);

    return {
      taxFreeAmount,
      taxableAmount,
      vatRate,
      vatAmount,
    };
  }

  calculateServiceFee(amount: number, rate: number): number {
    if (amount < 0) {
      throw new Error('금액은 0 이상이어야 합니다');
    }

    if (rate < 0 || rate > 1) {
      throw new Error('수수료율은 0과 1 사이여야 합니다');
    }

    return Math.round(amount * rate);
  }

  validateAmount(amount: number, fieldName: string): void {
    if (typeof amount !== 'number' || isNaN(amount)) {
      throw new Error(`${fieldName}는 유효한 숫자여야 합니다`);
    }

    if (amount < MIN_AMOUNT) {
      throw new Error(
        `${fieldName}는 최소 ${MIN_AMOUNT.toLocaleString()}원 이상이어야 합니다`
      );
    }

    if (amount > MAX_AMOUNT) {
      throw new Error(
        `${fieldName}는 ${MAX_AMOUNT.toLocaleString()}원을 초과할 수 없습니다`
      );
    }
  }

  calculateRefundableAmount(
    paidAmount: number,
    refundedAmount: number,
    breakdown: PaymentAmountBreakdown
  ): number {
    if (paidAmount < 0 || refundedAmount < 0) {
      throw new Error('금액은 0 이상이어야 합니다');
    }

    if (refundedAmount > paidAmount) {
      throw new Error('환불 금액은 결제 금액을 초과할 수 없습니다');
    }

    const remainingAmount = paidAmount - refundedAmount;
    return Math.max(0, remainingAmount);
  }
}

export const paymentAmountCalculator = new PaymentAmountCalculator();

