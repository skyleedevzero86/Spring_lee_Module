import { useState, useEffect } from 'react';
import { paymentService } from '@/lib/services/paymentService';
import type { OrderResponse } from '@/types/api';

export default function MyOrdersPage() {
  const [orders, setOrders] = useState<OrderResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadOrders();
  }, []);

  const loadOrders = async () => {
    try {
      setLoading(true);
      const data = await paymentService.getUserOrders();
      setOrders(data);
    } catch (err: any) {
      setError(err.response?.data?.message || '주문 목록을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleDownloadReceipt = async (orderId: string) => {
    try {
      const blob = await paymentService.downloadReceipt(orderId);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `영수증_${orderId}.pdf`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    } catch (err: any) {
      alert(err.response?.data?.message || '영수증 다운로드에 실패했습니다.');
    }
  };

  if (loading) {
    return <div className="max-w-6xl mx-auto mt-20 p-6">로딩 중...</div>;
  }

  if (error) {
    return <div className="max-w-6xl mx-auto mt-20 p-6 text-red-600">{error}</div>;
  }

  return (
    <div className="max-w-6xl mx-auto mt-20 p-6">
      <h1 className="text-3xl font-bold mb-6">내 결제 내역</h1>
      {orders.length === 0 ? (
        <p className="text-gray-600">결제 내역이 없습니다.</p>
      ) : (
        <div className="overflow-x-auto">
          <table className="min-w-full border-collapse border border-gray-300">
            <thead>
              <tr className="bg-gray-100">
                <th className="border border-gray-300 px-4 py-2">주문번호</th>
                <th className="border border-gray-300 px-4 py-2">주문명</th>
                <th className="border border-gray-300 px-4 py-2">금액</th>
                <th className="border border-gray-300 px-4 py-2">결제수단</th>
                <th className="border border-gray-300 px-4 py-2">상태</th>
                <th className="border border-gray-300 px-4 py-2">결제일시</th>
                <th className="border border-gray-300 px-4 py-2">작업</th>
              </tr>
            </thead>
            <tbody>
              {orders.map((order) => (
                <tr key={order.id}>
                  <td className="border border-gray-300 px-4 py-2">{order.orderId}</td>
                  <td className="border border-gray-300 px-4 py-2">{order.orderName}</td>
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
                  <td className="border border-gray-300 px-4 py-2">
                    {order.status === 'DONE' && (
                      <button
                        onClick={() => handleDownloadReceipt(order.orderId)}
                        className="bg-blue-600 text-white px-3 py-1 rounded hover:bg-blue-700 text-sm"
                      >
                        영수증 다운로드
                      </button>
                    )}
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

