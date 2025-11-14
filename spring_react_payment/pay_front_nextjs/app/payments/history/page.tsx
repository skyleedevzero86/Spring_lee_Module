'use client';

import { PaymentHistoryList } from '@/components/payment/PaymentHistoryList';
import { ProtectedRoute } from '@/components/common/ProtectedRoute';
import Link from 'next/link';

function PaymentHistoryPageContent() {
  return (
    <div className="min-h-screen bg-gray-50 py-8 px-4 sm:px-6 lg:px-8">
      <div className="max-w-7xl mx-auto">
        <div className="mb-8">
          <Link
            href="/payments"
            className="text-blue-600 hover:text-blue-800 mb-4 inline-block"
          >
            ???Œì•„ê°€ê¸?          </Link>
          <h1 className="text-3xl font-bold text-gray-900">ê²°ì œ ?´ë ¥</h1>
        </div>

        <div className="bg-white shadow rounded-lg p-6">
          <PaymentHistoryList />
        </div>
      </div>
    </div>
  );
}

export default function PaymentHistoryPage() {
  return (
    <ProtectedRoute>
      <PaymentHistoryPageContent />
    </ProtectedRoute>
  );
}

