import axios, { AxiosInstance, AxiosError, InternalAxiosRequestConfig } from 'axios';
import { ErrorResponse, ApiError } from '@/src/domain/types/error.types';
import { withRetry } from '@/src/lib/utils/retry';
import { TokenManager } from '@/src/lib/utils/token-manager';
import { sanitizeObject } from '@/src/lib/utils/security';
import { CsrfTokenManager } from '@/src/lib/utils/csrf';
import { API_TIMEOUT, STORAGE_KEYS } from '@/src/constants/api.constants';

class ApiClient {
  private client: AxiosInstance;

  constructor(baseURL: string) {
    this.client = axios.create({
      baseURL,
      timeout: API_TIMEOUT,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    this.setupInterceptors();
  }

  private setupInterceptors(): void {
    if (typeof window !== 'undefined') {
      CsrfTokenManager.initToken();
    }

    this.client.interceptors.request.use(
      (config: InternalAxiosRequestConfig) => {
        if (!TokenManager.isAuthenticated()) {
          TokenManager.clearToken();
        } else {
          TokenManager.refreshToken();
        }

        const userId = this.getUserId();
        const userRole = this.getUserRole();

        if (userId && config.headers) {
          config.headers['X-User-Id'] = String(userId);
        }

        if (userRole && config.headers) {
          config.headers['X-User-Role'] = userRole;
        }

        const csrfToken = CsrfTokenManager.getToken();
        if (csrfToken && config.headers) {
          config.headers[CsrfTokenManager.getHeaderName()] = csrfToken;
        }

        return config;
      },
      (error) => Promise.reject(error)
    );

    this.client.interceptors.response.use(
      (response) => response,
      (error: AxiosError<ErrorResponse>) => {
        if (error.response) {
          const errorResponse = error.response.data;
          const apiError = ApiError.fromResponse(
            errorResponse,
            error.response.status
          );
          return Promise.reject(apiError);
        }

        if (error.request) {
          return Promise.reject(
            new ApiError('NETWORK_ERROR', 0, '네트워크 오류가 발생했습니다.')
          );
        }

        return Promise.reject(
          new ApiError('UNKNOWN_ERROR', 0, '알 수 없는 오류가 발생했습니다.')
        );
      }
    );
  }

  private getUserId(): number | null {
    return TokenManager.getUserId();
  }

  private getUserRole(): string | null {
    return TokenManager.getUserRole();
  }

  setAuth(userId: number, role: string): void {
    TokenManager.setToken(userId, role);
  }

  clearAuth(): void {
    TokenManager.clearToken();
  }

  private sanitizeRequestData(data: unknown): unknown {
    if (data === null || data === undefined) {
      return data;
    }

    if (typeof data === 'string') {
      return data;
    }

    if (typeof data === 'object' && !Array.isArray(data)) {
      return sanitizeObject(data as Record<string, unknown>);
    }

    return data;
  }

  async get<T>(url: string, config?: InternalAxiosRequestConfig): Promise<T> {
    return withRetry(
      async () => {
        const response = await this.client.get<T>(url, config);
        return response.data;
      },
      {
        retryableErrorCodes: ['NETWORK_ERROR'],
        retryableStatusCodes: [408, 429, 500, 502, 503, 504],
      }
    );
  }

  async post<T>(
    url: string,
    data?: unknown,
    config?: InternalAxiosRequestConfig
  ): Promise<T> {
    const sanitizedData = this.sanitizeRequestData(data);
    return withRetry(
      async () => {
        const response = await this.client.post<T>(url, sanitizedData, config);
        return response.data;
      },
      {
        retryableErrorCodes: ['NETWORK_ERROR'],
        retryableStatusCodes: [408, 429, 500, 502, 503, 504],
      }
    );
  }

  async put<T>(
    url: string,
    data?: unknown,
    config?: InternalAxiosRequestConfig
  ): Promise<T> {
    const sanitizedData = this.sanitizeRequestData(data);
    return withRetry(
      async () => {
        const response = await this.client.put<T>(url, sanitizedData, config);
        return response.data;
      },
      {
        retryableErrorCodes: ['NETWORK_ERROR'],
        retryableStatusCodes: [408, 429, 500, 502, 503, 504],
      }
    );
  }

  async patch<T>(
    url: string,
    data?: unknown,
    config?: InternalAxiosRequestConfig
  ): Promise<T> {
    const sanitizedData = this.sanitizeRequestData(data);
    return withRetry(
      async () => {
        const response = await this.client.patch<T>(url, sanitizedData, config);
        return response.data;
      },
      {
        retryableErrorCodes: ['NETWORK_ERROR'],
        retryableStatusCodes: [408, 429, 500, 502, 503, 504],
      }
    );
  }

  async delete<T>(
    url: string,
    config?: InternalAxiosRequestConfig
  ): Promise<T> {
    return withRetry(
      async () => {
        const response = await this.client.delete<T>(url, config);
        return response.data;
      },
      {
        retryableErrorCodes: ['NETWORK_ERROR'],
        retryableStatusCodes: [408, 429, 500, 502, 503, 504],
      }
    );
  }
}

const apiClient = new ApiClient(
  process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080'
);

export default apiClient;

