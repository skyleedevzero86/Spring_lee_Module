import apiClient from './api-client';
import { ApiError } from '@/src/domain/types/error.types';
import { STORAGE_KEYS } from '@/src/constants/api.constants';
import axios from 'axios';

jest.mock('axios');
const mockedAxios = axios as jest.Mocked<typeof axios>;

describe('ApiClient', () => {
  const baseURL = 'http://localhost:8080';
  let mockAxiosInstance: any;

  beforeEach(() => {
    localStorage.clear();
    mockAxiosInstance = {
      get: jest.fn(),
      post: jest.fn(),
      put: jest.fn(),
      patch: jest.fn(),
      delete: jest.fn(),
      interceptors: {
        request: {
          use: jest.fn(),
        },
        response: {
          use: jest.fn(),
        },
      },
    };

    mockedAxios.create.mockReturnValue(mockAxiosInstance as any);
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  describe('setAuth', () => {
    it('인증 정보를 localStorage에 저장해야 함', () => {
      const userId = 1;
      const role = 'USER';

      apiClient.setAuth(userId, role);

      expect(localStorage.getItem(STORAGE_KEYS.USER_ID)).toBe('1');
      expect(localStorage.getItem(STORAGE_KEYS.USER_ROLE)).toBe(role);
    });
  });

  describe('clearAuth', () => {
    it('인증 정보를 localStorage에서 제거해야 함', () => {
      localStorage.setItem(STORAGE_KEYS.USER_ID, '1');
      localStorage.setItem(STORAGE_KEYS.USER_ROLE, 'USER');

      apiClient.clearAuth();

      expect(localStorage.getItem(STORAGE_KEYS.USER_ID)).toBeNull();
      expect(localStorage.getItem(STORAGE_KEYS.USER_ROLE)).toBeNull();
    });
  });

  describe('get', () => {
    it('GET 요청 성공', async () => {
      const mockData = { id: 1, name: '테스트' };
      mockAxiosInstance.get.mockResolvedValue({ data: mockData });

      const result = await apiClient.get('/api/test');

      expect(result).toEqual(mockData);
      expect(mockAxiosInstance.get).toHaveBeenCalledWith('/api/test', undefined);
    });
  });

  describe('post', () => {
    it('POST 요청 성공', async () => {
      const requestData = { name: '테스트' };
      const mockData = { id: 1, name: '테스트' };
      mockAxiosInstance.post.mockResolvedValue({ data: mockData });

      const result = await apiClient.post('/api/test', requestData);

      expect(result).toEqual(mockData);
      expect(mockAxiosInstance.post).toHaveBeenCalledWith('/api/test', requestData, undefined);
    });
  });

  describe('에러 처리', () => {
    it('서버 에러 응답 시 ApiError로 변환', () => {
      const errorResponse = {
        code: 'VALIDATION_ERROR',
        message: '유효성 검사 실패',
        timestamp: '2024-01-01T00:00:00Z',
      };

      const axiosError = {
        response: {
          status: 400,
          data: errorResponse,
        },
      } as any;

      const responseInterceptor = mockAxiosInstance.interceptors.response.use.mock.calls[0][1];

      try {
        responseInterceptor(axiosError);
      } catch (error) {
        expect(error).toBeInstanceOf(ApiError);
        if (error instanceof ApiError) {
          expect(error.code).toBe('VALIDATION_ERROR');
          expect(error.message).toBe('유효성 검사 실패');
          expect(error.statusCode).toBe(400);
        }
      }
    });

    it('네트워크 에러 시 NETWORK_ERROR 발생', () => {
      const axiosError = {
        request: {},
      } as any;

      const responseInterceptor = mockAxiosInstance.interceptors.response.use.mock.calls[0][1];

      try {
        responseInterceptor(axiosError);
      } catch (error) {
        expect(error).toBeInstanceOf(ApiError);
        if (error instanceof ApiError) {
          expect(error.code).toBe('NETWORK_ERROR');
          expect(error.message).toBe('네트워크 오류가 발생했습니다.');
        }
      }
    });

    it('알 수 없는 에러 시 UNKNOWN_ERROR 발생', () => {
      const axiosError = {} as any;

      const responseInterceptor = mockAxiosInstance.interceptors.response.use.mock.calls[0][1];

      try {
        responseInterceptor(axiosError);
      } catch (error) {
        expect(error).toBeInstanceOf(ApiError);
        if (error instanceof ApiError) {
          expect(error.code).toBe('UNKNOWN_ERROR');
          expect(error.message).toBe('알 수 없는 오류가 발생했습니다.');
        }
      }
    });
  });

  describe('인증 헤더 추가', () => {
    it('localStorage에 인증 정보가 있을 때 헤더에 추가', () => {
      localStorage.setItem(STORAGE_KEYS.USER_ID, '1');
      localStorage.setItem(STORAGE_KEYS.USER_ROLE, 'USER');

      const requestInterceptor = mockAxiosInstance.interceptors.request.use.mock.calls[0][0];

      const config = {
        headers: {},
      };

      const result = requestInterceptor(config);

      expect(result.headers['X-User-Id']).toBe('1');
      expect(result.headers['X-User-Role']).toBe('USER');
    });

    it('localStorage에 인증 정보가 없을 때 헤더에 추가하지 않음', () => {
      const requestInterceptor = mockAxiosInstance.interceptors.request.use.mock.calls[0][0];

      const config = {
        headers: {},
      };

      const result = requestInterceptor(config);

      expect(result.headers['X-User-Id']).toBeUndefined();
      expect(result.headers['X-User-Role']).toBeUndefined();
    });
  });
});

