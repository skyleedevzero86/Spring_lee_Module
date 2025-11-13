'use client';

import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { refundPaymentSchema, type RefundPaymentFormData } from '@/src/domain/validators/payment.validator';
import { usePayment } from '@/src/hooks/use-payment';
import { ErrorMessage } from '@/src/components/common/ErrorMessage';
import { LoadingSpinner } from '@/src/components/common/LoadingSpinner';
import { useState } from 'react';
import { useRouter } from 'next/navigation';
import type { RefundFormProps } from './RefundForm.types';

export const RefundForm = ({ paymentId, refundableAmount }: RefundFormProps) => {
  const router = useRouter();
  const { refundPayment, loading, error } = usePayment();
  const [success, setSuccess] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<RefundPaymentFormData>({
    resolver: zodResolver(refundPaymentSchema),
    defaultValues: {
      amount: refundableAmount,
    },
  });

  const onSubmit = async (data: RefundPaymentFormData) => {
    try {
      await refundPayment(paymentId, {
        ...data,
        refundNo: `REFUND-${Date.now()}`,
      });
      setSuccess(true);
      setTimeout(() => {
        router.push(`/payments/${paymentId}`);
      }, 2000);
    } catch (err) {
      console.error('환불 실패:', err);
    }
  };

  if (success) {
    return (
      <div className="bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded">
        <p className="font-medium">환불 요청이 완료되었습니다.</p>
        <p className="text-sm">잠시 후 결제 상세 페이지로 이동합니다.</p>
      </div>
    );
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <div>
        <label htmlFor="refundNo" className="block text-sm font-medium text-gray-700 mb-1">
          환불 번호
        </label>
        <input
          id="refundNo"
          type="text"
          {...register('refundNo')}
          className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          disabled={loading}
        />
        {errors.refundNo && (
          <p className="mt-1 text-sm text-red-600">{errors.refundNo.message}</p>
        )}
      </div>

      <div>
        <label htmlFor="reason" className="block text-sm font-medium text-gray-700 mb-1">
          환불 사유
        </label>
        <textarea
          id="reason"
          {...register('reason')}
          rows={4}
          className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          disabled={loading}
        />
        {errors.reason && (
          <p className="mt-1 text-sm text-red-600">{errors.reason.message}</p>
        )}
      </div>

      <div>
        <label htmlFor="amount" className="block text-sm font-medium text-gray-700 mb-1">
          환불 금액 (환불 가능 금액: {refundableAmount.toLocaleString()}원)
        </label>
        <input
          id="amount"
          type="number"
          {...register('amount', { valueAsNumber: true })}
          max={refundableAmount}
          className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          disabled={loading}
        />
        {errors.amount && (
          <p className="mt-1 text-sm text-red-600">{errors.amount.message}</p>
        )}
      </div>

      <ErrorMessage error={error} />

      <div className="flex gap-4">
        <button
          type="submit"
          disabled={loading}
          className="flex-1 bg-red-600 text-white py-2 px-4 rounded-md hover:bg-red-700 disabled:bg-gray-400 disabled:cursor-not-allowed flex items-center justify-center"
        >
          {loading ? <LoadingSpinner size="sm" /> : '환불 요청'}
        </button>
        <button
          type="button"
          onClick={() => router.back()}
          disabled={loading}
          className="flex-1 bg-gray-600 text-white py-2 px-4 rounded-md hover:bg-gray-700 disabled:bg-gray-400 disabled:cursor-not-allowed"
        >
          취소
        </button>
      </div>
    </form>
  );
};

