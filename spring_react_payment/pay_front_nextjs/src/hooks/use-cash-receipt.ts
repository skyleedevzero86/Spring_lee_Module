import { useMemo } from 'react';
import { cashReceiptService } from '@/application/services/cash-receipt.service';
import { useAsync } from '@/hooks/use-async';
import type {
  IssueCashReceiptRequest,
  CancelCashReceiptRequest,
  CashReceiptListResponse,
} from '@/domain/types/payment.types';

function useAsyncOperation<T, Args extends unknown[]>(
  asyncFn: (...args: Args) => Promise<T>
) {
  const { loading, error, execute } = useAsync(asyncFn);
  return { loading, error, execute };
}

export const useCashReceipt = () => {
  const issueCashReceiptAsync = useAsyncOperation(
    (request: IssueCashReceiptRequest) =>
      cashReceiptService.issueCashReceipt(request)
  );

  const cancelCashReceiptAsync = useAsyncOperation(
    (receiptKey: string, request: CancelCashReceiptRequest) =>
      cashReceiptService.cancelCashReceipt(receiptKey, request)
  );

  const getCashReceiptsAsync = useAsyncOperation(
    (requestDate: string, cursor?: number, limit?: number) =>
      cashReceiptService.getCashReceipts(requestDate, cursor, limit)
  );

  const loading = useMemo(
    () =>
      issueCashReceiptAsync.loading ||
      cancelCashReceiptAsync.loading ||
      getCashReceiptsAsync.loading,
    [
      issueCashReceiptAsync.loading,
      cancelCashReceiptAsync.loading,
      getCashReceiptsAsync.loading,
    ]
  );

  const error = useMemo(
    () =>
      issueCashReceiptAsync.error ||
      cancelCashReceiptAsync.error ||
      getCashReceiptsAsync.error,
    [
      issueCashReceiptAsync.error,
      cancelCashReceiptAsync.error,
      getCashReceiptsAsync.error,
    ]
  );

  return {
    loading,
    error,
    issueCashReceipt: issueCashReceiptAsync.execute,
    cancelCashReceipt: cancelCashReceiptAsync.execute,
    getCashReceipts: getCashReceiptsAsync.execute,
  };
};


