import apiClient from '../http/api-client';
import { API_ENDPOINTS } from '@/constants/api.constants';
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
} from '@/domain/types/payment.types';

class PaymentApi {
  async createPayment(
    request: CreatePaymentRequest
  ): Promise<PaymentResponse> {
    return apiClient.post<PaymentResponse>(API_ENDPOINTS.PAYMENTS.CREATE, request);
  }

  async approvePayment(
    request: ApprovePaymentRequest
  ): Promise<PaymentApprovalResponse> {
    return apiClient.post<PaymentApprovalResponse>(
      API_ENDPOINTS.PAYMENTS.APPROVE,
      request
    );
  }

  async getPaymentStatus(
    request: GetPaymentStatusRequest
  ): Promise<PaymentStatusResponse> {
    return apiClient.post<PaymentStatusResponse>(
      API_ENDPOINTS.PAYMENTS.STATUS,
      request
    );
  }

  async getPaymentHistory(): Promise<PaymentHistoryResponse[]> {
    return apiClient.get<PaymentHistoryResponse[]>(API_ENDPOINTS.PAYMENTS.HISTORY);
  }

  async getPaymentHistoryPage(
    page: number = 0,
    size: number = 20
  ): Promise<PageApiResponse<PaymentHistoryResponse>> {
    return apiClient.get<PageApiResponse<PaymentHistoryResponse>>(
      API_ENDPOINTS.PAYMENTS.HISTORY_PAGE(page, size)
    );
  }

  async getPaymentDetail(paymentId: number): Promise<PaymentDetailResponse> {
    return apiClient.get<PaymentDetailResponse>(
      API_ENDPOINTS.PAYMENTS.DETAIL(paymentId)
    );
  }

  async refundPayment(
    paymentId: number,
    request: RefundPaymentRequest
  ): Promise<RefundPaymentResponse> {
    return apiClient.post<RefundPaymentResponse>(
      API_ENDPOINTS.PAYMENTS.REFUND(paymentId),
      request
    );
  }
}

export const paymentApi = new PaymentApi();

