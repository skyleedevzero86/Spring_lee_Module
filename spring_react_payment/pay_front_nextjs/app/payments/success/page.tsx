'use client';

import { useEffect, useState } from 'react';
import { useSearchParams } from 'next/navigation';
import { usePayment } from '@/hooks/use-payment';
import { LoadingSpinner } from '@/components/common/LoadingSpinner';
import { ErrorMessage } from '@/components/common/ErrorMessage';
import { ProtectedRoute } from '@/components/common/ProtectedRoute';
import { PaymentStatus, PaymentStatusResponse } from '@/domain/types/payment.types';
import { ApiError } from '@/domain/types/error.types';
import Link from 'next/link';

function PaymentSuccessPageContent() {
  const searchParams = useSearchParams();
  const { getPaymentStatus, loading, error } = usePayment();
  const [paymentStatus, setPaymentStatus] = useState<PaymentStatusResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const orderNo = searchParams.get('orderNo');
    const status = searchParams.get('status');

    if (!orderNo) {
      setTimeout(() => setIsLoading(false), 0);
      return;
    }

    if (status === 'PAY_APPROVED') {
      setTimeout(() => setIsLoading(false), 0);
    }

    const fetchPaymentStatus = async (retryCount = 0) => {
      try {
        const timeoutPromise = new Promise<never>((_, reject) => {
          setTimeout(() => reject(new Error('요청 시간이 초과되었습니다.')), 5000);
        });
        
        const response = await Promise.race([
          getPaymentStatus({ orderNo }),
          timeoutPromise
        ]) as PaymentStatusResponse;
        
        if (response != null && typeof response === 'object' && !Array.isArray(response) && 'payStatus' in response) {
          setPaymentStatus(response as PaymentStatusResponse);
          setIsLoading(false);
          
          const payStatus = (response as PaymentStatusResponse).payStatus;
          if (payStatus === PaymentStatus.PENDING && status === 'PAY_APPROVED' && retryCount < 1) {
            setTimeout(() => {
              fetchPaymentStatus(retryCount + 1);
            }, 3000);
          }
        } else {
          console.warn('결제 상태 응답이 올바르지 않습니다:', response);
          setIsLoading(false);
        }
      } catch (err: unknown) {
        console.error('결제 상태 조회 실패:', err);
        
        if (err instanceof ApiError && err.statusCode === 429) {
          setIsLoading(false);
          return;
        }
        
        const errorMessage = err instanceof Error ? err.message : '';
        const isTimeout = errorMessage.includes('요청 시간이 초과');
        const isNotFound = errorMessage.includes('찾을 수 없') || 
                          (err instanceof ApiError && err.statusCode === 404);
        
        if (status === 'PAY_APPROVED') {
          setIsLoading(false);
          
          if (isTimeout && retryCount < 1) {
            setTimeout(() => {
              fetchPaymentStatus(retryCount + 1);
            }, 3000);
          }
          return;
        }
        
        if (isTimeout && retryCount < 1) {
          setTimeout(() => {
            fetchPaymentStatus(retryCount + 1);
          }, 2000);
          return;
        }
        
        if (isNotFound && retryCount < 2) {
          setTimeout(() => {
            fetchPaymentStatus(retryCount + 1);
          }, 2000);
          return;
        }
        
        setIsLoading(false);
      }
    };

    fetchPaymentStatus();
  }, [searchParams, getPaymentStatus]);

  const getStatusColor = (status: string) => {
    switch (status) {
      case PaymentStatus.COMPLETED:
        return 'bg-green-100 text-green-800';
      case PaymentStatus.APPROVED:
        return 'bg-blue-100 text-blue-800';
      case PaymentStatus.PENDING:
        return 'bg-yellow-100 text-yellow-800';
      case PaymentStatus.CANCELLED:
        return 'bg-red-100 text-red-800';
      case PaymentStatus.FAILED:
        return 'bg-gray-100 text-gray-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const getStatusText = (status: string) => {
    switch (status) {
      case PaymentStatus.COMPLETED:
        return '결제 완료';
      case PaymentStatus.APPROVED:
        return '결제 승인';
      case PaymentStatus.PENDING:
        return '결제 대기';
      case PaymentStatus.CANCELLED:
        return '결제 취소';
      case PaymentStatus.FAILED:
        return '결제 실패';
      default:
        return status || '알 수 없음';
    }
  };

  if (isLoading || loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex flex-col items-center justify-center">
        <LoadingSpinner />
        <p className="mt-4 text-sm text-gray-600">결제 정보를 조회하는 중입니다...</p>
        <p className="mt-2 text-xs text-gray-500">잠시만 기다려주세요.</p>
      </div>
    );
  }

  const orderNo = searchParams.get('orderNo');
  const urlStatus = searchParams.get('status');
  const urlPayMethod = searchParams.get('payMethod');

  if (!orderNo) {
    return (
      <div className="min-h-screen bg-gray-50 py-8 px-4 sm:px-6 lg:px-8">
        <div className="max-w-2xl mx-auto">
          <ErrorMessage error={new Error('주문번호가 없습니다.')} />
          <Link
            href="/payments"
            className="mt-4 inline-block text-blue-600 hover:text-blue-800"
          >
            ← 결제 목록으로 돌아가기
          </Link>
        </div>
      </div>
    );
  }

  if (error && !paymentStatus && urlStatus !== 'PAY_APPROVED') {
    const isRateLimitError = error instanceof ApiError && error.statusCode === 429;
    const errorMessage = typeof error === 'string' 
      ? error 
      : error?.message || '알 수 없는 오류가 발생했습니다.';
    
    return (
      <div className="min-h-screen bg-gray-50 py-8 px-4 sm:px-6 lg:px-8">
        <div className="max-w-2xl mx-auto">
          <div className="bg-white shadow rounded-lg p-6">
            <div className="mb-4">
              <div className="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-red-100">
                <svg
                  className="h-6 w-6 text-red-600"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M6 18L18 6M6 6l12 12"
                  />
                </svg>
              </div>
              <h1 className="mt-4 text-2xl font-bold text-gray-900 text-center">
                {isRateLimitError ? '요청 한도 초과' : '결제 정보를 찾을 수 없습니다'}
              </h1>
            </div>
            <ErrorMessage error={error instanceof ApiError ? error : error ? new Error(errorMessage) : null} />
            <div className="mt-6 space-y-4">
              {isRateLimitError ? (
                <div className="p-4 bg-orange-50 border border-orange-200 rounded-md">
                  <p className="text-sm text-orange-800 font-medium mb-2">
                    요청이 너무 많습니다
                  </p>
                  <p className="text-sm text-orange-700">
                    {error.retryAfter 
                      ? `${error.retryAfter}초 후에 다시 시도해주세요.`
                      : '잠시 후 다시 시도해주세요.'}
                  </p>
                </div>
              ) : (
                <div className="p-4 bg-yellow-50 border border-yellow-200 rounded-md">
                  <p className="text-sm text-yellow-800 font-medium mb-2">
                    가능한 원인:
                  </p>
                  <ul className="text-sm text-yellow-700 list-disc list-inside space-y-1">
                    <li>주문번호가 올바르지 않습니다</li>
                    <li>결제가 아직 생성되지 않았습니다</li>
                    <li>서버에서 결제 정보를 처리하는 중입니다</li>
                  </ul>
                </div>
              )}
              <div className="space-y-2">
                <p className="text-sm text-gray-600">
                  <span className="font-medium">주문번호:</span>{' '}
                  <span className="font-mono">{orderNo}</span>
                </p>
                {urlStatus && (
                  <p className="text-sm text-gray-600">
                    <span className="font-medium">토스 결제 상태:</span>{' '}
                    {urlStatus}
                  </p>
                )}
              </div>
              <div className="flex justify-center space-x-4 pt-4">
                <Link
                  href="/payments"
                  className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors"
                >
                  결제 목록으로 돌아가기
                </Link>
                <button
                  onClick={() => window.location.reload()}
                  className="px-4 py-2 bg-gray-200 text-gray-700 rounded-md hover:bg-gray-300 transition-colors"
                >
                  다시 시도
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }

  const actualStatus = paymentStatus?.payStatus || 
                      (urlStatus === 'PAY_APPROVED' ? PaymentStatus.APPROVED : urlStatus) || 
                      'UNKNOWN';
  const isSuccess = actualStatus === PaymentStatus.COMPLETED || 
                    actualStatus === PaymentStatus.APPROVED || 
                    urlStatus === 'PAY_APPROVED';

  return (
    <div className="min-h-screen bg-gray-50 py-8 px-4 sm:px-6 lg:px-8">
      <div className="max-w-2xl mx-auto">
        <div className="bg-white shadow rounded-lg p-6 space-y-6">
          <div className="text-center">
            {isSuccess ? (
              <div className="mb-4">
                <div className="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-green-100">
                  <svg
                    className="h-6 w-6 text-green-600"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M5 13l4 4L19 7"
                    />
                  </svg>
                </div>
                <h1 className="mt-4 text-2xl font-bold text-gray-900">
                  결제가 완료되었습니다
                </h1>
              </div>
            ) : (
              <div className="mb-4">
                <div className="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-yellow-100">
                  <svg
                    className="h-6 w-6 text-yellow-600"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
                    />
                  </svg>
                </div>
                <h1 className="mt-4 text-2xl font-bold text-gray-900">
                  결제 상태 확인 중
                </h1>
              </div>
            )}
          </div>

          <div className="border-t border-gray-200 pt-6 space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  주문번호
                </label>
                <p className="text-gray-900 font-mono text-sm">{orderNo}</p>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  결제 상태
                </label>
                <span
                  className={`inline-block px-3 py-1 text-sm font-semibold rounded-full ${getStatusColor(
                    actualStatus
                  )}`}
                >
                  {getStatusText(actualStatus)}
                </span>
              </div>

              {(paymentStatus?.payMethod || urlPayMethod) && (
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    결제 수단
                  </label>
                  <p className="text-gray-900">{paymentStatus?.payMethod || urlPayMethod}</p>
                </div>
              )}

              {paymentStatus?.amount && (
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    결제 금액
                  </label>
                  <p className="text-gray-900">
                    {paymentStatus.amount.toLocaleString()}원
                  </p>
                </div>
              )}

              {paymentStatus?.paidTs && (
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    결제 시간
                  </label>
                  <p className="text-gray-900">
                    {new Date(paymentStatus.paidTs).toLocaleString('ko-KR')}
                  </p>
                </div>
              )}

              {urlStatus && (
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    토스 결제 상태
                  </label>
                  <p className="text-gray-900">{urlStatus}</p>
                </div>
              )}
            </div>

            {paymentStatus?.payStatus === PaymentStatus.PENDING && urlStatus === 'PAY_APPROVED' && (
              <div className="mt-4 p-4 bg-yellow-50 border border-yellow-200 rounded-md">
                <p className="text-sm text-yellow-800">
                  ⚠️ 토스에서 결제 승인은 완료되었지만, 서버에서 결제 정보를 업데이트하는 중입니다.
                  잠시 후 다시 확인해주세요.
                </p>
              </div>
            )}

            {!paymentStatus && urlStatus === 'PAY_APPROVED' && (
              <div className="mt-4 p-4 bg-blue-50 border border-blue-200 rounded-md">
                <p className="text-sm text-blue-800">
                  ℹ️ 토스에서 결제 승인이 완료되었습니다. 서버에서 결제 정보를 확인하는 중입니다.
                </p>
              </div>
            )}
            
            {!paymentStatus && urlStatus !== 'PAY_APPROVED' && (
              <div className="mt-4 p-4 bg-yellow-50 border border-yellow-200 rounded-md">
                <p className="text-sm text-yellow-800">
                  ⚠️ 결제 정보를 찾을 수 없습니다. 주문번호를 확인해주세요.
                </p>
              </div>
            )}
          </div>

          <div className="border-t border-gray-200 pt-6 flex justify-center space-x-4">
            <Link
              href="/payments"
              className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors"
            >
              결제 목록으로 돌아가기
            </Link>
            {paymentStatus && (
              <Link
                href="/payments/history"
                className="px-4 py-2 bg-gray-200 text-gray-700 rounded-md hover:bg-gray-300 transition-colors"
              >
                결제 내역 보기
              </Link>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

export default function PaymentSuccessPage() {
  return (
    <ProtectedRoute>
      <PaymentSuccessPageContent />
    </ProtectedRoute>
  );
}

