import { cashReceiptApi } from '@/infrastructure/api/cash-receipt.api';
import type {
  IssueCashReceiptRequest,
  CancelCashReceiptRequest,
  CashReceiptResponse,
  CashReceiptListResponse,
} from '@/domain/types/payment.types';
import { handleServiceCall } from '@/lib/utils';

class CashReceiptService {
  async issueCashReceipt(
    request: IssueCashReceiptRequest
  ): Promise<CashReceiptResponse> {
    return handleServiceCall(
      () => cashReceiptApi.issueCashReceipt(request),
      'ISSUE_CASH_RECEIPT_FAILED',
      '현금영수증 발급에 실패했습니다.'
    );
  }

  async cancelCashReceipt(
    receiptKey: string,
    request: CancelCashReceiptRequest
  ): Promise<CashReceiptResponse> {
    return handleServiceCall(
      () => cashReceiptApi.cancelCashReceipt(receiptKey, request),
      'CANCEL_CASH_RECEIPT_FAILED',
      '현금영수증 취소에 실패했습니다.'
    );
  }

  async getCashReceipts(
    requestDate: string,
    cursor?: number,
    limit?: number
  ): Promise<CashReceiptListResponse> {
    return handleServiceCall(
      () => cashReceiptApi.getCashReceipts(requestDate, cursor, limit),
      'GET_CASH_RECEIPTS_FAILED',
      '현금영수증 조회에 실패했습니다.'
    );
  }
}

export const cashReceiptService = new CashReceiptService();


