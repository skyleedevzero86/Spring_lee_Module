'use client';

import { memo, useCallback } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useMember } from '@/src/hooks/use-member';
import { ErrorMessage } from '@/src/components/common/ErrorMessage';
import { Input } from '@/src/components/common/Input';
import { Button } from '@/src/components/common/Button';
import { useState } from 'react';
import { useRouter } from 'next/navigation';

const loginSchema = z.object({
  email: z.string().min(1, '이메일은 필수입니다.').email('올바른 이메일 형식이 아닙니다.'),
  password: z.string().min(1, '비밀번호는 필수입니다.'),
});

type LoginFormData = z.infer<typeof loginSchema>;

export const LoginForm = memo(() => {
  const router = useRouter();
  const { login, loginLoading, loginError } = useMember();
  const [localError, setLocalError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
  });

  const onSubmit = useCallback(
    async (data: LoginFormData) => {
      try {
        setLocalError(null);
        await login({
          email: data.email,
          password: data.password,
        });
        router.push('/payments');
      } catch (err) {
        setLocalError('로그인에 실패했습니다. 이메일과 비밀번호를 확인해주세요.');
      }
    },
    [login, router]
  );

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <Input
        label="이메일"
        type="email"
        {...register('email')}
        error={errors.email?.message}
        disabled={loginLoading}
      />

      <Input
        label="비밀번호"
        type="password"
        {...register('password')}
        error={errors.password?.message}
        disabled={loginLoading}
      />

      {loginError && <ErrorMessage error={loginError} />}
      {localError && (
        <div className="text-red-600 text-sm mt-1">{localError}</div>
      )}

      <Button
        type="submit"
        disabled={loginLoading}
        loading={loginLoading}
        className="w-full"
      >
        로그인
      </Button>
    </form>
  );
});

LoginForm.displayName = 'LoginForm';

