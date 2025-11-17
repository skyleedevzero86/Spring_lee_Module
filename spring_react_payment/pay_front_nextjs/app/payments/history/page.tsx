'use client';

import { useState, useEffect } from 'react';
import { PaymentHistoryList } from '@/components/payment/PaymentHistoryList';
import { ProtectedRoute } from '@/components/common/ProtectedRoute';
import { TokenManager } from '@/lib/utils';
import Link from 'next/link';

function PaymentHistoryPageContent() {
  const [isAdmin, setIsAdmin] = useState(false);

  useEffect(() => {
    const checkAdmin = async () => {
      const role = await TokenManager.getUserRole();
      setIsAdmin(role === 'ADMIN');
    };
    checkAdmin();
  }, []);

  return (
    <div className="min-h-screen bg-gray-50 py-8 px-4 sm:px-6 lg:px-8">
      <div className="max-w-7xl mx-auto">
        <div className="mb-8">
          <Link
            href="/payments"
            className="text-blue-600 hover:text-blue-800 mb-4 inline-block"
          >
            돌아가기
          </Link>
          <div className="flex justify-between items-center">
            <h1 className="text-3xl font-bold text-gray-900">결제 이력</h1>
            {isAdmin && (
              <Link
                href="/admin"
                className="px-4 py-2 bg-purple-600 text-white rounded-md hover:bg-purple-700 transition-colors"
              >
                관리자 페이지에서 전체 결제 이력 보기
              </Link>
            )}
          </div>
        </div>

        <div className="bg-white shadow rounded-lg">
          <div className="px-6 py-4 border-b border-gray-200">
            <h2 className="text-lg font-semibold text-gray-900">전체 결제 내역</h2>
          </div>
          <div className="p-6 overflow-x-auto">
            <PaymentHistoryList />
          </div>
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
