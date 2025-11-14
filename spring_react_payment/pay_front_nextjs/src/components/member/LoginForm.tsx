'use client';

import { memo, useCallback } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useMember } from '@/hooks/use-member';
import { ErrorMessage } from '@/components/common/ErrorMessage';
import { Input } from '@/components/common/Input';
import { Button } from '@/components/common/Button';
import { useState } from 'react';
import { useRouter } from 'next/navigation';

const loginSchema = z.object({
  email: z.string().min(1, '?´ë©”?¼ì? ?„ìˆ˜?…ë‹ˆ??').email('?¬ë°”ë¥??´ë©”???•ì‹???„ë‹™?ˆë‹¤.'),
  password: z.string().min(1, 'ë¹„ë?ë²ˆí˜¸???„ìˆ˜?…ë‹ˆ??'),
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
        setLocalError('ë¡œê·¸?¸ì— ?¤íŒ¨?ˆìŠµ?ˆë‹¤. ?´ë©”?¼ê³¼ ë¹„ë?ë²ˆí˜¸ë¥??•ì¸?´ì£¼?¸ìš”.');
      }
    },
    [login, router]
  );

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <Input
        label="?´ë©”??
        type="email"
        {...register('email')}
        error={errors.email?.message}
        disabled={loginLoading}
      />

      <Input
        label="ë¹„ë?ë²ˆí˜¸"
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
        ë¡œê·¸??      </Button>
    </form>
  );
});

LoginForm.displayName = 'LoginForm';

