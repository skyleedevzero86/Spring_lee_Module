'use client';

import { RegisterForm } from '@/src/components/member/RegisterForm';
import { BackButton } from '@/src/components/common/BackButton';

export default function RegisterPage() {
  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8">
        <div>
          <div className="mb-4">
            <BackButton />
          </div>
          <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">
            회원가입
          </h2>
        </div>
        <RegisterForm />
      </div>
    </div>
  );
}

