import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { paymentApi } from '@/infrastructure/api/payment.api';
import { paymentService } from '@/application/services/payment.service';
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
} from '@/domain/types/payment.types';

export const paymentQueryKeys = {
  all: ['payments'] as const,
  lists: () => [...paymentQueryKeys.all, 'list'] as const,
  list: (filters: string) => [...paymentQueryKeys.lists(), { filters }] as const,
  details: () => [...paymentQueryKeys.all, 'detail'] as const,
  detail: (id: number) => [...paymentQueryKeys.details(), id] as const,
};

export function useCreatePayment() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (request: CreatePaymentRequest) => paymentService.createPayment(request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: paymentQueryKeys.lists() });
    },
  });
}

export function useApprovePayment() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (request: ApprovePaymentRequest) => paymentService.approvePayment(request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: paymentQueryKeys.all });
    },
  });
}

export function useGetPaymentStatus() {
  return useMutation({
    mutationFn: (request: GetPaymentStatusRequest) => paymentApi.getPaymentStatus(request),
  });
}

export function usePaymentHistory(enabled = true) {
  return useQuery({
    queryKey: paymentQueryKeys.lists(),
    queryFn: async () => {
      try {
        return await paymentApi.getPaymentHistory();
      } catch (error) {
        if (error instanceof ApiError && 
            (error.code === 'NETWORK_ERROR' || 
             error.message.includes('?œë²„???°ê²°?????†ìŠµ?ˆë‹¤') ||
             error.message.includes('ERR_CONNECTION_REFUSED'))) {
          return [];
        }
        throw error;
      }
    },
    enabled,
    staleTime: 2 * 60 * 1000,
    retry: false,
    refetchOnWindowFocus: false,
    refetchOnReconnect: false,
  });
}

export function usePaymentHistoryPage(page: number = 0, size: number = 20, enabled = true) {
  return useQuery({
    queryKey: [...paymentQueryKeys.lists(), page, size],
    queryFn: () => paymentApi.getPaymentHistoryPage(page, size),
    enabled,
    staleTime: 2 * 60 * 1000,
  });
}

export function usePaymentDetail(paymentId: number, enabled = true) {
  return useQuery({
    queryKey: paymentQueryKeys.detail(paymentId),
    queryFn: () => paymentApi.getPaymentDetail(paymentId),
    enabled: enabled && !!paymentId,
    staleTime: 5 * 60 * 1000,
  });
}

export function useRefundPayment() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ paymentId, request }: { paymentId: number; request: RefundPaymentRequest }) =>
      paymentService.refundPayment(paymentId, request),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: paymentQueryKeys.detail(variables.paymentId) });
      queryClient.invalidateQueries({ queryKey: paymentQueryKeys.lists() });
    },
  });
}

