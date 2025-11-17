'use client';

import { useState, useEffect, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import { ProtectedRoute } from '@/components/common/ProtectedRoute';
import { LoadingSpinner } from '@/components/common/LoadingSpinner';
import { paymentApi } from '@/infrastructure/api/payment.api';
import { TokenManager } from '@/lib/utils';
import { exportPaymentsToExcel } from '@/lib/utils';
import { PaymentStatusPieChart } from '@/components/admin/PaymentStatusPieChart';
import { PaymentTrendLineChart } from '@/components/admin/PaymentTrendLineChart';
import type { PaymentHistoryResponse, PageApiResponse } from '@/domain/types/payment.types';
import Link from 'next/link';

function AdminDashboardContent() {
  const router = useRouter();
  const [payments, setPayments] = useState<PaymentHistoryResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [size] = useState(15);
  const [pageInfo, setPageInfo] = useState<PageApiResponse<PaymentHistoryResponse> | null>(null);
  const [statusFilter, setStatusFilter] = useState<string>('ALL');
  const [mounted, setMounted] = useState(false);
  const [allPayments, setAllPayments] = useState<PaymentHistoryResponse[]>([]);
  const [loadingAllPayments, setLoadingAllPayments] = useState(false);
  const [stats, setStats] = useState({
    totalAmount: 0,
    totalCount: 0,
    completedCount: 0,
    pendingCount: 0,
    failedCount: 0,
    cancelledCount: 0,
  });

  const loadPayments = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await paymentApi.getPaymentHistoryPage(page, size);
      setPageInfo(response);
      
      let filteredPayments = response.content;
      if (statusFilter !== 'ALL') {
        filteredPayments = response.content.filter(
          (p) => p.status === statusFilter
        );
      }
      
      setPayments(filteredPayments);
      
      // 통계 계산
      const allPayments = response.content;
      const totalAmount = allPayments.reduce((sum, p) => sum + (p.amount || 0), 0);
      const completedPayments = allPayments.filter((p) => p.status === 'COMPLETED');
      const pendingPayments = allPayments.filter((p) => p.status === 'PENDING');
      const failedPayments = allPayments.filter((p) => p.status === 'FAILED');
      const cancelledPayments = allPayments.filter((p) => p.status === 'CANCELLED');
      
      setStats({
        totalAmount,
        totalCount: response.totalElements,
        completedCount: completedPayments.length,
        pendingCount: pendingPayments.length,
        failedCount: failedPayments.length,
        cancelledCount: cancelledPayments.length,
      });
    } catch (err) {
      console.error('결제 내역 로드 실패:', err);
      setError('결제 내역을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  }, [page, size, statusFilter]);

  // Ensure we're on the client side
  useEffect(() => {
    setMounted(true);
  }, []);

  const loadAllPayments = useCallback(async () => {
    try {
      setLoadingAllPayments(true);
      const allData = await paymentApi.getPaymentHistory();
      setAllPayments(allData);
      
      const totalAmount = allData.reduce((sum, p) => sum + (p.amount || 0), 0);
      const completedPayments = allData.filter((p) => p.status === 'COMPLETED');
      const pendingPayments = allData.filter((p) => p.status === 'PENDING');
      const failedPayments = allData.filter((p) => p.status === 'FAILED');
      const cancelledPayments = allData.filter((p) => p.status === 'CANCELLED');
      
      setStats((prev) => ({
        ...prev,
        totalAmount,
        completedCount: completedPayments.length,
        pendingCount: pendingPayments.length,
        failedCount: failedPayments.length,
        cancelledCount: cancelledPayments.length,
      }));
    } catch (err) {
      console.error('전체 결제 내역 로드 실패:', err);
    } finally {
      setLoadingAllPayments(false);
    }
  }, []);

  const handleExportExcel = useCallback(async () => {
    try {
      const allData = await paymentApi.getPaymentHistory();
      exportPaymentsToExcel(allData, '결제내역');
    } catch (err) {
      console.error('엑셀 다운로드 실패:', err);
      alert('엑셀 다운로드에 실패했습니다.');
    }
  }, []);

  // Check admin role only once on mount (after client-side check)
  useEffect(() => {
    if (!mounted) return;
    
    const checkAdmin = async () => {
      try {
        const role = await TokenManager.getUserRole();
        if (role !== 'ADMIN') {
          router.push('/');
          return;
        }
        // Load payments after admin check passes
        loadPayments();
        loadAllPayments();
      } catch (error) {
        console.error('Admin check failed:', error);
        router.push('/');
      }
    };
    checkAdmin();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [mounted]); // Only run once after mount

  // Reload payments when page or filter changes (after initial load)
  useEffect(() => {
    if (pageInfo !== null) {
      // Only reload if we've already loaded once
      loadPayments();
    }
    // loadPayments is already memoized with page, size, statusFilter as dependencies
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page, statusFilter]);

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('ko-KR', {
      style: 'currency',
      currency: 'KRW',
    }).format(amount);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleString('ko-KR', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const getStatusBadgeColor = (status: string) => {
    switch (status) {
      case 'COMPLETED':
        return 'bg-green-100 text-green-800';
      case 'PENDING':
        return 'bg-yellow-100 text-yellow-800';
      case 'CANCELLED':
        return 'bg-red-100 text-red-800';
      case 'FAILED':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const getStatusLabel = (status: string) => {
    switch (status) {
      case 'COMPLETED':
        return '완료';
      case 'PENDING':
        return '대기';
      case 'CANCELLED':
        return '취소';
      case 'FAILED':
        return '실패';
      default:
        return status;
    }
  };

  if (loading && payments.length === 0) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8 px-4 sm:px-6 lg:px-8">
      <div className="max-w-7xl mx-auto">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">관리자 대시보드</h1>
          <p className="text-gray-600">모든 사용자의 결제 내역을 관리합니다.</p>
        </div>

        {/* 통계 카드 */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
          <div className="bg-white rounded-lg shadow p-6">
            <div className="text-sm font-medium text-gray-500 mb-1">총 결제 금액</div>
            <div className="text-2xl font-bold text-gray-900">
              {formatCurrency(stats.totalAmount)}
            </div>
          </div>
          <div className="bg-white rounded-lg shadow p-6">
            <div className="text-sm font-medium text-gray-500 mb-1">총 결제 건수</div>
            <div className="text-2xl font-bold text-gray-900">{stats.totalCount}</div>
          </div>
          <div className="bg-white rounded-lg shadow p-6">
            <div className="text-sm font-medium text-gray-500 mb-1">완료된 결제</div>
            <div className="text-2xl font-bold text-green-600">{stats.completedCount}</div>
          </div>
          <div className="bg-white rounded-lg shadow p-6">
            <div className="text-sm font-medium text-gray-500 mb-1">대기 중인 결제</div>
            <div className="text-2xl font-bold text-yellow-600">{stats.pendingCount}</div>
          </div>
        </div>

        {/* 차트 섹션 */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
          <div className="bg-white rounded-lg shadow p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">결제 상태별 분포</h3>
            {loadingAllPayments ? (
              <div className="flex items-center justify-center h-[300px]">
                <LoadingSpinner size="md" />
              </div>
            ) : (
              <PaymentStatusPieChart
                data={{
                  completed: stats.completedCount,
                  pending: stats.pendingCount,
                  failed: stats.failedCount,
                  cancelled: stats.cancelledCount,
                }}
              />
            )}
          </div>
          <div className="bg-white rounded-lg shadow p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">결제 추이</h3>
            {loadingAllPayments ? (
              <div className="flex items-center justify-center h-[300px]">
                <LoadingSpinner size="md" />
              </div>
            ) : (
              <PaymentTrendLineChart payments={allPayments} />
            )}
          </div>
        </div>

        {/* 필터 및 테이블 */}
        <div className="bg-white rounded-lg shadow">
          <div className="px-6 py-4 border-b border-gray-200 flex justify-between items-center">
            <h2 className="text-xl font-semibold text-gray-900">결제 내역</h2>
            <div className="flex gap-4">
              <select
                value={statusFilter}
                onChange={(e) => {
                  setStatusFilter(e.target.value);
                  setPage(0);
                }}
                className="border border-gray-300 rounded-md px-3 py-2 text-sm"
              >
                <option value="ALL">전체</option>
                <option value="COMPLETED">완료</option>
                <option value="PENDING">대기</option>
                <option value="CANCELLED">취소</option>
                <option value="FAILED">실패</option>
              </select>
              <button
                onClick={loadPayments}
                className="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700 text-sm"
              >
                새로고침
              </button>
              <button
                onClick={handleExportExcel}
                className="bg-green-600 text-white px-4 py-2 rounded-md hover:bg-green-700 text-sm flex items-center gap-2"
              >
                <svg
                  className="w-4 h-4"
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
                엑셀 다운로드
              </button>
            </div>
          </div>

          {error && (
            <div className="px-6 py-4 bg-red-50 border-b border-red-200">
              <p className="text-red-800">{error}</p>
            </div>
          )}

          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    주문번호
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    상품명
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    금액
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    상태
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    결제수단
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    생성일시
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    결제일시
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    상세
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {payments.length === 0 ? (
                  <tr>
                    <td colSpan={8} className="px-6 py-4 text-center text-gray-500">
                      {loading ? '로딩 중...' : '결제 내역이 없습니다.'}
                    </td>
                  </tr>
                ) : (
                  payments.map((payment) => (
                    <tr key={payment.id} className="hover:bg-gray-50">
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                        {payment.orderNo}
                      </td>
                      <td className="px-6 py-4 text-sm text-gray-900">
                        {payment.productDesc}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        {formatCurrency(payment.amount)}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span
                          className={`px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full ${getStatusBadgeColor(
                            payment.status
                          )}`}
                        >
                          {getStatusLabel(payment.status)}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        {payment.payMethod || '-'}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        {formatDate(payment.createdAt)}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        {payment.paidTs ? formatDate(payment.paidTs) : '-'}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm">
                        <Link
                          href={`/payments/${payment.id}`}
                          className="text-blue-600 hover:text-blue-900"
                        >
                          보기
                        </Link>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>

          {/* 페이징 */}
          {pageInfo && pageInfo.totalPages > 1 && (
            <div className="px-6 py-4 border-t border-gray-200 flex items-center justify-between">
              <div className="text-sm text-gray-700">
                총 {pageInfo.totalElements}건 중{' '}
                {page * size + 1}-
                {Math.min((page + 1) * size, pageInfo.totalElements)}건 표시
              </div>
              <div className="flex gap-2">
                <button
                  onClick={() => setPage(0)}
                  disabled={!pageInfo.hasPrevious}
                  className="px-3 py-2 text-sm border border-gray-300 rounded-md disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
                >
                  처음
                </button>
                <button
                  onClick={() => setPage(page - 1)}
                  disabled={!pageInfo.hasPrevious}
                  className="px-3 py-2 text-sm border border-gray-300 rounded-md disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
                >
                  이전
                </button>
                <span className="px-3 py-2 text-sm text-gray-700">
                  {page + 1} / {pageInfo.totalPages}
                </span>
                <button
                  onClick={() => setPage(page + 1)}
                  disabled={!pageInfo.hasNext}
                  className="px-3 py-2 text-sm border border-gray-300 rounded-md disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
                >
                  다음
                </button>
                <button
                  onClick={() => setPage(pageInfo.totalPages - 1)}
                  disabled={!pageInfo.hasNext}
                  className="px-3 py-2 text-sm border border-gray-300 rounded-md disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
                >
                  마지막
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default function AdminDashboardPage() {
  return (
    <ProtectedRoute requireAdmin>
      <AdminDashboardContent />
    </ProtectedRoute>
  );
}

