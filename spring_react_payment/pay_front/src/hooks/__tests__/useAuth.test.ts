import { renderHook, waitFor } from '@testing-library/react';
import { useAuth } from '../useAuth';
import { authService } from '@/lib/services/authService';
import { useAuthStore } from '@/store/authStore';
import { useNavigate } from 'react-router-dom';
import { handleApiError } from '@/lib/errorHandler';

jest.mock('@/lib/services/authService');
jest.mock('@/store/authStore');
jest.mock('react-router-dom', () => ({
  useNavigate: jest.fn(),
}));
jest.mock('@/lib/errorHandler');

describe('useAuth', () => {
  const mockNavigate = jest.fn();
  const mockSetAuth = jest.fn();
  const mockClearAuth = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    (useNavigate as jest.Mock).mockReturnValue(mockNavigate);
    (useAuthStore as jest.Mock).mockReturnValue({
      setAuth: mockSetAuth,
      clearAuth: mockClearAuth,
    });
  });

  describe('login', () => {
    it('로그인이 성공하면 인증 정보를 저장하고 홈으로 이동해야 함', async () => {
      // given
      const credentials = {
        email: 'test@example.com',
        password: 'password123',
      };
      const mockResponse = {
        message: '로그인 성공',
        data: {
          userId: 1,
          email: 'test@example.com',
          name: '테스트 사용자',
          role: 'USER',
          token: 'jwt-token-123',
        },
      };
      (authService.login as jest.Mock).mockResolvedValue(mockResponse);

      // when
      const { result } = renderHook(() => useAuth());
      await result.current.login(credentials);

      // then
      await waitFor(() => {
        expect(authService.login).toHaveBeenCalledWith(credentials);
        expect(mockSetAuth).toHaveBeenCalledWith(
          {
            userId: 1,
            email: 'test@example.com',
            name: '테스트 사용자',
            role: 'USER',
          },
          'jwt-token-123'
        );
        expect(mockNavigate).toHaveBeenCalledWith('/');
      });
    });

    it('로그인 응답에 token이 없으면 에러를 설정해야 함', async () => {
      // given
      const credentials = {
        email: 'test@example.com',
        password: 'password123',
      };
      const mockResponse = {
        message: '로그인 성공',
        data: {
          userId: 1,
          email: 'test@example.com',
          name: '테스트 사용자',
          role: 'USER',
        },
      };
      (authService.login as jest.Mock).mockResolvedValue(mockResponse);

      // when
      const { result } = renderHook(() => useAuth());
      await result.current.login(credentials);

      // then
      await waitFor(() => {
        expect(result.current.error).toBe('로그인 응답이 올바르지 않습니다.');
      });
    });

    it('로그인 응답에 필수 필드가 없으면 에러를 설정해야 함', async () => {
      // given
      const credentials = {
        email: 'test@example.com',
        password: 'password123',
      };
      const mockResponse = {
        message: '로그인 성공',
        data: {
          userId: 1,
          email: 'test@example.com',
          token: 'jwt-token-123',
        },
      };
      (authService.login as jest.Mock).mockResolvedValue(mockResponse);

      // when
      const { result } = renderHook(() => useAuth());
      await result.current.login(credentials);

      // then
      await waitFor(() => {
        expect(result.current.error).toBe('로그인 정보가 불완전합니다.');
      });
    });

    it('로그인이 실패하면 에러를 설정해야 함', async () => {
      // given
      const credentials = {
        email: 'test@example.com',
        password: 'wrong-password',
      };
      const error = new Error('로그인 실패');
      const errorMessage = '인증에 실패했습니다.';
      (authService.login as jest.Mock).mockRejectedValue(error);
      (handleApiError as jest.Mock).mockReturnValue(errorMessage);

      // when
      const { result } = renderHook(() => useAuth());
      await result.current.login(credentials);

      // then
      await waitFor(() => {
        expect(result.current.error).toBe(errorMessage);
      });
    });

    it('로그인 중에는 loading 상태가 true여야 함', async () => {
      // given
      const credentials = {
        email: 'test@example.com',
        password: 'password123',
      };
      let resolvePromise: (value: any) => void;
      const promise = new Promise((resolve) => {
        resolvePromise = resolve;
      });
      (authService.login as jest.Mock).mockReturnValue(promise);

      // when
      const { result } = renderHook(() => useAuth());
      result.current.login(credentials);

      // then
      expect(result.current.loading).toBe(true);
      resolvePromise!({
        message: '로그인 성공',
        data: {
          userId: 1,
          email: 'test@example.com',
          name: '테스트 사용자',
          role: 'USER',
          token: 'jwt-token-123',
        },
      });
      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });
    });
  });

  describe('logout', () => {
    it('로그아웃하면 인증 정보를 제거하고 로그인 페이지로 이동해야 함', () => {
      // given
      const { result } = renderHook(() => useAuth());

      // when
      result.current.logout();

      // then
      expect(mockClearAuth).toHaveBeenCalled();
      expect(mockNavigate).toHaveBeenCalledWith('/login');
    });
  });
});



