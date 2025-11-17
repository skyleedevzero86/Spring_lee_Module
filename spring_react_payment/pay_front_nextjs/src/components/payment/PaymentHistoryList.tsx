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
     error.message.includes('서버에 연결할 수 없습니다') ||
     error.message.includes('ERR_CONNECTION_REFUSED'));

  if (isConnectionError) {
    return (
      <div className="text-center py-8 text-gray-500">
        결제 이력이 없습니다.
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
        : new ApiError('UNKNOWN_ERROR', 500, '알 수 없는 오류가 발생했습니다.');
    return <ErrorMessage error={apiError} />;
  }

  if (paymentHistory.length === 0) {
    return (
      <div className="text-center py-8 text-gray-500">
        결제 이력이 없습니다.
      </div>
    );
  }

  return (
    <div className="w-full overflow-x-auto">
      <table className="min-w-full divide-y divide-gray-300">
        <thead className="bg-gray-50">
          <tr>
            <th className="px-4 py-3.5 text-left text-xs font-semibold text-gray-900 sm:px-6">
              주문번호
            </th>
            <th className="px-4 py-3.5 text-left text-xs font-semibold text-gray-900 sm:px-6">
              상품 명
            </th>
            <th className="px-4 py-3.5 text-left text-xs font-semibold text-gray-900 sm:px-6">
              금액
            </th>
            <th className="px-4 py-3.5 text-left text-xs font-semibold text-gray-900 sm:px-6">
              상태
            </th>
            <th className="px-4 py-3.5 text-left text-xs font-semibold text-gray-900 sm:px-6">
              결제수단
            </th>
            <th className="px-4 py-3.5 text-left text-xs font-semibold text-gray-900 sm:px-6">
              생성일
            </th>
            <th className="px-4 py-3.5 text-left text-xs font-semibold text-gray-900 sm:px-6">
              작업
            </th>
          </tr>
        </thead>
        <tbody className="divide-y divide-gray-200 bg-white">
          {paymentHistory.map((payment) => (
            <tr key={payment.id} className="hover:bg-gray-50">
              <td className="whitespace-nowrap px-4 py-4 text-sm text-gray-900 sm:px-6">
                <div className="max-w-xs truncate" title={payment.orderNo}>
                  {payment.orderNo}
                </div>
              </td>
              <td className="whitespace-nowrap px-4 py-4 text-sm text-gray-900 sm:px-6">
                {payment.productDesc}
              </td>
              <td className="whitespace-nowrap px-4 py-4 text-sm font-medium text-gray-900 sm:px-6">
                {payment.amount.toLocaleString()}원
              </td>
              <td className="whitespace-nowrap px-4 py-4 text-sm sm:px-6">
                <span
                  className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${getStatusColor(payment.status)}`}
                >
                  {payment.status}
                </span>
              </td>
              <td className="whitespace-nowrap px-4 py-4 text-sm text-gray-500 sm:px-6">
                {payment.payMethod || '-'}
              </td>
              <td className="whitespace-nowrap px-4 py-4 text-sm text-gray-500 sm:px-6">
                {new Date(payment.createdAt).toLocaleString('ko-KR', {
                  year: 'numeric',
                  month: '2-digit',
                  day: '2-digit',
                  hour: '2-digit',
                  minute: '2-digit',
                  second: '2-digit',
                  hour12: true,
                })}
              </td>
              <td className="whitespace-nowrap px-4 py-4 text-sm font-medium sm:px-6">
                <Link
                  href={`/payments/${payment.id}`}
                  className="text-blue-600 hover:text-blue-900 transition-colors"
                >
                  상세보기
                </Link>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};
