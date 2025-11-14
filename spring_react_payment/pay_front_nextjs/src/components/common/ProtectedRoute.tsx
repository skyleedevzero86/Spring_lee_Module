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
  const userRole = TokenManager.getUserRole();

  useEffect(() => {
    const validateAuth = async () => {
      const hasToken = TokenManager.isAuthenticated();
      if (!hasToken) {
        setIsAuthenticated(false);
        setIsValidating(false);
        router.push('/login');
        return;
      }

      const isValid = await TokenManager.validateToken();
      setIsAuthenticated(isValid);
      setIsValidating(false);

      if (!isValid) {
        router.push('/login');
      } else if (requireAdmin && userRole !== 'ADMIN') {
        router.push('/');
      }
    };

    validateAuth();
  }, [requireAdmin, userRole, router]);

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

  if (requireAdmin && userRole !== 'ADMIN') {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <h2 className="text-2xl font-bold text-gray-900 mb-4">?�근 권한???�습?�다</h2>
          <p className="text-gray-600">관리자�??�근?????�습?�다.</p>
        </div>
      </div>
    );
  }

  return <>{children}</>;
};

