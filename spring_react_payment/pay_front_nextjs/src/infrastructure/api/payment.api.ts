import apiClient from '../http/api-client';
import { API_ENDPOINTS } from '@/constants/api.constants';
import { TokenManager } from '@/lib/utils';
import { ApiError } from '@/domain/types/error.types';
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

  async cancelPayment(
    paymentKey: string,
    request: CancelPaymentRequest
  ): Promise<CancelPaymentResponse> {
    return apiClient.post<CancelPaymentResponse>(
      API_ENDPOINTS.PAYMENTS.CANCEL(paymentKey),
      request
    );
  }

  async cancelPaymentById(
    paymentId: number,
    request: CancelPaymentRequest
  ): Promise<CancelPaymentResponse> {
    return apiClient.post<CancelPaymentResponse>(
      API_ENDPOINTS.PAYMENTS.CANCEL_BY_ID(paymentId),
      request
    );
  }

  async downloadReceipt(paymentId: number): Promise<Blob> {
    try {
      const token = await TokenManager.getJwtToken();
      const userId = await TokenManager.getUserId();
      const userRole = await TokenManager.getUserRole();

      const baseURL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080';
      const url = `${baseURL}${API_ENDPOINTS.PAYMENTS.RECEIPT(paymentId)}`;

      const headers: HeadersInit = {
        'Accept': 'application/pdf',
      };

      if (token) {
        headers['Authorization'] = `Bearer ${token}`;
      }

      if (userId) {
        headers['X-User-Id'] = String(userId);
      }

      if (userRole) {
        headers['X-User-Role'] = userRole;
      }

      const response = await fetch(url, {
        method: 'GET',
        headers,
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new ApiError(
          'RECEIPT_DOWNLOAD_ERROR',
          response.status,
          `영수증 다운로드 실패: ${response.statusText}`,
          errorText
        );
      }

      return response.blob();
    } catch (error) {
      if (error instanceof ApiError) {
        throw error;
      }
      throw new ApiError(
        'RECEIPT_DOWNLOAD_ERROR',
        0,
        '영수증 다운로드 중 오류가 발생했습니다.',
        error instanceof Error ? error.message : String(error)
      );
    }
  }
}

export const paymentApi = new PaymentApi();

