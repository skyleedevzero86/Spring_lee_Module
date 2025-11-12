import api from '@/lib/api';
import type { OrderResponse } from '@/types/api';

interface SearchParams {
  orderId: string;
  memberId: string;
  status: string;
  startDate: string;
  endDate: string;
}

export const handleSearch = async (
  searchParams: SearchParams,
  setOrders: (orders: OrderResponse[]) => void,
  setLoading: (loading: boolean) => void,
  setError: (error: string | null) => void
): Promise<void> => {
  try {
    setLoading(true);
    const params = new URLSearchParams();
    if (searchParams.orderId) params.append('orderId', searchParams.orderId);
    if (searchParams.memberId) params.append('memberId', searchParams.memberId);
    if (searchParams.status) params.append('status', searchParams.status);
    if (searchParams.startDate) params.append('startDate', searchParams.startDate);
    if (searchParams.endDate) params.append('endDate', searchParams.endDate);

    const response = await api.get<OrderResponse[]>(
      `/api/v1/admin/orders/search?${params.toString()}`
    );
    setOrders(response.data);
    setError(null);
  } catch (err: any) {
    setError(err.response?.data?.message || '검색에 실패했습니다.');
  } finally {
    setLoading(false);
  }
};

export const loadAllOrders = async (
  setOrders: (orders: OrderResponse[]) => void,
  setLoading: (loading: boolean) => void,
  setError: (error: string | null) => void
): Promise<void> => {
  try {
    setLoading(true);
    const response = await api.get<OrderResponse[]>('/api/v1/admin/orders');
    setOrders(response.data);
    setError(null);
  } catch (err: any) {
    setError(err.response?.data?.message || '주문 목록을 불러오는데 실패했습니다.');
  } finally {
    setLoading(false);
  }
};

