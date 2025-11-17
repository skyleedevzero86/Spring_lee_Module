'use client';

import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useCashReceipt } from '@/hooks/use-cash-receipt';
import { ErrorMessage } from '@/components/common/ErrorMessage';
import { LoadingSpinner } from '@/components/common/LoadingSpinner';
import { useState } from 'react';
import { useRouter } from 'next/navigation';

const issueCashReceiptSchema = z.object({
  amount: z.number().positive('금액은 0보다 커야 합니다.'),
  orderId: z.string().min(6, '주문번호는 6자 이상이어야 합니다.').max(64, '주문번호는 64자 이하여야 합니다.'),
  orderName: z.string().min(1, '구매상품명을 입력해주세요.').max(100, '구매상품명은 최대 100자까지 입력 가능합니다.'),
  type: z.enum(['소득공제', '지출증빙'], {
    required_error: '현금영수증 종류를 선택해주세요.',
  }),
  customerIdentityNumber: z.string().min(1, '소비자 인증수단을 입력해주세요.').max(30, '소비자 인증수단은 최대 30자까지 입력 가능합니다.'),
  taxFreeAmount: z.number().min(0).optional(),
});

type IssueCashReceiptFormData = z.infer<typeof issueCashReceiptSchema>;

export const CashReceiptForm = () => {
  const router = useRouter();
  const { issueCashReceipt, loading, error } = useCashReceipt();
  const [success, setSuccess] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<IssueCashReceiptFormData>({
    resolver: zodResolver(issueCashReceiptSchema),
  });

  const onSubmit = async (data: IssueCashReceiptFormData) => {
    try {
      await issueCashReceipt(data);
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
        <p className="font-medium">현금영수증 발급 요청이 완료되었습니다.</p>
        <p className="text-sm">잠시 후 결제 내역 페이지로 이동합니다.</p>
      </div>
    );
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <div>
        <label htmlFor="amount" className="block text-sm font-medium text-gray-700 mb-1">
          금액 <span className="text-red-500">*</span>
        </label>
        <input
          id="amount"
          type="number"
          {...register('amount', { valueAsNumber: true })}
          className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          disabled={loading}
        />
        {errors.amount && (
          <p className="mt-1 text-sm text-red-600">{errors.amount.message}</p>
        )}
      </div>

      <div>
        <label htmlFor="orderId" className="block text-sm font-medium text-gray-700 mb-1">
          주문번호 <span className="text-red-500">*</span>
        </label>
        <input
          id="orderId"
          type="text"
          {...register('orderId')}
          className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          disabled={loading}
        />
        {errors.orderId && (
          <p className="mt-1 text-sm text-red-600">{errors.orderId.message}</p>
        )}
      </div>

      <div>
        <label htmlFor="orderName" className="block text-sm font-medium text-gray-700 mb-1">
          구매상품명 <span className="text-red-500">*</span>
        </label>
        <input
          id="orderName"
          type="text"
          {...register('orderName')}
          className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          disabled={loading}
          placeholder="예: 생수 외 1건"
        />
        {errors.orderName && (
          <p className="mt-1 text-sm text-red-600">{errors.orderName.message}</p>
        )}
      </div>

      <div>
        <label htmlFor="type" className="block text-sm font-medium text-gray-700 mb-1">
          현금영수증 종류 <span className="text-red-500">*</span>
        </label>
        <select
          id="type"
          {...register('type')}
          className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          disabled={loading}
        >
          <option value="">선택해주세요</option>
          <option value="소득공제">소득공제</option>
          <option value="지출증빙">지출증빙</option>
        </select>
        {errors.type && (
          <p className="mt-1 text-sm text-red-600">{errors.type.message}</p>
        )}
      </div>

      <div>
        <label htmlFor="customerIdentityNumber" className="block text-sm font-medium text-gray-700 mb-1">
          소비자 인증수단 <span className="text-red-500">*</span>
        </label>
        <input
          id="customerIdentityNumber"
          type="text"
          {...register('customerIdentityNumber')}
          className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          disabled={loading}
          placeholder="휴대폰 번호, 사업자등록번호, 현금영수증 카드 번호 등"
        />
        {errors.customerIdentityNumber && (
          <p className="mt-1 text-sm text-red-600">{errors.customerIdentityNumber.message}</p>
        )}
        <p className="mt-1 text-sm text-gray-500">
          소득공제: 휴대폰 번호 또는 현금영수증 카드 번호<br />
          지출증빙: 사업자등록번호
        </p>
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

      <ErrorMessage error={error} />

      <div className="flex gap-4">
        <button
          type="submit"
          disabled={loading}
          className="flex-1 bg-blue-600 text-white py-2 px-4 rounded-md hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed flex items-center justify-center"
        >
          {loading ? <LoadingSpinner size="sm" /> : '현금영수증 발급 요청'}
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

