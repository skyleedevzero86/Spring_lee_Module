'use client';

import { usePaymentHistory } from '@/hooks/queries/use-payment-queries';
import { LoadingSpinner } from '@/components/common/LoadingSpinner';
import { ErrorMessage } from '@/components/common/ErrorMessage';
import { PaymentStatus } from '@/domain/types/payment.types';
import { ApiError } from '@/domain/types/error.types';
import Link from 'next/link';

export const PaymentHistoryList = () => {
  const { data: paymentHistory = [], isLoading: loading, error } = usePaymentHistory();

  const getStatusColor = (status: string) => {
    switch (status) {
      case PaymentStatus.COMPLETED:
        return 'bg-green-100 text-green-800';
      case PaymentStatus.PENDING:
        return 'bg-yellow-100 text-yellow-800';
      case PaymentStatus.CANCELLED:
        return 'bg-red-100 text-red-800';
      case PaymentStatus.FAILED:
        return 'bg-gray-100 text-gray-800';
      default:
        return 'bg-blue-100 text-blue-800';
    }
  };

  const isConnectionError = error instanceof ApiError && 
    (error.code === 'NETWORK_ERROR' || 
     error.message.includes('?œë²„???°ê²°?????†ìŠµ?ˆë‹¤') ||
     error.message.includes('ERR_CONNECTION_REFUSED'));

  if (isConnectionError) {
    return (
      <div className="text-center py-8 text-gray-500">
        ê²°ì œ ?´ë ¥???†ìŠµ?ˆë‹¤.
      </div>
    );
  }

  if (loading) {
    return <LoadingSpinner size="lg" className="py-8" />;
  }

  if (error && !isConnectionError) {
    const apiError =
      error instanceof ApiError
        ? error
        : new ApiError('UNKNOWN_ERROR', 500, '?????†ëŠ” ?¤ë¥˜ê°€ ë°œìƒ?ˆìŠµ?ˆë‹¤.');
    return <ErrorMessage error={apiError} />;
  }

  if (paymentHistory.length === 0) {
    return (
      <div className="text-center py-8 text-gray-500">
        ê²°ì œ ?´ë ¥???†ìŠµ?ˆë‹¤.
      </div>
    );
  }

  return (
    <div className="overflow-x-auto">
      <table className="min-w-full divide-y divide-gray-200">
        <thead className="bg-gray-50">
          <tr>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              ì£¼ë¬¸ë²ˆí˜¸
            </th>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              ?í’ˆ ?¤ëª…
            </th>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              ê¸ˆì•¡
            </th>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              ?íƒœ
            </th>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              ê²°ì œ?˜ë‹¨
            </th>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              ?ì„±??            </th>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              ?‘ì—…
            </th>
          </tr>
        </thead>
        <tbody className="bg-white divide-y divide-gray-200">
          {paymentHistory.map((payment) => (
            <tr key={payment.id}>
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                {payment.orderNo}
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                {payment.productDesc}
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                {payment.amount.toLocaleString()}??              </td>
              <td className="px-6 py-4 whitespace-nowrap">
                <span
                  className={`px-2 py-1 text-xs font-semibold rounded-full ${getStatusColor(
                    payment.status
                  )}`}
                >
                  {payment.status}
                </span>
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                {payment.payMethod || '-'}
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                {new Date(payment.createdAt).toLocaleString('ko-KR')}
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                <Link
                  href={`/payments/${payment.id}`}
                  className="text-blue-600 hover:text-blue-900"
                >
                  ?ì„¸ë³´ê¸°
                </Link>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

