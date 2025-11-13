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
import { useMemberStore } from '@/src/store/member.store';
import apiClient from '@/src/infrastructure/http/api-client';

const loginSchema = z.object({
  email: z.string().min(1, '이메일은 필수입니다.').email('올바른 이메일 형식이 아닙니다.'),
  password: z.string().min(1, '비밀번호는 필수입니다.'),
});

type LoginFormData = z.infer<typeof loginSchema>;

export const LoginForm = memo(() => {
  const router = useRouter();
  const { setMember } = useMemberStore();
  const { findByEmail, loading, error } = useMember();
  const [loginError, setLoginError] = useState<string | null>(null);

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
        setLoginError(null);
        const member = await findByEmail(data.email);
        
        if (!member) {
          setLoginError('이메일 또는 비밀번호가 올바르지 않습니다.');
          return;
        }

        apiClient.setAuth(member.id, member.role);
        setMember({
          id: member.id,
          email: member.email,
          name: member.name,
          role: member.role,
        });

        router.push('/payments');
      } catch (err) {
        setLoginError('로그인에 실패했습니다. 이메일과 비밀번호를 확인해주세요.');
      }
    },
    [findByEmail, setMember, router]
  );

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
      />

      {error && <ErrorMessage error={error} />}
      {loginError && (
        <div className="text-red-600 text-sm mt-1">{loginError}</div>
      )}

      <Button
        type="submit"
        disabled={loading}
        loading={loading}
        className="w-full"
      >
        로그인
      </Button>
    </form>
  );
});

LoginForm.displayName = 'LoginForm';

