import type { ApiError } from '@/src/domain/types/error.types';
import type {
  CreatePaymentRequest,
  ApprovePaymentRequest,
  GetPaymentStatusRequest,
  RefundPaymentRequest,
} from '@/src/domain/types/payment.types';

export interface UsePaymentReturn {
  loading: boolean;
  error: ApiError | null;
  createPayment: (request: CreatePaymentRequest) => Promise<unknown>;
  approvePayment: (request: ApprovePaymentRequest) => Promise<unknown>;
  getPaymentStatus: (request: GetPaymentStatusRequest) => Promise<unknown>;
  getPaymentHistory: () => Promise<unknown>;
  getPaymentHistoryPage: (page?: number, size?: number) => Promise<unknown>;
  getPaymentDetail: (paymentId: number) => Promise<unknown>;
  refundPayment: (paymentId: number, request: RefundPaymentRequest) => Promise<unknown>;
}

