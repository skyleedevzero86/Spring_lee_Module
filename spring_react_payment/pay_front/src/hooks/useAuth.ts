import { useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from '@/lib/services/authService';
import { useAuthStore } from '@/store/authStore';
import { handleApiError } from '@/lib/errorHandler';
import type { LoginRequest } from '@/types/api';

export const useAuth = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();
  const { setAuth, clearAuth } = useAuthStore();

  const login = useCallback(async (credentials: LoginRequest) => {
    setLoading(true);
    setError(null);

    try {
      const response = await authService.login(credentials);
      if (!response?.data?.token || !response.data) {
        throw new Error('로그인 응답이 올바르지 않습니다.');
      }

      const { userId, email, name, role, token } = response.data;
      if (!userId || !email || !name || !role || !token) {
        throw new Error('로그인 정보가 불완전합니다.');
      }

      setAuth(
        {
          userId,
          email,
          name,
          role,
        },
        token
      );
      navigate('/');
      return response;
    } catch (err: unknown) {
      const errorMessage = handleApiError(err);
      setError(errorMessage);
      return;
    } finally {
      setLoading(false);
    }
  }, [navigate, setAuth]);

  const logout = useCallback(() => {
    clearAuth();
    navigate('/login');
  }, [navigate, clearAuth]);

  return {
    login,
    logout,
    loading,
    error,
  };
};

