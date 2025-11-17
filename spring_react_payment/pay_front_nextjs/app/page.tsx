'use client';

import Link from 'next/link';
import { TokenManager } from '@/lib/utils';
import { useEffect, useState } from 'react';

export default function HomePage() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isAdmin, setIsAdmin] = useState(false);

  useEffect(() => {
    const checkAuth = async () => {
      const authenticated = TokenManager.isAuthenticated();
      setIsAuthenticated(authenticated);
      
      if (authenticated) {
        const role = await TokenManager.getUserRole();
        setIsAdmin(role === 'ADMIN');
      }
    };
    checkAuth();
  }, []);

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center">
      <div className="text-center">
        <h1 className="text-4xl font-bold text-gray-900 mb-8">
          토스 페이먼츠 결제 시스템
        </h1>
        <div className="space-y-4">
          {!isAuthenticated ? (
            <>
              <Link
                href="/register"
                className="block bg-blue-600 text-white px-6 py-3 rounded-md hover:bg-blue-700 transition"
              >
                회원가입
              </Link>
              <Link
                href="/login"
                className="block bg-green-600 text-white px-6 py-3 rounded-md hover:bg-green-700 transition"
              >
                로그인
              </Link>
            </>
          ) : (
            <>
              <Link
                href="/payments"
                className="block bg-green-600 text-white px-6 py-3 rounded-md hover:bg-green-700 transition"
              >
                결제 관리
              </Link>
              {isAdmin && (
                <Link
                  href="/admin"
                  className="block bg-purple-600 text-white px-6 py-3 rounded-md hover:bg-purple-700 transition"
                >
                  관리자 대시보드
                </Link>
              )}
            </>
          )}
        </div>
      </div>
    </div>
  );
}
