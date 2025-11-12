import api from '../api';
import type {
  PurchaseInitRequest,
  PurchaseInitResponse,
  PurchaseConfirmRequest,
  OrderResponse,
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
  ): Promise<void> {
    await api.post(
      `${PAYMENT_BASE_URL}/confirm`,
      request
    );
  },

  async getUserOrders(): Promise<OrderResponse[]> {
    const response = await api.get<OrderResponse[]>(
      `${PAYMENT_BASE_URL}/orders`
    );
    return response.data;
  },

  async downloadReceipt(orderId: string): Promise<Blob> {
    const response = await api.get(
      `${PAYMENT_BASE_URL}/receipt/${orderId}`,
      { responseType: 'blob' }
    );
    return response.data;
  },
};

