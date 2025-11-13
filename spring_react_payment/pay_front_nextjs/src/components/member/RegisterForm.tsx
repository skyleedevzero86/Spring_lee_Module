'use client';

import { memo } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { registerMemberSchema, type RegisterMemberFormData } from '@/src/domain/validators/member.validator';
import { useMember } from '@/src/hooks/use-member';
import { ErrorMessage } from '@/src/components/common/ErrorMessage';
import { Input } from '@/src/components/common/Input';
import { Button } from '@/src/components/common/Button';
import { useState, useCallback } from 'react';
import { useRouter } from 'next/navigation';

export const RegisterForm = memo(() => {
  const router = useRouter();
  const { register: registerMember, loading, error } = useMember();
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
      }
    },
    [registerMember, router]
  );

  if (success) {
    return (
      <div className="bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded">
        <p className="font-medium">회원가입이 완료되었습니다.</p>
        <p className="text-sm">잠시 후 결제 페이지로 이동합니다.</p>
      </div>
    );
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <Input
        label="이메일"
        type="email"
        {...register('email')}
        error={errors.email?.message}
        disabled={loading}
      />

      <Input
        label="비밀번호"
        type="password"
        {...register('password')}
        error={errors.password?.message}
        disabled={loading}
        helperText="최소 8자 이상이어야 합니다."
      />

      <Input
        label="이름"
        type="text"
        {...register('name')}
        error={errors.name?.message}
        disabled={loading}
      />

      <ErrorMessage error={error} />

      <Button
        type="submit"
        disabled={loading}
        loading={loading}
        className="w-full"
      >
        회원가입
      </Button>
    </form>
  );
});

RegisterForm.displayName = 'RegisterForm';

