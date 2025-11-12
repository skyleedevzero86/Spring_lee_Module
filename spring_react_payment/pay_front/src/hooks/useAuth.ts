import { useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from '@/lib/services/authService';
import { useAuthStore } from '@/store/authStore';
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
      if (response.data?.token && response.data) {
        setAuth(
          {
            userId: response.data.userId,
            email: response.data.email,
            name: response.data.name,
            role: response.data.role,
          },
          response.data.token
        );
      }
      navigate('/');
      return response;
    } catch (err: any) {
      const errorMessage =
        err.response?.data?.message || '로그인에 실패했습니다.';
      setError(errorMessage);
      throw err;
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

