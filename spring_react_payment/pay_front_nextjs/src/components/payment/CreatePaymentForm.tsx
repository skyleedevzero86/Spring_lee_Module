'use client';

import { memo } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { createPaymentSchema, type CreatePaymentFormData } from '@/src/domain/validators/payment.validator';
import { useCreatePayment } from '@/src/hooks/queries/use-payment-queries';
import { ErrorMessage } from '@/src/components/common/ErrorMessage';
import { ApiError } from '@/src/domain/types/error.types';
import { Input } from '@/src/components/common/Input';
import { Button } from '@/src/components/common/Button';
import { useState, useCallback, useMemo } from 'react';

export const CreatePaymentForm = memo(() => {
  const { mutate: createPayment, isPending: loading, error } = useCreatePayment();
  const [success, setSuccess] = useState(false);

  const defaultValues = useMemo(
    () => ({
      amountTaxFree: 0,
      retUrl:
        typeof window !== 'undefined'
          ? `${window.location.origin}/payments/success`
          : '',
      retCancelUrl:
        typeof window !== 'undefined'
          ? `${window.location.origin}/payments/cancel`
          : '',
    }),
    []
  );

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<CreatePaymentFormData>({
    resolver: zodResolver(createPaymentSchema),
    defaultValues,
  });

  const onSubmit = useCallback(
    (data: CreatePaymentFormData) => {
      createPayment(
        {
          ...data,
          expiredTime:
            data.expiredTime ||
            new Date(Date.now() + 3600000).toISOString(),
        },
        {
          onSuccess: (response) => {
            if (response.checkoutPage) {
              window.location.href = response.checkoutPage;
            } else {
              setSuccess(true);
            }
          },
        }
      );
    },
    [createPayment]
  );

  if (success) {
    return (
      <div className="bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded">
        <p className="font-medium">결제가 생성되었습니다.</p>
      </div>
    );
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <Input
        label="주문번호"
        type="text"
        {...register('orderNo')}
        error={errors.orderNo?.message}
        disabled={loading}
      />

      <Input
        label="상품 설명"
        type="text"
        {...register('productDesc')}
        error={errors.productDesc?.message}
        disabled={loading}
      />

      <Input
        label="결제 금액"
        type="number"
        {...register('amount', { valueAsNumber: true })}
        error={errors.amount?.message}
        disabled={loading}
      />

      <Input
        label="비과세 금액"
        type="number"
        {...register('amountTaxFree', { valueAsNumber: true })}
        error={errors.amountTaxFree?.message}
        disabled={loading}
        helperText="기본값: 0"
      />

      <Input
        label="결제 완료 URL"
        type="url"
        {...register('retUrl')}
        error={errors.retUrl?.message}
        disabled={loading}
      />

      <Input
        label="결제 취소 URL"
        type="url"
        {...register('retCancelUrl')}
        error={errors.retCancelUrl?.message}
        disabled={loading}
      />

      <ErrorMessage
        error={
          error instanceof ApiError
            ? error
            : error
              ? new ApiError('UNKNOWN_ERROR', 500, '알 수 없는 오류가 발생했습니다.')
              : null
        }
      />

      <Button
        type="submit"
        disabled={loading}
        loading={loading}
        className="w-full"
      >
        결제 생성
      </Button>
    </form>
  );
});

CreatePaymentForm.displayName = 'CreatePaymentForm';

