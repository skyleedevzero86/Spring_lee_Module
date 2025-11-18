import api from '../api';
import type { PaymentLogResponse } from '@/types/api';

const ADMIN_BASE_URL = '/api/v1/admin';

export const adminService = {
  async getOrderLogs(orderId: string): Promise<PaymentLogResponse[]> {
    const response = await api.get<PaymentLogResponse[]>(
      `${ADMIN_BASE_URL}/orders/${encodeURIComponent(orderId)}/logs`
    );
    return response.data;
  },
};






