import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface User {
  userId: number;
  email: string;
  name: string;
  role: string;
}

interface AuthState {
  user: User | null;
  token: string | null;
  setAuth: (user: User, token: string) => void;
  clearAuth: () => void;
  isAuthenticated: () => boolean;
  isAdmin: () => boolean;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      token: null,
      setAuth: (user, token) => {
        localStorage.setItem('token', token);
        set({ user, token });
      },
      clearAuth: () => {
        localStorage.removeItem('token');
        localStorage.removeItem('auth-storage');
        set({ user: null, token: null });
      },
      isAuthenticated: () => {
        const state = get();
        return state.user !== null && state.token !== null;
      },
      isAdmin: () => {
        const state = get();
        return state.user?.role === 'ADMIN';
      },
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({ user: state.user, token: state.token }),
    }
  )
);

