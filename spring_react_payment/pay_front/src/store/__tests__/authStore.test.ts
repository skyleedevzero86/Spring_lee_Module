import { renderHook, act } from '@testing-library/react';
import { useAuthStore } from '../authStore';

describe('useAuthStore', () => {
  beforeEach(() => {
    localStorage.clear();
    const { result } = renderHook(() => useAuthStore());
    act(() => {
      result.current.clearAuth();
    });
  });

  describe('setAuth', () => {
    it('인증 정보를 저장해야 함', () => {
      // given
      const user = {
        userId: 1,
        email: 'test@example.com',
        name: '테스트 사용자',
        role: 'USER',
      };
      const token = 'jwt-token-123';

      // when
      const { result } = renderHook(() => useAuthStore());
      act(() => {
        result.current.setAuth(user, token);
      });

      // then
      expect(result.current.user).toEqual(user);
      expect(result.current.token).toBe(token);
    });
  });

  describe('clearAuth', () => {
    it('인증 정보를 제거해야 함', () => {
      // given
      const user = {
        userId: 1,
        email: 'test@example.com',
        name: '테스트 사용자',
        role: 'USER',
      };
      const token = 'jwt-token-123';
      const { result } = renderHook(() => useAuthStore());
      act(() => {
        result.current.setAuth(user, token);
      });

      // when
      act(() => {
        result.current.clearAuth();
      });

      // then
      expect(result.current.user).toBeNull();
      expect(result.current.token).toBeNull();
      expect(localStorage.getItem('token')).toBeNull();
    });
  });

  describe('isAuthenticated', () => {
    it('인증 정보가 있으면 true를 반환해야 함', () => {
      // given
      const user = {
        userId: 1,
        email: 'test@example.com',
        name: '테스트 사용자',
        role: 'USER',
      };
      const token = 'jwt-token-123';
      const { result } = renderHook(() => useAuthStore());
      act(() => {
        result.current.setAuth(user, token);
      });

      // when
      const isAuthenticated = result.current.isAuthenticated();

      // then
      expect(isAuthenticated).toBe(true);
    });

    it('인증 정보가 없으면 false를 반환해야 함', () => {
      // given
      const { result } = renderHook(() => useAuthStore());

      // when
      const isAuthenticated = result.current.isAuthenticated();

      // then
      expect(isAuthenticated).toBe(false);
    });

    it('user만 있고 token이 없으면 false를 반환해야 함', () => {
      // given
      const user = {
        userId: 1,
        email: 'test@example.com',
        name: '테스트 사용자',
        role: 'USER',
      };
      const { result } = renderHook(() => useAuthStore());
      act(() => {
        result.current.setAuth(user, 'token');
      });
      act(() => {
        result.current.clearAuth();
      });
      act(() => {
        result.current.setAuth(user, '');
      });

      // when
      const isAuthenticated = result.current.isAuthenticated();

      // then
      expect(isAuthenticated).toBe(false);
    });
  });

  describe('isAdmin', () => {
    it('관리자 역할이면 true를 반환해야 함', () => {
      // given
      const user = {
        userId: 1,
        email: 'admin@example.com',
        name: '관리자',
        role: 'ADMIN',
      };
      const token = 'jwt-token-123';
      const { result } = renderHook(() => useAuthStore());
      act(() => {
        result.current.setAuth(user, token);
      });

      // when
      const isAdmin = result.current.isAdmin();

      // then
      expect(isAdmin).toBe(true);
    });

    it('일반 사용자 역할이면 false를 반환해야 함', () => {
      // given
      const user = {
        userId: 1,
        email: 'test@example.com',
        name: '테스트 사용자',
        role: 'USER',
      };
      const token = 'jwt-token-123';
      const { result } = renderHook(() => useAuthStore());
      act(() => {
        result.current.setAuth(user, token);
      });

      // when
      const isAdmin = result.current.isAdmin();

      // then
      expect(isAdmin).toBe(false);
    });

    it('인증 정보가 없으면 false를 반환해야 함', () => {
      // given
      const { result } = renderHook(() => useAuthStore());

      // when
      const isAdmin = result.current.isAdmin();

      // then
      expect(isAdmin).toBe(false);
    });
  });
});





