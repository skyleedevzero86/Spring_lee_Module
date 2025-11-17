import { useMemo } from 'react';
import { paymentService } from '@/application/services/payment.service';
import { usePaymentStore } from '@/store/payment.store';
import { useAsync } from '@/hooks/use-async';
import type {
  CreatePaymentRequest,
  ApprovePaymentRequest,
  GetPaymentStatusRequest,
  RefundPaymentRequest,
  PaymentHistoryResponse,
  PaymentDetailResponse,
  PageApiResponse,
} from '@/domain/types/payment.types';

function useAsyncOperation<T, Args extends unknown[]>(
  asyncFn: (...args: Args) => Promise<T>
) {
  const { loading, error, execute } = useAsync(asyncFn);
  return { loading, error, execute };
}

export const usePayment = () => {
  const {
    setPaymentHistory,
    setPaymentDetail,
    setPaymentHistoryPage,
  } = usePaymentStore();

  const createPaymentAsync = useAsyncOperation(
    (request: CreatePaymentRequest) => paymentService.createPayment(request)
  );

  const approvePaymentAsync = useAsyncOperation(
    (request: ApprovePaymentRequest) => paymentService.approvePayment(request)
  );

  const getPaymentStatusAsync = useAsyncOperation(
    (request: GetPaymentStatusRequest) => paymentService.getPaymentStatus(request)
  );

  const getPaymentHistoryAsync = useAsync(
    () => paymentService.getPaymentHistory(),
    {
      onSuccess: (history: PaymentHistoryResponse[]) => {
        setPaymentHistory(history);
      },
    }
  );

  const getPaymentHistoryPageAsync = useAsync(
    (page: number = 0, size: number = 20) =>
      paymentService.getPaymentHistoryPage(page, size),
    {
      onSuccess: (historyPage: PageApiResponse<PaymentHistoryResponse>) => {
        setPaymentHistoryPage(historyPage);
      },
    }
  );

  const getPaymentDetailAsync = useAsync(
    (paymentId: number) => paymentService.getPaymentDetail(paymentId),
    {
      onSuccess: (detail: PaymentDetailResponse) => {
        setPaymentDetail(detail);
      },
    }
  );

  const refundPaymentAsync = useAsyncOperation(
    (paymentId: number, request: RefundPaymentRequest) =>
      paymentService.refundPayment(paymentId, request)
  );

  const loading = useMemo(
    () =>
      createPaymentAsync.loading ||
      approvePaymentAsync.loading ||
      getPaymentStatusAsync.loading ||
      getPaymentHistoryAsync.loading ||
      getPaymentHistoryPageAsync.loading ||
      getPaymentDetailAsync.loading ||
      refundPaymentAsync.loading,
    [
      createPaymentAsync.loading,
      approvePaymentAsync.loading,
      getPaymentStatusAsync.loading,
      getPaymentHistoryAsync.loading,
      getPaymentHistoryPageAsync.loading,
      getPaymentDetailAsync.loading,
      refundPaymentAsync.loading,
    ]
  );

  const error = useMemo(
    () =>
      createPaymentAsync.error ||
      approvePaymentAsync.error ||
      getPaymentStatusAsync.error ||
      getPaymentHistoryAsync.error ||
      getPaymentHistoryPageAsync.error ||
      getPaymentDetailAsync.error ||
      refundPaymentAsync.error,
    [
      createPaymentAsync.error,
      approvePaymentAsync.error,
      getPaymentStatusAsync.error,
      getPaymentHistoryAsync.error,
      getPaymentHistoryPageAsync.error,
      getPaymentDetailAsync.error,
      refundPaymentAsync.error,
    ]
  );

  return {
    loading,
    error,
    createPayment: createPaymentAsync.execute,
    approvePayment: approvePaymentAsync.execute,
    getPaymentStatus: getPaymentStatusAsync.execute,
    getPaymentHistory: getPaymentHistoryAsync.execute,
    getPaymentHistoryPage: getPaymentHistoryPageAsync.execute,
    getPaymentDetail: getPaymentDetailAsync.execute,
    refundPayment: refundPaymentAsync.execute,
  };
};

