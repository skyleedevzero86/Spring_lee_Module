import type {
  PaymentValidationResult,
  PaymentAmountBreakdown,
} from './types/payment-calculation.types';
import type { CreatePaymentRequest } from './types/payment.types';
import { PaymentStatus } from './types/payment.types';
import { paymentAmountCalculator } from './payment-amount-calculator';
import { paymentStateMachine } from './payment-state-machine';

export class PaymentValidator {
  validatePaymentRequest(
    request: CreatePaymentRequest
  ): PaymentValidationResult {
    const errors: string[] = [];
    const warnings: string[] = [];

    if (!request.orderNo || request.orderNo.trim().length === 0) {
      errors.push('주문번호는 필수입니다');
    } else if (request.orderNo.length > 100) {
      errors.push('주문번호는 100자를 초과할 수 없습니다');
    }

    if (!request.productDesc || request.productDesc.trim().length === 0) {
      errors.push('상품 설명은 필수입니다');
    } else if (request.productDesc.length > 500) {
      errors.push('상품 설명은 500자를 초과할 수 없습니다');
    }

    if (request.amount <= 0) {
      errors.push('금액은 0보다 커야 합니다');
    } else {
      try {
        paymentAmountCalculator.validateAmount(request.amount, '금액');
      } catch (error) {
        errors.push(
          error instanceof Error ? error.message : '유효하지 않은 금액입니다'
        );
      }
    }

    if (request.amountTaxFree < 0) {
      errors.push('면세 금액은 0 이상이어야 합니다');
    }

    if (request.amountTaxFree > request.amount) {
      errors.push('면세 금액은 총 금액을 초과할 수 없습니다');
    }

    if (request.amountTaxable !== undefined && request.amountTaxable < 0) {
      errors.push('과세 금액은 0 이상이어야 합니다');
    }

    if (request.amountVat !== undefined && request.amountVat < 0) {
      errors.push('부가세 금액은 0 이상이어야 합니다');
    }

    if (
      request.amountTaxable !== undefined &&
      request.amountVat !== undefined
    ) {
      const expectedVat = Math.round(request.amountTaxable * 0.1);
      if (Math.abs(request.amountVat - expectedVat) > 1) {
        warnings.push(
          '부가세 금액이 과세 금액과 일치하지 않을 수 있습니다 (과세 금액의 10% 예상)'
        );
      }
    }

    if (
      request.amountTaxFree + (request.amountTaxable || 0) !==
      request.amount
    ) {
      warnings.push(
        '면세 금액 + 과세 금액이 총 금액과 일치하지 않을 수 있습니다'
      );
    }

    if (!request.retUrl || !this.isValidUrl(request.retUrl)) {
      errors.push('유효한 리턴 URL이 필요합니다');
    }

    if (!request.retCancelUrl || !this.isValidUrl(request.retCancelUrl)) {
      errors.push('유효한 취소 리턴 URL이 필요합니다');
    }

    if (request.expiredTime) {
      const expiredDate = new Date(request.expiredTime);
      if (isNaN(expiredDate.getTime())) {
        errors.push('유효하지 않은 만료 시간 형식입니다');
      } else if (expiredDate <= new Date()) {
        errors.push('만료 시간은 미래여야 합니다');
      }
    }

    return {
      isValid: errors.length === 0,
      errors,
      warnings,
    };
  }

  validatePaymentStatus(
    currentStatus: PaymentStatus,
    targetStatus: PaymentStatus
  ): PaymentValidationResult {
    const errors: string[] = [];
    const warnings: string[] = [];

    if (!paymentStateMachine.canTransition(currentStatus, targetStatus)) {
      errors.push(
        `${currentStatus}에서 ${targetStatus}로의 상태 전이는 유효하지 않습니다`
      );
    }

    if (paymentStateMachine.isTerminalState(currentStatus)) {
      errors.push(`터미널 상태 ${currentStatus}에서는 전이가 불가능합니다`);
    }

    return {
      isValid: errors.length === 0,
      errors,
      warnings,
    };
  }

  validateAmountBreakdown(
    breakdown: PaymentAmountBreakdown
  ): PaymentValidationResult {
    const errors: string[] = [];
    const warnings: string[] = [];

    if (breakdown.subtotal < 0) {
      errors.push('소계는 0 이상이어야 합니다');
    }

    if (breakdown.discountAmount < 0) {
      errors.push('할인 금액은 0 이상이어야 합니다');
    }

    if (breakdown.discountAmount > breakdown.subtotal) {
      errors.push('할인 금액은 소계를 초과할 수 없습니다');
    }

    if (breakdown.taxableAmount < 0) {
      errors.push('과세 금액은 0 이상이어야 합니다');
    }

    if (breakdown.taxFreeAmount < 0) {
      errors.push('면세 금액은 0 이상이어야 합니다');
    }

    if (breakdown.vatAmount < 0) {
      errors.push('부가세 금액은 0 이상이어야 합니다');
    }

    if (breakdown.serviceFeeAmount < 0) {
      errors.push('수수료 금액은 0 이상이어야 합니다');
    }

    if (breakdown.totalAmount < 0) {
      errors.push('총 금액은 0 이상이어야 합니다');
    }

    const expectedTotal =
      breakdown.subtotal -
      breakdown.discountAmount +
      breakdown.vatAmount +
      breakdown.serviceFeeAmount +
      breakdown.disposableCupDeposit;

    if (Math.abs(breakdown.totalAmount - expectedTotal) > 1) {
      warnings.push(
        '총 금액이 계산된 구성 요소의 합과 일치하지 않을 수 있습니다'
      );
    }

    return {
      isValid: errors.length === 0,
      errors,
      warnings,
    };
  }

  private isValidUrl(url: string): boolean {
    try {
      const urlObj = new URL(url);
      return urlObj.protocol === 'http:' || urlObj.protocol === 'https:';
    } catch {
      return false;
    }
  }
}

export const paymentValidator = new PaymentValidator();

