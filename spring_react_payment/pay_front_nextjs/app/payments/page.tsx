'use client';

import { CreatePaymentForm } from '@/components/payment/CreatePaymentForm';
import { PaymentHistoryList } from '@/components/payment/PaymentHistoryList';
import { BackButton } from '@/components/common/BackButton';
import { ProtectedRoute } from '@/components/common/ProtectedRoute';
import Link from 'next/link';

function PaymentsPageContent() {
  return (
    <div className="min-h-screen bg-gray-50 py-8 px-4 sm:px-6 lg:px-8">
      <div className="max-w-7xl mx-auto">
        <div className="mb-8">
          <div className="mb-4">
            <BackButton />
          </div>
          <h1 className="text-3xl font-bold text-gray-900 mb-4">결제 관�?/h1>
          <div className="flex gap-4">
            <Link
              href="/payments/create"
              className="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700"
            >
              결제 ?�성
            </Link>
            <Link
              href="/payments/history"
              className="bg-gray-600 text-white px-4 py-2 rounded-md hover:bg-gray-700"
            >
              결제 ?�력
            </Link>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          <div className="bg-white shadow rounded-lg p-6">
            <h2 className="text-xl font-semibold mb-4">결제 ?�성</h2>
            <CreatePaymentForm />
          </div>

          <div className="bg-white shadow rounded-lg p-6">
            <h2 className="text-xl font-semibold mb-4">최근 결제 ?�력</h2>
            <PaymentHistoryList />
          </div>
        </div>
      </div>
    </div>
  );
}

export default function PaymentsPage() {
  return (
    <ProtectedRoute>
      <PaymentsPageContent />
    </ProtectedRoute>
  );
}

