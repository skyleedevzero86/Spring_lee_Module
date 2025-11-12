import api from '../api';
import type { LoginRequest, LoginResponse, RegisterRequest, RegisterResponse } from '@/types/api';

const AUTH_BASE_URL = '/api/v1/users';

export const authService = {
  async login(credentials: LoginRequest): Promise<LoginResponse> {
    const response = await api.post<LoginResponse>(
      `${AUTH_BASE_URL}/login`,
      credentials
    );
    return response.data;
  },

  async register(data: RegisterRequest): Promise<RegisterResponse> {
    const response = await api.post<RegisterResponse>(
      `${AUTH_BASE_URL}/register`,
      data
    );
    return response.data;
  },
};

