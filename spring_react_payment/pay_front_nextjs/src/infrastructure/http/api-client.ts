import axios, { AxiosInstance, AxiosError, InternalAxiosRequestConfig } from 'axios';
import { ErrorResponse, ApiError } from '@/domain/types/error.types';
import {
  withRetry,
  TokenManager,
  sanitizeObject,
  sanitizeInput,
  logger,
  CsrfTokenManager,
} from '@/lib/utils';
import { API_TIMEOUT, STORAGE_KEYS } from '@/constants/api.constants';

class ApiClient {
  private client: AxiosInstance;

  constructor(baseURL: string) {
    this.client = axios.create({
      baseURL,
      timeout: API_TIMEOUT,
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
      },
    });

    this.setupInterceptors();
  }

  private setupInterceptors(): void {
    if (typeof window !== 'undefined') {
      CsrfTokenManager.initToken().catch((error) => {
        logger.warn('CSRF 토큰 초기화 실패', { error });
      });
    }

    this.client.interceptors.request.use(
      async (config: InternalAxiosRequestConfig) => {
        try {
          const token = await TokenManager.getJwtToken();
          if (token && config.headers) {
            config.headers['Authorization'] = `Bearer ${token}`;
          }
        } catch (error) {
          const syncToken = TokenManager.getJwtTokenSync();
          if (syncToken && config.headers) {
            config.headers['Authorization'] = `Bearer ${syncToken}`;
          }
        }

        const userId = await this.getUserIdAsync();
        const userRole = await this.getUserRoleAsync();

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

        if (process.env.NODE_ENV === 'development') {
          logger.debug('API 요청 시작', {
            method: config.method?.toUpperCase(),
            url: config.url,
            baseURL: config.baseURL,
            headers: Object.keys(config.headers || {}),
          });
        }

        return config;
      },
      (error) => {
        logger.error('요청 설정 중 오류 발생', { error });
        return Promise.reject(error);
      }
    );

    this.client.interceptors.response.use(
      (response) => response,
      (error: AxiosError<ErrorResponse>) => {
        if (error.response) {
          const status = error.response.status;
          const statusText = error.response.statusText;
          const url = error.config?.url;
          const method = error.config?.method;
          const data = error.response.data;

          if (status >= 400 && status < 500) {
            logger.warn('API 클라이언트 에러', {
              status,
              statusText,
              url,
              method,
              data,
            });
          } else if (status >= 500) {
            logger.error('API 응답 에러', {
              status,
              statusText,
              url,
              method,
              data,
            });
            
            const errorCode = data?.code;
            if (errorCode === 'C002') {
              logger.warn('서버 내부 오류 (C002) - 캐시 문제일 수 있음', {
                url,
                method,
                code: errorCode,
                message: data?.message,
              });
            }
          }

          if (status === 401) {
            TokenManager.clearTokenSync();
            if (typeof window !== 'undefined') {
              window.location.href = '/login';
            }
          }

          const errorResponse = error.response.data;
          
          if (status === 429) {
            const retryAfterHeader = error.response.headers['retry-after'];
            const retryAfter = retryAfterHeader 
              ? parseInt(retryAfterHeader, 10) 
              : undefined;
            
            logger.warn('Rate limit 초과 (429)', {
              url,
              method,
              retryAfter,
              rateLimitLimit: error.response.headers['x-ratelimit-limit'],
              rateLimitRemaining: error.response.headers['x-ratelimit-remaining'],
              rateLimitReset: error.response.headers['x-ratelimit-reset'],
            });
          }
          
          if (status === 500) {
            const message = errorResponse?.message || '서버 내부 오류가 발생했습니다.';
            const detail = errorResponse?.detail || 
              (process.env.NODE_ENV === 'development' 
                ? `서버가 요청을 처리하는 중 오류가 발생했습니다. (${url})`
                : '잠시 후 다시 시도해주세요.');
            
            return Promise.reject(
              new ApiError(
                errorResponse?.code || 'INTERNAL_SERVER_ERROR',
                status,
                message,
                detail
              )
            );
          }
          
          const retryAfterHeader = error.response.headers['retry-after'];
          const retryAfter = retryAfterHeader 
            ? parseInt(retryAfterHeader, 10) 
            : undefined;
          
          const apiError = ApiError.fromResponse(
            errorResponse,
            status,
            retryAfter
          );
          return Promise.reject(apiError);
        }

        if (error.request) {
          const isConnectionRefused = 
            error.code === 'ECONNREFUSED' || 
            error.code === 'ERR_NETWORK' ||
            error.message?.includes('ERR_CONNECTION_REFUSED') ||
            error.message?.includes('ERR_NETWORK') ||
            error.message?.includes('가져오기 실패') ||
            error.message?.includes('Failed to fetch') ||
            error.message?.includes('Network Error');
          
          const baseURL = error.config?.baseURL || '알 수 없음';
          const url = error.config?.url || '알 수 없음';
          const fullUrl = `${baseURL}${url}`;
          
          logger.error('Network Error', {
            url: error.config?.url,
            method: error.config?.method,
            baseURL: error.config?.baseURL,
            fullUrl,
            code: error.code,
            message: error.message,
            request: error.request ? {
              readyState: (error.request as any).readyState,
              status: (error.request as any).status,
              statusText: (error.request as any).statusText,
            } : null,
            stack: error.stack,
          });
          
          let errorMessage = '네트워크 오류가 발생했습니다.';
          let errorDetail = '';
          
          if (isConnectionRefused || error.code === 'ERR_NETWORK') {
            errorMessage = '서버에 연결할 수 없습니다.';
            errorDetail = `백엔드 서버(${baseURL})가 실행 중인지 확인해주세요. ` +
                         `서버가 실행 중이라면 방화벽이나 네트워크 설정을 확인해주세요. ` +
                         `요청 URL: ${fullUrl}`;
          }
          
          return Promise.reject(
            new ApiError(
              'NETWORK_ERROR', 
              0, 
              errorMessage,
              errorDetail
            )
          );
        }

        return Promise.reject(
          new ApiError('UNKNOWN_ERROR', 0, '알 수 없는 오류가 발생했습니다.')
        );
      }
    );
  }

  private async getUserIdAsync(): Promise<number | null> {
    try {
      return await TokenManager.getUserId();
    } catch (error) {
      return TokenManager.getUserIdSync();
    }
  }

  private async getUserRoleAsync(): Promise<string | null> {
    try {
      return await TokenManager.getUserRole();
    } catch (error) {
      return TokenManager.getUserRoleSync();
    }
  }

  private getUserId(): number | null {
    return TokenManager.getUserIdSync();
  }

  private getUserRole(): string | null {
    return TokenManager.getUserRoleSync();
  }

  setAuth(userId: number, role: string, token?: string): void {
    TokenManager.setToken(userId, role, token).catch((error) => {
      console.error('토큰 저장 실패:', error);
    });
  }

  clearAuth(): void {
    TokenManager.clearToken().catch((error) => {
      console.error('토큰 삭제 실패:', error);
    });
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
        try {
          const response = await this.client.get<T>(url, config);
          return response.data;
        } catch (error) {
          if (axios.isAxiosError(error) && error.response?.status === 500) {
            const errorData = error.response.data as any;
            if (errorData?.code === 'C002') {
              logger.warn('서버 내부 오류 (C002) - 재시도하지 않음', {
                url,
                code: errorData.code,
                message: errorData.message,
              });
              throw error;
            }
          }
          throw error;
        }
      },
      {
        retryableErrorCodes: ['NETWORK_ERROR'],
        retryableStatusCodes: [408, 429, 502, 503, 504],
        maxRetries: 2,
      }
    );
  }

  async post<T>(
    url: string,
    data?: unknown,
    config?: InternalAxiosRequestConfig
  ): Promise<T> {
    const shouldSanitize = !url.includes('/login') && !url.includes('/register');
    const requestData = shouldSanitize ? this.sanitizeRequestData(data) : data;
    return withRetry(
      async () => {
        const response = await this.client.post<T>(url, requestData, config);
        return response.data;
      },
      {
        retryableErrorCodes: ['NETWORK_ERROR'],
        retryableStatusCodes: [408, 429, 500, 502, 503, 504],
        // 429 에러는 Retry-After 헤더가 있을 때만 재시도
        maxRetries: 2, // 429 에러는 최대 2번만 재시도
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
        // 429 에러는 Retry-After 헤더가 있을 때만 재시도
        maxRetries: 2, // 429 에러는 최대 2번만 재시도
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
        // 429 에러는 Retry-After 헤더가 있을 때만 재시도
        maxRetries: 2, // 429 에러는 최대 2번만 재시도
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
        // 429 에러는 Retry-After 헤더가 있을 때만 재시도
        maxRetries: 2, // 429 에러는 최대 2번만 재시도
      }
    );
  }
}

const apiClient = new ApiClient(
  process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080'
);

export default apiClient;
