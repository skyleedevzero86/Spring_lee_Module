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
     error.message.includes('?�버???�결?????�습?�다') ||
     error.message.includes('ERR_CONNECTION_REFUSED'));

  if (isConnectionError) {
    return (
      <div className="text-center py-8 text-gray-500">
        결제 ?�력???�습?�다.
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
        : new ApiError('UNKNOWN_ERROR', 500, '?????�는 ?�류가 발생?�습?�다.');
    return <ErrorMessage error={apiError} />;
  }

  if (paymentHistory.length === 0) {
    return (
      <div className="text-center py-8 text-gray-500">
        결제 ?�력???�습?�다.
      </div>
    );
  }

  return (
    <div className="overflow-x-auto">
      <table className="min-w-full divide-y divide-gray-200">
        <thead className="bg-gray-50">
          <tr>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              주문번호
            </th>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              ?�품 ?�명
            </th>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              금액
            </th>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              ?�태
            </th>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              결제?�단
            </th>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              ?�성??            </th>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              ?�업
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
                  ?�세보기
                </Link>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

