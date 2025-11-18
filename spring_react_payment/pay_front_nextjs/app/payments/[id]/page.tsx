'use client';

import { useEffect, useState, useRef } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { usePayment } from '@/hooks/use-payment';
import { usePaymentStore } from '@/store/payment.store';
import { LoadingSpinner } from '@/components/common/LoadingSpinner';
import { ErrorMessage } from '@/components/common/ErrorMessage';
import { ProtectedRoute } from '@/components/common/ProtectedRoute';
import { PaymentStatus } from '@/domain/types/payment.types';
import Link from 'next/link';

function PaymentDetailPageContent() {
  const params = useParams();
  const router = useRouter();
  const paymentId = Number(params.id);
  const { getPaymentDetail, downloadReceipt, loading, error } = usePayment();
  const { paymentDetail } = usePaymentStore();
  const [refundLoading, setRefundLoading] = useState(false);
  const [receiptLoading, setReceiptLoading] = useState(false);
  const hasLoadedRef = useRef<number | null>(null);
  const getPaymentDetailRef = useRef(getPaymentDetail);

  useEffect(() => {
    getPaymentDetailRef.current = getPaymentDetail;
  }, [getPaymentDetail]);

  useEffect(() => {
    if (hasLoadedRef.current !== paymentId) {
      hasLoadedRef.current = paymentId;
      
      if (paymentId) {
        getPaymentDetailRef.current(paymentId).catch((err) => {
          console.error('결제 상세 조회 실패:', err);
          hasLoadedRef.current = null;
        });
      }
    }
  }, [paymentId]);

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

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 py-8 px-4 sm:px-6 lg:px-8">
        <div className="max-w-4xl mx-auto">
          <LoadingSpinner size="lg" className="py-8" />
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-50 py-8 px-4 sm:px-6 lg:px-8">
        <div className="max-w-4xl mx-auto">
          <ErrorMessage error={error} />
          <Link
            href="/payments"
            className="mt-4 inline-block text-blue-600 hover:text-blue-800"
          >
            ← 돌아가기
          </Link>
        </div>
      </div>
    );
  }

  if (!paymentDetail) {
    return (
      <div className="min-h-screen bg-gray-50 py-8 px-4 sm:px-6 lg:px-8">
        <div className="max-w-4xl mx-auto">
          <div className="text-center py-8 text-gray-500">
            결제 정보를 찾을 수 없습니다.
          </div>
          <Link
            href="/payments"
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
      <div className="max-w-4xl mx-auto">
        <div className="mb-8">
          <Link
            href="/payments"
            className="text-blue-600 hover:text-blue-800 mb-4 inline-block"
          >
            ← 돌아가기
          </Link>
          <h1 className="text-3xl font-bold text-gray-900">결제 상세</h1>
        </div>

        <div className="bg-white shadow rounded-lg p-6 space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                주문번호
              </label>
              <p className="text-gray-900">{paymentDetail.orderNo}</p>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                상태
              </label>
              <span
                className={`inline-block px-3 py-1 text-sm font-semibold rounded-full ${getStatusColor(
                  paymentDetail.status
                )}`}
              >
                {paymentDetail.status}
              </span>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                상품 설명
              </label>
              <p className="text-gray-900">{paymentDetail.productDesc}</p>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                결제 금액
              </label>
              <p className="text-gray-900">
                {paymentDetail.amount.toLocaleString()}원
              </p>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                비과세 금액
              </label>
              <p className="text-gray-900">
                {paymentDetail.amountTaxFree.toLocaleString()}원
              </p>
            </div>

            {paymentDetail.payMethod && (
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  결제수단
                </label>
                <p className="text-gray-900">{paymentDetail.payMethod}</p>
              </div>
            )}

            {paymentDetail.transactionId && (
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  거래 ID
                </label>
                <p className="text-gray-900">{paymentDetail.transactionId}</p>
              </div>
            )}

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                생성일
              </label>
              <p className="text-gray-900">
                {new Date(paymentDetail.createdAt).toLocaleString('ko-KR')}
              </p>
            </div>

            {paymentDetail.paidTs && (
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  결제 완료일
                </label>
                <p className="text-gray-900">
                  {new Date(paymentDetail.paidTs).toLocaleString('ko-KR')}
                </p>
              </div>
            )}
          </div>

          <div className="pt-6 border-t flex gap-4">
            <button
              onClick={async () => {
                try {
                  setReceiptLoading(true);
                  const blob = await downloadReceipt(paymentId);
                  const url = window.URL.createObjectURL(blob);
                  const link = document.createElement('a');
                  link.href = url;
                  link.download = `영수증_${paymentDetail.orderNo}_${new Date().toISOString().split('T')[0]}.pdf`;
                  document.body.appendChild(link);
                  link.click();
                  document.body.removeChild(link);
                  window.URL.revokeObjectURL(url);
                } catch (err) {
                  console.error('영수증 다운로드 실패:', err);
                  alert('영수증 다운로드에 실패했습니다.');
                } finally {
                  setReceiptLoading(false);
                }
              }}
              disabled={receiptLoading}
              className="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed flex items-center gap-2"
            >
              {receiptLoading ? (
                <>
                  <LoadingSpinner size="sm" />
                  <span>다운로드 중...</span>
                </>
              ) : (
                <>
                  <svg
                    className="w-5 h-5"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
                    />
                  </svg>
                  <span>영수증 다운로드</span>
                </>
              )}
            </button>
            {paymentDetail.status === PaymentStatus.COMPLETED && (
              <button
                onClick={() => router.push(`/payments/${paymentId}/refund`)}
                disabled={refundLoading}
                className="bg-red-600 text-white px-4 py-2 rounded-md hover:bg-red-700 disabled:bg-gray-400 disabled:cursor-not-allowed"
              >
                {refundLoading ? '처리 중...' : '환불 요청'}
              </button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

export default function PaymentDetailPage() {
  return (
    <ProtectedRoute>
      <PaymentDetailPageContent />
    </ProtectedRoute>
  );
}

