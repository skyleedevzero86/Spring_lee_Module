import apiClient from '../http/api-client';
import { API_ENDPOINTS } from '@/constants/api.constants';
import type {
  IssueCashReceiptRequest,
  CancelCashReceiptRequest,
  CashReceiptResponse,
  CashReceiptListResponse,
} from '@/domain/types/payment.types';

class CashReceiptApi {
  async issueCashReceipt(
    request: IssueCashReceiptRequest
  ): Promise<CashReceiptResponse> {
    return apiClient.post<CashReceiptResponse>(
      API_ENDPOINTS.CASH_RECEIPTS.ISSUE,
      request
    );
  }

  async cancelCashReceipt(
    receiptKey: string,
    request: CancelCashReceiptRequest
  ): Promise<CashReceiptResponse> {
    return apiClient.post<CashReceiptResponse>(
      API_ENDPOINTS.CASH_RECEIPTS.CANCEL(receiptKey),
      request
    );
  }

  async getCashReceipts(
    requestDate: string,
    cursor?: number,
    limit?: number
  ): Promise<CashReceiptListResponse> {
    return apiClient.get<CashReceiptListResponse>(
      API_ENDPOINTS.CASH_RECEIPTS.LIST(requestDate, cursor, limit)
    );
  }
}

export const cashReceiptApi = new CashReceiptApi();


