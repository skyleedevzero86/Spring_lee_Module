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
          console.error('?Œì›ê°€???¤íŒ¨:', err);
        }
      }
    },
    [registerMember, router]
  );

  if (success) {
    return (
      <div className="bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded">
        <p className="font-medium">?Œì›ê°€?…ì´ ?„ë£Œ?˜ì—ˆ?µë‹ˆ??</p>
        <p className="text-sm">? ì‹œ ??ê²°ì œ ?˜ì´ì§€ë¡??´ë™?©ë‹ˆ??</p>
      </div>
    );
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      <Input
        label="?´ë©”??
        type="email"
        {...register('email')}
        error={errors.email?.message}
        disabled={registerLoading}
      />

      <Input
        label="ë¹„ë?ë²ˆí˜¸"
        type="password"
        {...register('password')}
        error={errors.password?.message}
        disabled={registerLoading}
        helperText="ìµœì†Œ 8???´ìƒ?´ì–´???©ë‹ˆ??"
      />

      <Input
        label="?´ë¦„"
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
        ?Œì›ê°€??
      </Button>
    </form>
  );
});

RegisterForm.displayName = 'RegisterForm';

