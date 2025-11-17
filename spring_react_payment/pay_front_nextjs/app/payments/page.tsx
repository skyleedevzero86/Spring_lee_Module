'use client';

import { useState, useEffect } from 'react';
import { CreatePaymentForm } from '@/components/payment/CreatePaymentForm';
import { PaymentHistoryList } from '@/components/payment/PaymentHistoryList';
import { BackButton } from '@/components/common/BackButton';
import { ProtectedRoute } from '@/components/common/ProtectedRoute';
import { TokenManager } from '@/lib/utils';
import { useRouter } from 'next/navigation';
import Link from 'next/link';

function PaymentsPageContent() {
  const router = useRouter();
  const [isAdmin, setIsAdmin] = useState(false);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const checkAdmin = async () => {
      try {
        const role = await TokenManager.getUserRole();
        const adminStatus = role === 'ADMIN';
        setIsAdmin(adminStatus);
        console.log('Admin check:', { role, isAdmin: adminStatus });
      } catch (error) {
        console.error('Failed to check admin role:', error);
        setIsAdmin(false);
      } finally {
        setIsLoading(false);
      }
    };
    checkAdmin();
  }, []);

  const handleLogout = async () => {
    await TokenManager.clearToken();
    router.push('/');
  };

  return (
    <div className="min-h-screen bg-gray-50 py-8 px-4 sm:px-6 lg:px-8">
      <div className="max-w-7xl mx-auto">
        <div className="mb-8">
          <div className="mb-4">
            <BackButton />
          </div>
          <h1 className="text-3xl font-bold text-gray-900 mb-4">결제 관리</h1>
          <div className="flex gap-4 flex-wrap">
            <Link
              href="/payments/create"
              className="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700"
            >
              결제 생성
            </Link>
            <Link
              href="/payments/history"
              className="bg-gray-600 text-white px-4 py-2 rounded-md hover:bg-gray-700"
            >
              결제 내역
            </Link>
            {!isLoading && isAdmin && (
              <Link
                href="/admin"
                className="bg-purple-600 text-white px-4 py-2 rounded-md hover:bg-purple-700"
              >
                전체 결제 이력
              </Link>
            )}
            <button
              onClick={handleLogout}
              className="bg-red-600 text-white px-4 py-2 rounded-md hover:bg-red-700"
            >
              로그아웃
            </button>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          <div className="bg-white shadow rounded-lg p-6">
            <h2 className="text-xl font-semibold mb-4">결제 생성</h2>
            <CreatePaymentForm />
          </div>

          <div className="bg-white shadow rounded-lg">
            <div className="px-6 py-4 border-b border-gray-200">
              <h2 className="text-xl font-semibold">최근 결제 내역</h2>
            </div>
            <div className="p-6 overflow-x-auto">
              <PaymentHistoryList />
            </div>
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
