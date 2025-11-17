import { paymentApi } from '@/infrastructure/api/payment.api';
import type {
  CreatePaymentRequest,
  PaymentResponse,
  ApprovePaymentRequest,
  PaymentApprovalResponse,
  GetPaymentStatusRequest,
  PaymentStatusResponse,
  PaymentHistoryResponse,
  PaymentDetailResponse,
  RefundPaymentRequest,
  RefundPaymentResponse,
  PageApiResponse,
  CancelPaymentRequest,
  CancelPaymentResponse,
} from '@/domain/types/payment.types';
import { PaymentStatus } from '@/domain/types/payment.types';
import { handleServiceCall } from '@/lib/utils';
import { paymentAmountCalculator } from '@/domain/payment-amount-calculator';
import { paymentStateMachine } from '@/domain/payment-state-machine';
import { paymentRefundCalculator } from '@/domain/payment-refund-calculator';
import { paymentValidator } from '@/domain/payment-validator';
import { ApiError } from '@/domain/types/error.types';
import type {
  PaymentAmountBreakdown,
  Discount,
  RefundRequest,
} from '@/domain/types/payment-calculation.types';

class PaymentService {
  async createPayment(
    request: CreatePaymentRequest
  ): Promise<PaymentResponse> {
    const validation = paymentValidator.validatePaymentRequest(request);
    if (!validation.isValid) {
      throw new ApiError(
        'VALIDATION_ERROR',
        400,
        validation.errors.join(', ')
      );
    }

    const breakdown = paymentAmountCalculator.calculateTotal(request.amount, {
      taxFreeAmount: request.amountTaxFree,
      vatRate: request.amountVat
        ? request.amountVat / (request.amountTaxable || 0)
        : 0.1,
      serviceFeeRate: request.amountServiceFee
        ? request.amountServiceFee / request.amount
        : 0.03,
      disposableCupDeposit: request.disposableCupDeposit,
    });

    const breakdownValidation = paymentValidator.validateAmountBreakdown(
      breakdown
    );
    if (!breakdownValidation.isValid) {
      throw new ApiError(
        'CALCULATION_ERROR',
        400,
        breakdownValidation.errors.join(', ')
      );
    }

    return handleServiceCall(
      () => paymentApi.createPayment(request),
      'CREATE_PAYMENT_FAILED',
      '결제 생성에 실패했습니다.'
    );
  }

  async createPaymentWithDiscount(
    request: CreatePaymentRequest,
    discount: Discount
  ): Promise<PaymentResponse> {
    const breakdown = paymentAmountCalculator.calculateTotal(request.amount, {
      discount,
      taxFreeAmount: request.amountTaxFree,
      disposableCupDeposit: request.disposableCupDeposit,
    });

    const adjustedRequest: CreatePaymentRequest = {
      ...request,
      amount: breakdown.totalAmount,
      amountTaxFree: breakdown.taxFreeAmount,
      amountTaxable: breakdown.taxableAmount,
      amountVat: breakdown.vatAmount,
      amountServiceFee: breakdown.serviceFeeAmount,
    };

    return this.createPayment(adjustedRequest);
  }

  async approvePayment(
    request: ApprovePaymentRequest,
    currentStatus?: PaymentStatus
  ): Promise<PaymentApprovalResponse> {
    if (currentStatus) {
      const statusValidation = paymentValidator.validatePaymentStatus(
        currentStatus,
        PaymentStatus.APPROVED
      );
      if (!statusValidation.isValid) {
        throw new ApiError(
          'INVALID_STATUS_TRANSITION',
          400,
          statusValidation.errors.join(', ')
        );
      }
    }

    return handleServiceCall(
      () => paymentApi.approvePayment(request),
      'APPROVE_PAYMENT_FAILED',
      '결제 승인에 실패했습니다.'
    );
  }

  async getPaymentStatus(
    request: GetPaymentStatusRequest
  ): Promise<PaymentStatusResponse> {
    return handleServiceCall(
      () => paymentApi.getPaymentStatus(request),
      'GET_PAYMENT_STATUS_FAILED',
      '결제 상태 조회에 실패했습니다.'
    );
  }

  async getPaymentHistory(): Promise<PaymentHistoryResponse[]> {
    return handleServiceCall(
      () => paymentApi.getPaymentHistory(),
      'GET_PAYMENT_HISTORY_FAILED',
      '결제 이력 조회에 실패했습니다.'
    );
  }

