'use client';

import { useState, useEffect } from 'react';
import { useCashReceipt } from '@/hooks/use-cash-receipt';
import { ErrorMessage } from '@/components/common/ErrorMessage';
import { LoadingSpinner } from '@/components/common/LoadingSpinner';
import type { CashReceiptResponse } from '@/domain/types/payment.types';

export const CashReceiptList = () => {
  const { getCashReceipts, loading, error } = useCashReceipt();
  const [receipts, setReceipts] = useState<CashReceiptResponse[]>([]);
  const [requestDate, setRequestDate] = useState(
    new Date().toISOString().split('T')[0]
  );
  const [cursor, setCursor] = useState<number | undefined>();
  const [hasNext, setHasNext] = useState(false);

  const loadReceipts = async () => {
    try {
      const response = await getCashReceipts(requestDate, cursor, 100);
      if (cursor) {
        setReceipts((prev) => [...prev, ...response.data]);
      } else {
        setReceipts(response.data);
      }
      setHasNext(response.hasNext || false);
      setCursor(response.lastCursor);
    } catch (err) {
      // Error is handled by ErrorMessage component
    }
  };

  useEffect(() => {
    loadReceipts();
  }, [requestDate]);

  const handleLoadMore = () => {
    if (hasNext && cursor) {
      loadReceipts();
    }
  };

  const getStatusBadge = (status: string) => {
    const statusMap: Record<string, { label: string; className: string }> = {
      IN_PROGRESS: { label: '대기중', className: 'bg-yellow-100 text-yellow-800' },
      COMPLETED: { label: '완료', className: 'bg-green-100 text-green-800' },
      FAILED: { label: '실패', className: 'bg-red-100 text-red-800' },
    };
    const statusInfo = statusMap[status] || { label: status, className: 'bg-gray-100 text-gray-800' };
    return (
      <span className={`px-2 py-1 rounded text-xs font-medium ${statusInfo.className}`}>
        {statusInfo.label}
      </span>
    );
  };

  const getTransactionTypeBadge = (type: string) => {
    return type === 'CONFIRM' ? (
      <span className="px-2 py-1 rounded text-xs font-medium bg-blue-100 text-blue-800">발급</span>
    ) : (
      <span className="px-2 py-1 rounded text-xs font-medium bg-red-100 text-red-800">취소</span>
    );
  };

  return (
    <div className="space-y-4">
      <div className="flex gap-4 items-end">
        <div className="flex-1">
          <label htmlFor="requestDate" className="block text-sm font-medium text-gray-700 mb-1">
            조회 날짜
          </label>
          <input
            id="requestDate"
            type="date"
            value={requestDate}
            onChange={(e) => {
              setRequestDate(e.target.value);
              setCursor(undefined);
            }}
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            disabled={loading}
          />
        </div>
        <button
          onClick={() => {
            setCursor(undefined);
            loadReceipts();
          }}
          disabled={loading}
          className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed"
        >
          조회
        </button>
      </div>

      <ErrorMessage error={error} />

      {loading && receipts.length === 0 ? (
        <div className="flex justify-center py-8">
          <LoadingSpinner />
        </div>
      ) : receipts.length === 0 ? (
        <div className="text-center py-8 text-gray-500">
          조회된 현금영수증이 없습니다.
        </div>
      ) : (
        <>
          <div className="space-y-4">
            {receipts.map((receipt) => (
              <div
                key={receipt.receiptKey}
                className="border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow"
              >
                <div className="flex justify-between items-start mb-2">
                  <div>
                    <h3 className="font-medium text-lg">{receipt.orderName}</h3>
                    <p className="text-sm text-gray-500">주문번호: {receipt.orderId}</p>
                  </div>
                  <div className="flex gap-2">
                    {getTransactionTypeBadge(receipt.transactionType)}
                    {getStatusBadge(receipt.issueStatus)}
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-4 mt-4 text-sm">
                  <div>
                    <span className="text-gray-500">금액:</span>
                    <span className="ml-2 font-medium">{receipt.amount.toLocaleString()}원</span>
                  </div>
                  <div>
                    <span className="text-gray-500">면세 금액:</span>
                    <span className="ml-2">{receipt.taxFreeAmount?.toLocaleString() || 0}원</span>
                  </div>
                  <div>
                    <span className="text-gray-500">종류:</span>
                    <span className="ml-2">{receipt.type}</span>
                  </div>
                  <div>
                    <span className="text-gray-500">발급번호:</span>
                    <span className="ml-2">{receipt.issueNumber || '-'}</span>
                  </div>
                  <div>
                    <span className="text-gray-500">요청일시:</span>
                    <span className="ml-2">{new Date(receipt.requestedAt).toLocaleString('ko-KR')}</span>
                  </div>
                  {receipt.receiptUrl && (
                    <div>
                      <a
                        href={receipt.receiptUrl}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="text-blue-600 hover:underline"
                      >
                        영수증 보기
                      </a>
                    </div>
                  )}
                </div>

                {receipt.failure && (
                  <div className="mt-4 p-3 bg-red-50 border border-red-200 rounded">
                    <p className="text-sm text-red-800">
                      <span className="font-medium">오류:</span> {receipt.failure.message}
                    </p>
                  </div>
                )}
              </div>
            ))}
          </div>

          {hasNext && (
            <div className="flex justify-center pt-4">
              <button
                onClick={handleLoadMore}
                disabled={loading}
                className="px-6 py-2 bg-gray-600 text-white rounded-md hover:bg-gray-700 disabled:bg-gray-400 disabled:cursor-not-allowed"
              >
                {loading ? <LoadingSpinner size="sm" /> : '더 보기'}
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
};


