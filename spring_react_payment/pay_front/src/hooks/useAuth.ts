import { useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from '@/lib/services/authService';
import type { LoginRequest } from '@/types/api';

export const useAuth = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  const login = useCallback(async (credentials: LoginRequest) => {
    setLoading(true);
    setError(null);

    try {
      const response = await authService.login(credentials);
      if (response.data?.token) {
        localStorage.setItem('token', response.data.token);
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
  }, [navigate]);

  const logout = useCallback(() => {
    localStorage.removeItem('token');
    navigate('/login');
  }, [navigate]);

  return {
    login,
    logout,
    loading,
    error,
  };
};

