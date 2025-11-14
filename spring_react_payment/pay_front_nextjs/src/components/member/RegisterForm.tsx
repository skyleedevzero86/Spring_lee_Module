'use client';

import { memo } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { registerMemberSchema, type RegisterMemberFormData } from '@/domain/validators/member.validator';
import { useMember } from '@/hooks/use-member';
import { ErrorMessage } from '@/components/common/ErrorMessage';
import { Input } from '@/components/common/Input';
import { Button } from '@/components/common/Button';
import { useState, useCallback } from 'react';
import { useRouter } from 'next/navigation';

export const RegisterForm = memo(() => {
  const router = useRouter();
  const { register: registerMember, registerLoading, registerError } = useMember();
  const [success, setSuccess] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<RegisterMemberFormData>({
    resolver: zodResolver(registerMemberSchema),
  });

  const onSubmit = useCallback(
    async (data: RegisterMemberFormData) => {
      try {
        await registerMember(data);
        setSuccess(true);
        setTimeout(() => {
          router.push('/payments');
        }, 2000);
      } catch (err) {
        if (process.env.NODE_ENV === 'development') {
          console.error('?�원가???�패:', err);
        }
      }
    },
    [registerMember, router]
  );

  if (success) {
    return (
      <div className="bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded">
        <p className="font-medium">?�원가?�이 ?�료?�었?�니??</p>
        <p className="text-sm">?�시 ??결제 ?�이지�??�동?�니??</p>
      </div>
    );
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <Input
        label="?�메??
        type="email"
        {...register('email')}
        error={errors.email?.message}
        disabled={registerLoading}
      />

      <Input
        label="비�?번호"
        type="password"
        {...register('password')}
        error={errors.password?.message}
        disabled={registerLoading}
        helperText="최소 8???�상?�어???�니??"
      />

      <Input
        label="?�름"
        type="text"
        {...register('name')}
        error={errors.name?.message}
        disabled={registerLoading}
      />

      <ErrorMessage error={registerError} />

      <Button
        type="submit"
        disabled={registerLoading}
        loading={registerLoading}
        className="w-full"
      >
        ?�원가??
      </Button>
    </form>
  );
});

RegisterForm.displayName = 'RegisterForm';

