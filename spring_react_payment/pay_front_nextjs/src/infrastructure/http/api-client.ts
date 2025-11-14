import axios, { AxiosInstance, AxiosError, InternalAxiosRequestConfig } from 'axios';
import { ErrorResponse, ApiError } from '@/domain/types/error.types';
import { withRetry } from '@/lib/utils/retry';
import { TokenManager } from '@/lib/utils/token-manager';
import { sanitizeObject, sanitizeInput } from '@/lib/utils/security';
import { CsrfTokenManager } from '@/lib/utils/csrf';
import { API_TIMEOUT, STORAGE_KEYS } from '@/constants/api.constants';

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
      CsrfTokenManager.initToken().catch((error) => {
        if (process.env.NODE_ENV === 'development') {
          console.warn('CSRF ? í° ì´ˆê¸°???¤íŒ¨:', error);
        }
      });
    }

    this.client.interceptors.request.use(
      (config: InternalAxiosRequestConfig) => {
        const token = TokenManager.getJwtToken();
        if (token && config.headers) {
          config.headers['Authorization'] = `Bearer ${token}`;
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
          if (process.env.NODE_ENV === 'development') {
            console.error('API ?‘ë‹µ ?ëŸ¬:', {
              status: error.response.status,
              statusText: error.response.statusText,
              url: error.config?.url,
              method: error.config?.method,
              data: error.response.data,
            });
          }

          if (error.response.status === 401) {
            TokenManager.clearToken();
            if (typeof window !== 'undefined') {
              window.location.href = '/login';
            }
          }

          const errorResponse = error.response.data;
          
          if (error.response.status === 500) {
            const message = errorResponse?.message || '?œë²„ ?´ë? ?¤ë¥˜ê°€ ë°œìƒ?ˆìŠµ?ˆë‹¤.';
            const detail = errorResponse?.detail || 
              (process.env.NODE_ENV === 'development' 
                ? `?œë²„ê°€ ?”ì²­??ì²˜ë¦¬?˜ëŠ” ì¤??¤ë¥˜ê°€ ë°œìƒ?ˆìŠµ?ˆë‹¤. (${error.config?.url})`
                : '? ì‹œ ???¤ì‹œ ?œë„?´ì£¼?¸ìš”.');
            
            return Promise.reject(
              new ApiError(
                errorResponse?.code || 'INTERNAL_SERVER_ERROR',
                error.response.status,
                message,
                detail
              )
            );
          }
          
          const apiError = ApiError.fromResponse(
            errorResponse,
            error.response.status
          );
          return Promise.reject(apiError);
        }

        if (error.request) {
          const isConnectionRefused = 
            error.code === 'ECONNREFUSED' || 
            error.message?.includes('ERR_CONNECTION_REFUSED') ||
            error.message?.includes('ê°€?¸ì˜¤ê¸??¤íŒ¨') ||
            error.message?.includes('Failed to fetch');
          
          if (process.env.NODE_ENV === 'development') {
            console.error('API ?”ì²­ ?¤íŒ¨:', {
              url: error.config?.url,
              method: error.config?.method,
              baseURL: error.config?.baseURL,
              code: error.code,
              message: error.message,
            });
          }
          
          return Promise.reject(
            new ApiError(
              'NETWORK_ERROR', 
              0, 
              isConnectionRefused 
                ? '?œë²„???°ê²°?????†ìŠµ?ˆë‹¤. ?œë²„ê°€ ?¤í–‰ ì¤‘ì¸ì§€ ?•ì¸?´ì£¼?¸ìš”.'
                : '?¤íŠ¸?Œí¬ ?¤ë¥˜ê°€ ë°œìƒ?ˆìŠµ?ˆë‹¤.'
            )
          );
        }

        return Promise.reject(
          new ApiError('UNKNOWN_ERROR', 0, '?????†ëŠ” ?¤ë¥˜ê°€ ë°œìƒ?ˆìŠµ?ˆë‹¤.')
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

  setAuth(userId: number, role: string, token?: string): void {
    TokenManager.setToken(userId, role, token);
  }

  clearAuth(): void {
    TokenManager.clearToken();
  }

  private sanitizeRequestData(data: unknown): unknown {
    if (data === null || data === undefined) {
      return data;
    }

    if (typeof data === 'string') {
      return sanitizeInput(data);
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

