import type {
  RefundCalculation,
  PaymentAmountBreakdown,
} from './types/payment-calculation.types';
import { PaymentStatus } from './types/payment.types';
import { paymentStateMachine } from './payment-state-machine';

export interface RefundPolicy {
  allowPartialRefund: boolean;
  allowFullRefund: boolean;
  refundDeadlineDays: number;
  cancellationFeeRate: number;
  minRefundAmount: number;
}

export interface RefundRequest {
  paymentId: number;
  refundAmount?: number;
  refundTaxFree?: number;
  refundTaxable?: number;
  refundVat?: number;
  refundServiceFee?: number;
  reason?: string;
}

export class PaymentRefundCalculator {
  private readonly defaultPolicy: RefundPolicy = {
    allowPartialRefund: true,
    allowFullRefund: true,
    refundDeadlineDays: 7,
    cancellationFeeRate: 0.05,
    minRefundAmount: 100,
  };

  calculateRefund(
    request: RefundRequest,
    paymentStatus: PaymentStatus,
    paidAmount: number,
    refundedAmount: number,
    breakdown: PaymentAmountBreakdown,
    policy: RefundPolicy = this.defaultPolicy
  ): RefundCalculation {
    if (!paymentStateMachine.canRefund(paymentStatus)) {
      throw new Error(
        `결제 상태 ${paymentStatus}에서는 환불이 허용되지 않습니다`
      );
    }

    const refundableAmount = paidAmount - refundedAmount;
    if (refundableAmount <= 0) {
      throw new Error('환불 가능한 금액이 없습니다');
    }

    let requestedRefundAmount = request.refundAmount || refundableAmount;

    if (requestedRefundAmount < policy.minRefundAmount) {
      throw new Error(
        `환불 금액은 최소 ${policy.minRefundAmount}원 이상이어야 합니다`
      );
    }

    if (requestedRefundAmount > refundableAmount) {
      throw new Error('환불 금액은 환불 가능 금액을 초과할 수 없습니다');
    }

    if (!policy.allowPartialRefund && requestedRefundAmount !== refundableAmount) {
      throw new Error('부분 환불이 허용되지 않습니다');
    }

    const cancellationFee = this.calculateCancellationFee(
      requestedRefundAmount,
      policy.cancellationFeeRate
    );

    const finalRefundAmount = requestedRefundAmount - cancellationFee;

    const refundableTaxFree = request.refundTaxFree ?? breakdown.taxFreeAmount;
    const refundableTaxable =
      request.refundTaxable ?? breakdown.taxableAmount;
    const refundableVat = request.refundVat ?? breakdown.vatAmount;
    const refundableServiceFee =
      request.refundServiceFee ?? breakdown.serviceFeeAmount;

    const remainingAmount = refundableAmount - requestedRefundAmount;

    return {
      refundableAmount,
      refundableTaxFree,
      refundableTaxable,
      refundableVat,
      refundableServiceFee,
      refundedAmount: Math.max(0, finalRefundAmount),
      remainingAmount: Math.max(0, remainingAmount),
    };
  }

  calculateCancellationFee(
    refundAmount: number,
    feeRate: number
  ): number {
    if (refundAmount < 0) {
      throw new Error('환불 금액은 0 이상이어야 합니다');
    }

    if (feeRate < 0 || feeRate > 1) {
      throw new Error('취소 수수료율은 0과 1 사이여야 합니다');
    }

    return Math.round(refundAmount * feeRate);
  }

  isWithinRefundDeadline(
    paymentDate: Date,
    deadlineDays: number
  ): boolean {
    const deadline = new Date(paymentDate);
    deadline.setDate(deadline.getDate() + deadlineDays);
    return new Date() <= deadline;
  }

  validateRefundRequest(
    request: RefundRequest,
    paymentStatus: PaymentStatus,
    paidAmount: number,
    refundedAmount: number,
    paymentDate: Date,
    policy: RefundPolicy = this.defaultPolicy
  ): { isValid: boolean; errors: string[] } {
    const errors: string[] = [];

    if (!paymentStateMachine.canRefund(paymentStatus)) {
      errors.push(`결제 상태 ${paymentStatus}에서는 환불이 허용되지 않습니다`);
    }

    if (!this.isWithinRefundDeadline(paymentDate, policy.refundDeadlineDays)) {
      errors.push('환불 기한이 지났습니다');
    }

    const refundableAmount = paidAmount - refundedAmount;
    if (refundableAmount <= 0) {
      errors.push('환불 가능한 금액이 없습니다');
    }

    if (request.refundAmount) {
      if (request.refundAmount < policy.minRefundAmount) {
        errors.push(
          `환불 금액은 최소 ${policy.minRefundAmount}원 이상이어야 합니다`
        );
      }

      if (request.refundAmount > refundableAmount) {
        errors.push('환불 금액은 환불 가능 금액을 초과할 수 없습니다');
      }

      if (
        !policy.allowPartialRefund &&
        request.refundAmount !== refundableAmount
      ) {
        errors.push('부분 환불이 허용되지 않습니다');
      }
    }

    return {
      isValid: errors.length === 0,
      errors,
    };
  }
}

export const paymentRefundCalculator = new PaymentRefundCalculator();