  async getPaymentHistoryPage(
    page: number = 0,
    size: number = 20
  ): Promise<PageApiResponse<PaymentHistoryResponse>> {
    return handleServiceCall(
      () => paymentApi.getPaymentHistoryPage(page, size),
      'GET_PAYMENT_HISTORY_FAILED',
      '결제 이력 조회에 실패했습니다.'
    );
  }

  async getPaymentDetail(
    paymentId: number
  ): Promise<PaymentDetailResponse> {
    return handleServiceCall(
      () => paymentApi.getPaymentDetail(paymentId),
      'GET_PAYMENT_DETAIL_FAILED',
      '결제 상세 조회에 실패했습니다.'
    );
  }

  async refundPayment(
    paymentId: number,
    request: RefundPaymentRequest
  ): Promise<RefundPaymentResponse> {
    const paymentDetail = await this.getPaymentDetail(paymentId);
    const refundRequest: RefundRequest = {
      paymentId,
      refundAmount: request.amount,
      refundTaxFree: request.amountTaxFree,
      refundTaxable: request.amountTaxable,
      refundVat: request.amountVat,
      refundServiceFee: request.amountServiceFee,
      reason: request.reason,
    };

    const breakdown: PaymentAmountBreakdown = {
      subtotal: paymentDetail.amount,
      discountAmount: paymentDetail.discountedAmount || 0,
      taxableAmount: paymentDetail.amountTaxable || 0,
      taxFreeAmount: paymentDetail.amountTaxFree,
      vatAmount: paymentDetail.amountVat || 0,
      serviceFeeAmount: paymentDetail.amountServiceFee || 0,
      disposableCupDeposit: paymentDetail.disposableCupDeposit || 0,
      totalAmount: paymentDetail.paidAmount || paymentDetail.amount,
    };

    const paidAmount = paymentDetail.paidAmount || paymentDetail.amount;
    const refundedAmount = 0;

    const paymentDate = new Date(paymentDetail.paidTs || paymentDetail.createdAt);
    const validation = paymentRefundCalculator.validateRefundRequest(
      refundRequest,
      paymentDetail.status as PaymentStatus,
      paidAmount,
      refundedAmount,
      paymentDate
    );

    if (!validation.isValid) {
      throw new ApiError(
        'REFUND_VALIDATION_ERROR',
        400,
        validation.errors.join(', ')
      );
    }

    const refundCalculation = paymentRefundCalculator.calculateRefund(
      refundRequest,
      paymentDetail.status as PaymentStatus,
      paidAmount,
      refundedAmount,
      breakdown
    );

    const adjustedRequest: RefundPaymentRequest = {
      ...request,
      amount: refundCalculation.refundedAmount,
      amountTaxFree: refundCalculation.refundableTaxFree,
      amountTaxable: refundCalculation.refundableTaxable,
      amountVat: refundCalculation.refundableVat,
      amountServiceFee: refundCalculation.refundableServiceFee,
    };

    return handleServiceCall(
      () => paymentApi.refundPayment(paymentId, adjustedRequest),
      'REFUND_PAYMENT_FAILED',
      '환불 처리에 실패했습니다.'
    );
  }

  calculatePaymentAmount(
    subtotal: number,
    options: {
      discount?: Discount;
      taxFreeAmount?: number;
      vatRate?: number;
      serviceFeeRate?: number;
      disposableCupDeposit?: number;
    } = {}
  ): PaymentAmountBreakdown {
    return paymentAmountCalculator.calculateTotal(subtotal, options);
  }

  validatePaymentStatusTransition(
    from: PaymentStatus,
    to: PaymentStatus
  ): boolean {
    return paymentStateMachine.canTransition(from, to);
  }

  getAllowedStatusTransitions(
    currentStatus: PaymentStatus
  ): PaymentStatus[] {
    return paymentStateMachine.getAllowedTransitions(currentStatus);
  }

  async cancelPayment(
    paymentKey: string,
    request: CancelPaymentRequest
  ): Promise<CancelPaymentResponse> {
    return handleServiceCall(
      () => paymentApi.cancelPayment(paymentKey, request),
      'CANCEL_PAYMENT_FAILED',
      '결제 취소에 실패했습니다.'
    );
  }
}

export const paymentService = new PaymentService();
