import { useState, useEffect } from 'react';
import api from '@/lib/api';
import type { OrderResponse } from '@/types/api';

export default function AdminDashboardPage() {
  const [orders, setOrders] = useState<OrderResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchParams, setSearchParams] = useState({
    orderId: '',
    memberId: '',
    status: '',
    startDate: '',
    endDate: '',
  });

  useEffect(() => {
    loadAllOrders();
  }, []);

  const loadAllOrders = async () => {
    try {
      setLoading(true);
      const response = await api.get<OrderResponse[]>('/api/v1/admin/orders');
      setOrders(response.data);
    } catch (err: any) {
      setError(err.response?.data?.message || '주문 목록을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async () => {
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
    } catch (err: any) {
      setError(err.response?.data?.message || '검색에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  if (loading && orders.length === 0) {
    return <div className="max-w-6xl mx-auto mt-20 p-6">로딩 중...</div>;
  }

  return (
    <div className="max-w-6xl mx-auto mt-20 p-6">
      <h1 className="text-3xl font-bold mb-6">관리자 대시보드</h1>

      <div className="mb-6 p-4 border rounded bg-gray-50">
        <h2 className="text-xl font-semibold mb-4">결제 검색</h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div>
            <label className="block text-sm font-medium mb-1">주문번호</label>
            <input
              type="text"
              className="w-full border px-3 py-2 rounded"
              value={searchParams.orderId}
              onChange={(e) =>
                setSearchParams({ ...searchParams, orderId: e.target.value })
              }
            />
          </div>
          <div>
            <label className="block text-sm font-medium mb-1">사용자 ID</label>
            <input
              type="number"
              className="w-full border px-3 py-2 rounded"
              value={searchParams.memberId}
              onChange={(e) =>
                setSearchParams({ ...searchParams, memberId: e.target.value })
              }
            />
          </div>
          <div>
            <label className="block text-sm font-medium mb-1">상태</label>
            <select
              className="w-full border px-3 py-2 rounded"
              value={searchParams.status}
              onChange={(e) =>
                setSearchParams({ ...searchParams, status: e.target.value })
              }
            >
              <option value="">전체</option>
              <option value="PENDING">대기</option>
              <option value="DONE">완료</option>
              <option value="REFUNDED">환불됨</option>
              <option value="ABORTED">취소됨</option>
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium mb-1">시작일</label>
            <input
              type="datetime-local"
              className="w-full border px-3 py-2 rounded"
              value={searchParams.startDate}
              onChange={(e) =>
                setSearchParams({ ...searchParams, startDate: e.target.value })
              }
            />
          </div>
          <div>
            <label className="block text-sm font-medium mb-1">종료일</label>
            <input
              type="datetime-local"
              className="w-full border px-3 py-2 rounded"
              value={searchParams.endDate}
              onChange={(e) =>
                setSearchParams({ ...searchParams, endDate: e.target.value })
              }
            />
          </div>
        </div>
        <div className="mt-4 flex gap-2">
          <button
            onClick={handleSearch}
            className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
          >
            검색
          </button>
          <button
            onClick={loadAllOrders}
            className="bg-gray-600 text-white px-4 py-2 rounded hover:bg-gray-700"
          >
            전체 조회
          </button>
        </div>
      </div>

      {error && <div className="mb-4 text-red-600">{error}</div>}

      {orders.length === 0 ? (
        <p className="text-gray-600">결제 내역이 없습니다.</p>
      ) : (
        <div className="overflow-x-auto">
          <table className="min-w-full border-collapse border border-gray-300">
            <thead>
              <tr className="bg-gray-100">
                <th className="border border-gray-300 px-4 py-2">아이디</th>
                <th className="border border-gray-300 px-4 py-2">주문번호</th>
                <th className="border border-gray-300 px-4 py-2">주문명</th>
                <th className="border border-gray-300 px-4 py-2">사용자 ID</th>
                <th className="border border-gray-300 px-4 py-2">금액</th>
                <th className="border border-gray-300 px-4 py-2">결제수단</th>
                <th className="border border-gray-300 px-4 py-2">상태</th>
                <th className="border border-gray-300 px-4 py-2">결제일시</th>
              </tr>
            </thead>
            <tbody>
              {orders.map((order) => (
                <tr key={order.id}>
                  <td className="border border-gray-300 px-4 py-2">{order.id}</td>
                  <td className="border border-gray-300 px-4 py-2">{order.orderId}</td>
                  <td className="border border-gray-300 px-4 py-2">{order.orderName}</td>
                  <td className="border border-gray-300 px-4 py-2">{order.memberId}</td>
                  <td className="border border-gray-300 px-4 py-2">
                    {order.amount.toLocaleString()}원
                  </td>
                  <td className="border border-gray-300 px-4 py-2">
                    {order.paymentMethod || '-'}
                  </td>
                  <td className="border border-gray-300 px-4 py-2">{order.status}</td>
                  <td className="border border-gray-300 px-4 py-2">
                    {new Date(order.createdAt).toLocaleString('ko-KR')}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

