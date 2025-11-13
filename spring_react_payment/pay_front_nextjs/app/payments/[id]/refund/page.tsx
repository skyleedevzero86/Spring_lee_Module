'use client';

import { useEffect, useState } from 'react';
import { useParams } from 'next/navigation';
import { usePayment } from '@/src/hooks/use-payment';
import { LoadingSpinner } from '@/src/components/common/LoadingSpinner';
import { ErrorMessage } from '@/src/components/common/ErrorMessage';
import { ProtectedRoute } from '@/src/components/common/ProtectedRoute';
import { RefundForm } from '@/src/components/payment/RefundForm';
import Link from 'next/link';

function RefundPageContent() {
  const params = useParams();
  const paymentId = Number(params.id);
  const { getPaymentDetail, loading, error } = usePayment();
  const [refundableAmount, setRefundableAmount] = useState(0);

  useEffect(() => {
    if (paymentId) {
      getPaymentDetail(paymentId).then((detail) => {
        if (detail) {
          setRefundableAmount(detail.amount);
        }
      });
    }
  }, [paymentId, getPaymentDetail]);

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 py-8 px-4 sm:px-6 lg:px-8">
        <div className="max-w-2xl mx-auto">
          <LoadingSpinner size="lg" className="py-8" />
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-50 py-8 px-4 sm:px-6 lg:px-8">
        <div className="max-w-2xl mx-auto">
          <ErrorMessage error={error} />
          <Link
            href={`/payments/${paymentId}`}
            className="mt-4 inline-block text-blue-600 hover:text-blue-800"
          >
            ← 돌아가기
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8 px-4 sm:px-6 lg:px-8">
      <div className="max-w-2xl mx-auto">
        <div className="mb-8">
          <Link
            href={`/payments/${paymentId}`}
            className="text-blue-600 hover:text-blue-800 mb-4 inline-block"
          >
            ← 돌아가기
          </Link>
          <h1 className="text-3xl font-bold text-gray-900">환불 요청</h1>
        </div>

        <div className="bg-white shadow rounded-lg p-6">
          <RefundForm paymentId={paymentId} refundableAmount={refundableAmount} />
        </div>
      </div>
    </div>
  );
}

export default function RefundPage() {
  return (
    <ProtectedRoute>
      <RefundPageContent />
    </ProtectedRoute>
  );
}

