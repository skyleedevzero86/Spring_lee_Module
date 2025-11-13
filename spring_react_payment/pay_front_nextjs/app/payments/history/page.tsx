import { PaymentHistoryList } from '@/src/components/payment/PaymentHistoryList';
import Link from 'next/link';

export default function PaymentHistoryPage() {
  return (
    <div className="min-h-screen bg-gray-50 py-8 px-4 sm:px-6 lg:px-8">
      <div className="max-w-7xl mx-auto">
        <div className="mb-8">
          <Link
            href="/payments"
            className="text-blue-600 hover:text-blue-800 mb-4 inline-block"
          >
            ← 돌아가기
          </Link>
          <h1 className="text-3xl font-bold text-gray-900">결제 이력</h1>
        </div>

        <div className="bg-white shadow rounded-lg p-6">
          <PaymentHistoryList />
        </div>
      </div>
    </div>
  );
}

