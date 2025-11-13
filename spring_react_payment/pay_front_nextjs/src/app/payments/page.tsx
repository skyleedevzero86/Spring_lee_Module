import dynamic from 'next/dynamic';
import Link from 'next/link';

const CreatePaymentForm = dynamic(
  () => import('@/src/components/payment/CreatePaymentForm').then((mod) => ({ default: mod.CreatePaymentForm })),
  { ssr: false }
);

const PaymentHistoryList = dynamic(
  () => import('@/src/components/payment/PaymentHistoryList').then((mod) => ({ default: mod.PaymentHistoryList })),
  { ssr: false }
);

export default function PaymentsPage() {
  return (
    <div className="min-h-screen bg-gray-50 py-8 px-4 sm:px-6 lg:px-8">
      <div className="max-w-7xl mx-auto">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-4">결제 관리</h1>
          <div className="flex gap-4">
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
              결제 이력
            </Link>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          <div className="bg-white shadow rounded-lg p-6">
            <h2 className="text-xl font-semibold mb-4">결제 생성</h2>
            <CreatePaymentForm />
          </div>

          <div className="bg-white shadow rounded-lg p-6">
            <h2 className="text-xl font-semibold mb-4">최근 결제 이력</h2>
            <PaymentHistoryList />
          </div>
        </div>
      </div>
    </div>
  );
}

