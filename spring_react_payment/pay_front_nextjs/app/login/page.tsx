'use client';

import { LoginForm } from '@/components/member/LoginForm';
import { BackButton } from '@/components/common/BackButton';
import Link from 'next/link';

export default function LoginPage() {
  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8">
        <div>
          <div className="mb-4">
            <BackButton />
          </div>
          <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">
            로그??
          </h2>
          <p className="mt-2 text-center text-sm text-gray-600">
            계정???�으?��???{' '}
            <Link href="/register" className="font-medium text-blue-600 hover:text-blue-500">
              ?�원가??
            </Link>
          </p>
        </div>
        <LoginForm />
      </div>
    </div>
  );
}

