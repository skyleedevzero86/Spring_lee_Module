import api from '../api';
import type {
  PurchaseInitRequest,
  PurchaseInitResponse,
  PurchaseConfirmRequest,
} from '@/types/api';

const PAYMENT_BASE_URL = '/api/v1/purchase';

export const paymentService = {
  async initPurchase(
    request: PurchaseInitRequest
  ): Promise<PurchaseInitResponse> {
    const response = await api.post<PurchaseInitResponse>(
      `${PAYMENT_BASE_URL}/init`,
      request
    );
    return response.data;
  },

  async confirmPurchase(
    request: PurchaseConfirmRequest
  ): Promise<{ success: boolean }> {
    const response = await api.post(
      `${PAYMENT_BASE_URL}/confirm`,
      request
    );
    return { success: response.status === 200 };
  },
};

