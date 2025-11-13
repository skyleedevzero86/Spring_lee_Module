import { paymentApi } from '@/src/infrastructure/api/payment.api';
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
} from '@/src/domain/types/payment.types';
import { handleServiceCall } from '@/src/lib/utils/service-helper';

class PaymentService {
  async createPayment(
    request: CreatePaymentRequest
  ): Promise<PaymentResponse> {
    return handleServiceCall(
      () => paymentApi.createPayment(request),
      'CREATE_PAYMENT_FAILED',
      '결제 생성에 실패했습니다.'
    );
  }

  async approvePayment(
    request: ApprovePaymentRequest
  ): Promise<PaymentApprovalResponse> {
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
    return handleServiceCall(
      () => paymentApi.refundPayment(paymentId, request),
      'REFUND_PAYMENT_FAILED',
      '환불 처리에 실패했습니다.'
    );
  }
}

export const paymentService = new PaymentService();

