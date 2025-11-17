'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { TokenManager } from '@/lib/utils';
import { LoadingSpinner } from './LoadingSpinner';

interface ProtectedRouteProps {
  children: React.ReactNode;
  requireAdmin?: boolean;
}

export const ProtectedRoute = ({ children, requireAdmin = false }: ProtectedRouteProps) => {
  const router = useRouter();
  const [isValidating, setIsValidating] = useState(true);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [hasAdminRole, setHasAdminRole] = useState(true);

  useEffect(() => {
    const validateAuth = async () => {
      try {
        const isValid = await TokenManager.validateToken();
        setIsAuthenticated(isValid);
        
        if (!isValid) {
          setIsValidating(false);
          router.push('/login');
          return;
        }

        if (requireAdmin) {
          const currentRole = await TokenManager.getUserRole();
          const isAdmin = currentRole === 'ADMIN';
          setHasAdminRole(isAdmin);
          
          if (!isAdmin) {
            setIsValidating(false);
            router.push('/');
            return;
          }
        }
        
        setIsValidating(false);
      } catch (error) {
        console.error('인증 확인 실패:', error);
        setIsAuthenticated(false);
        setIsValidating(false);
        router.push('/login');
      }
    };

    validateAuth();
  }, [requireAdmin, router]);

  if (isValidating) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  if (!isAuthenticated) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  if (requireAdmin && !hasAdminRole) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <h2 className="text-2xl font-bold text-gray-900 mb-4">접근 권한이 없습니다</h2>
          <p className="text-gray-600">관리자만 접근할 수 있습니다.</p>
        </div>
      </div>
    );
  }

  return <>{children}</>;
};
