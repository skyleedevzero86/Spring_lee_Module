'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { useRouter, usePathname } from 'next/navigation';
import { TokenManager } from '@/lib/utils';

export const Header = () => {
  const router = useRouter();
  const pathname = usePathname();
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isAdmin, setIsAdmin] = useState(false);
  const [userEmail, setUserEmail] = useState<string | null>(null);

  useEffect(() => {
    const checkAuth = async () => {
      const authenticated = TokenManager.isAuthenticated();
      setIsAuthenticated(authenticated);

      if (authenticated) {
        const role = await TokenManager.getUserRole();
        setIsAdmin(role === 'ADMIN');
        // 이메일은 토큰에서 가져올 수 없으므로 null로 설정
        setUserEmail(null);
      }
    };

    checkAuth();

    // 주기적으로 인증 상태 확인
    const interval = setInterval(checkAuth, 5000);
    return () => clearInterval(interval);
  }, [pathname]);

  const handleLogout = async () => {
    await TokenManager.clearToken();
    setIsAuthenticated(false);
    setIsAdmin(false);
    router.push('/');
  };

  const isActive = (path: string) => {
    return pathname === path;
  };

  return (
    <header className="bg-white shadow-sm border-b border-gray-200">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          {/* 로고 및 메인 메뉴 */}
          <div className="flex items-center space-x-8">
            <Link href="/" className="flex items-center">
              <h1 className="text-xl font-bold text-gray-900">
                토스 페이먼츠
              </h1>
            </Link>

            {isAuthenticated && (
              <nav className="hidden md:flex space-x-4">
                {isAdmin ? (
                  // 관리자 전용 메뉴
                  <Link
                    href="/admin"
                    className={`px-3 py-2 rounded-md text-sm font-medium transition-colors ${
                      isActive('/admin')
                        ? 'bg-purple-100 text-purple-700'
                        : 'text-purple-600 hover:bg-purple-50'
                    }`}
                  >
                    관리자 대시보드
                  </Link>
                ) : (
                  // 일반 사용자 메뉴
                  <>
                    <Link
                      href="/payments/create"
                      className={`px-3 py-2 rounded-md text-sm font-medium transition-colors ${
                        isActive('/payments/create') || pathname.startsWith('/payments/create')
                          ? 'bg-blue-100 text-blue-700'
                          : 'text-gray-700 hover:bg-gray-100'
                      }`}
                    >
                      결제 생성
                    </Link>
                    <Link
                      href="/payments/history"
                      className={`px-3 py-2 rounded-md text-sm font-medium transition-colors ${
                        isActive('/payments/history') || pathname.startsWith('/payments/history')
                          ? 'bg-blue-100 text-blue-700'
                          : 'text-gray-700 hover:bg-gray-100'
                      }`}
                    >
                      결제 내역
                    </Link>
                  </>
                )}
              </nav>
            )}
          </div>

          {/* 우측 메뉴 */}
          {isAuthenticated && (
            <div className="flex items-center space-x-4">
              {isAdmin && (
                <>
                  <span className="hidden sm:inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-purple-100 text-purple-800">
                    관리자
                  </span>
                  <Link
                    href="/admin"
                    className={`px-4 py-2 text-sm font-medium rounded-md transition-colors ${
                      isActive('/admin') || pathname.startsWith('/admin')
                        ? 'bg-purple-100 text-purple-700'
                        : 'bg-purple-600 text-white hover:bg-purple-700'
                    }`}
                  >
                    전체 결제 이력
                  </Link>
                </>
              )}
              <button
                onClick={handleLogout}
                className="px-4 py-2 text-sm font-medium text-gray-700 hover:text-gray-900 hover:bg-gray-100 rounded-md transition-colors"
              >
                로그아웃
              </button>
            </div>
          )}
        </div>

        {/* 모바일 메뉴 */}
        {isAuthenticated && (
          <div className="md:hidden border-t border-gray-200 py-2">
            <nav className="flex space-x-4 overflow-x-auto">
              {isAdmin ? (
                // 관리자 전용 메뉴
                <Link
                  href="/admin"
                  className={`px-3 py-2 rounded-md text-sm font-medium whitespace-nowrap ${
                    isActive('/admin') || pathname.startsWith('/admin')
                      ? 'bg-purple-100 text-purple-700'
                      : 'text-purple-600 hover:bg-purple-50'
                  }`}
                >
                  관리자 대시보드
                </Link>
              ) : (
                // 일반 사용자 메뉴
                <>
                  <Link
                    href="/payments/create"
                    className={`px-3 py-2 rounded-md text-sm font-medium whitespace-nowrap ${
                      isActive('/payments/create') || pathname.startsWith('/payments/create')
                        ? 'bg-blue-100 text-blue-700'
                        : 'text-gray-700 hover:bg-gray-100'
                    }`}
                  >
                    결제 생성
                  </Link>
                  <Link
                    href="/payments/history"
                    className={`px-3 py-2 rounded-md text-sm font-medium whitespace-nowrap ${
                      isActive('/payments/history') || pathname.startsWith('/payments/history')
                        ? 'bg-blue-100 text-blue-700'
                        : 'text-gray-700 hover:bg-gray-100'
                    }`}
                  >
                    결제 내역
                  </Link>
                </>
              )}
            </nav>
          </div>
        )}
      </div>
    </header>
  );
};

