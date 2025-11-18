'use client';

import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { usePayment } from '@/hooks/use-payment';
import { ErrorMessage } from '@/components/common/ErrorMessage';
import { LoadingSpinner } from '@/components/common/LoadingSpinner';
import { useState } from 'react';
import { useRouter } from 'next/navigation';

const cancelPaymentSchema = z.object({
  cancelReason: z.string().min(1, '취소 사유를 입력해주세요.').max(200, '취소 사유는 최대 200자까지 입력 가능합니다.'),
  cancelAmount: z.number().positive().optional(),
  taxFreeAmount: z.number().min(0).optional(),
  currency: z.string().optional(),
  refundReceiveAccount: z.object({
    bank: z.string().min(1, '은행 코드를 입력해주세요.'),
    accountNumber: z.string().min(1, '계좌번호를 입력해주세요.').max(20, '계좌번호는 최대 20자까지 입력 가능합니다.'),
    holderName: z.string().min(1, '예금주를 입력해주세요.').max(60, '예금주는 최대 60자까지 입력 가능합니다.'),
  }).optional(),
  idempotencyKey: z.string().optional(),
});

type CancelPaymentFormData = z.infer<typeof cancelPaymentSchema>;

export interface CancelPaymentFormProps {
  paymentKey: string;
  totalAmount?: number;
}

export const CancelPaymentForm = ({ paymentKey, totalAmount }: CancelPaymentFormProps) => {
  const router = useRouter();
  const { cancelPayment, loading, error } = usePayment();
  const [success, setSuccess] = useState(false);
  const [showRefundAccount, setShowRefundAccount] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<CancelPaymentFormData>({
    resolver: zodResolver(cancelPaymentSchema),
    defaultValues: {
      cancelAmount: totalAmount,
      currency: 'KRW',
    },
  });

  const onSubmit = async (data: CancelPaymentFormData) => {
    try {
      await cancelPayment(paymentKey, data);
      setSuccess(true);
      setTimeout(() => {
        router.push('/payments/history');
      }, 2000);
    } catch (err) {
      // Error is handled by ErrorMessage component
    }
  };

  if (success) {
    return (
      <div className="bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded">
        <p className="font-medium">결제 취소 요청이 완료되었습니다.</p>
        <p className="text-sm">잠시 후 결제 내역 페이지로 이동합니다.</p>
      </div>
    );
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <div>
        <label htmlFor="cancelReason" className="block text-sm font-medium text-gray-700 mb-1">
          취소 사유 <span className="text-red-500">*</span>
        </label>
        <textarea
          id="cancelReason"
          {...register('cancelReason')}
          rows={4}
          className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          disabled={loading}
          placeholder="결제 취소 사유를 입력해주세요."
        />
        {errors.cancelReason && (
          <p className="mt-1 text-sm text-red-600">{errors.cancelReason.message}</p>
        )}
      </div>

      <div>
        <label htmlFor="cancelAmount" className="block text-sm font-medium text-gray-700 mb-1">
          취소 금액 {totalAmount && `(전체 금액: ${totalAmount.toLocaleString()}원)`}
        </label>
        <input
          id="cancelAmount"
          type="number"
          {...register('cancelAmount', { valueAsNumber: true })}
          max={totalAmount}
          className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          disabled={loading}
          placeholder={totalAmount ? `최대 ${totalAmount.toLocaleString()}원` : '취소할 금액을 입력해주세요'}
        />
        {errors.cancelAmount && (
          <p className="mt-1 text-sm text-red-600">{errors.cancelAmount.message}</p>
        )}
        <p className="mt-1 text-sm text-gray-500">값을 입력하지 않으면 전액 취소됩니다.</p>
      </div>

      <div>
        <label htmlFor="taxFreeAmount" className="block text-sm font-medium text-gray-700 mb-1">
          면세 금액
        </label>
        <input
          id="taxFreeAmount"
          type="number"
          {...register('taxFreeAmount', { valueAsNumber: true })}
          className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          disabled={loading}
        />
        {errors.taxFreeAmount && (
          <p className="mt-1 text-sm text-red-600">{errors.taxFreeAmount.message}</p>
        )}
      </div>

      <div>
        <label htmlFor="currency" className="block text-sm font-medium text-gray-700 mb-1">
          통화
        </label>
        <select
          id="currency"
          {...register('currency')}
          className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          disabled={loading}
        >
          <option value="KRW">KRW (원)</option>
          <option value="USD">USD (달러)</option>
          <option value="JPY">JPY (엔)</option>
        </select>
      </div>

      <div>
        <label className="flex items-center">
          <input
            type="checkbox"
            checked={showRefundAccount}
            onChange={(e) => setShowRefundAccount(e.target.checked)}
            className="mr-2"
            disabled={loading}
          />
          <span className="text-sm font-medium text-gray-700">가상계좌 환불 계좌 정보 입력</span>
        </label>
        <p className="mt-1 text-sm text-gray-500">가상계좌 결제 취소 시에만 필요합니다.</p>
      </div>

      {showRefundAccount && (
        <div className="space-y-4 p-4 bg-gray-50 rounded-md">
          <div>
            <label htmlFor="bank" className="block text-sm font-medium text-gray-700 mb-1">
              은행 코드 <span className="text-red-500">*</span>
            </label>
            <input
              id="bank"
              type="text"
              {...register('refundReceiveAccount.bank')}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              disabled={loading}
              placeholder="예: 20 (우리은행)"
            />
            {errors.refundReceiveAccount?.bank && (
              <p className="mt-1 text-sm text-red-600">{errors.refundReceiveAccount.bank.message}</p>
            )}
          </div>

          <div>
            <label htmlFor="accountNumber" className="block text-sm font-medium text-gray-700 mb-1">
              계좌번호 <span className="text-red-500">*</span>
            </label>
            <input
              id="accountNumber"
              type="text"
              {...register('refundReceiveAccount.accountNumber')}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              disabled={loading}
              placeholder="- 없이 숫자만 입력"
            />
            {errors.refundReceiveAccount?.accountNumber && (
              <p className="mt-1 text-sm text-red-600">{errors.refundReceiveAccount.accountNumber.message}</p>
            )}
          </div>

          <div>
            <label htmlFor="holderName" className="block text-sm font-medium text-gray-700 mb-1">
              예금주 <span className="text-red-500">*</span>
            </label>
            <input
              id="holderName"
              type="text"
              {...register('refundReceiveAccount.holderName')}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              disabled={loading}
            />
            {errors.refundReceiveAccount?.holderName && (
              <p className="mt-1 text-sm text-red-600">{errors.refundReceiveAccount.holderName.message}</p>
            )}
          </div>
        </div>
      )}

      <ErrorMessage error={error} />

      <div className="flex gap-4">
        <button
          type="submit"
          disabled={loading}
          className="flex-1 bg-red-600 text-white py-2 px-4 rounded-md hover:bg-red-700 disabled:bg-gray-400 disabled:cursor-not-allowed flex items-center justify-center"
        >
          {loading ? <LoadingSpinner size="sm" /> : '결제 취소 요청'}
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


