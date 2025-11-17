import apiClient from '../api-client';
import { ApiError } from '@/domain/types/error.types';
import { STORAGE_KEYS } from '@/constants/api.constants';
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
    it('?�증 ?�보�?localStorage???�?�해????, () => {
      const userId = 1;
      const role = 'USER';

      apiClient.setAuth(userId, role);

      expect(localStorage.getItem(STORAGE_KEYS.USER_ID)).toBe('1');
      expect(localStorage.getItem(STORAGE_KEYS.USER_ROLE)).toBe(role);
    });
  });

  describe('clearAuth', () => {
    it('?�증 ?�보�?localStorage?�서 ?�거?�야 ??, () => {
      localStorage.setItem(STORAGE_KEYS.USER_ID, '1');
      localStorage.setItem(STORAGE_KEYS.USER_ROLE, 'USER');

      apiClient.clearAuth();

      expect(localStorage.getItem(STORAGE_KEYS.USER_ID)).toBeNull();
      expect(localStorage.getItem(STORAGE_KEYS.USER_ROLE)).toBeNull();
    });
  });

  describe('get', () => {
    it('GET ?�청 ?�공', async () => {
      const mockData = { id: 1, name: '?�스?? };
      mockAxiosInstance.get.mockResolvedValue({ data: mockData });

      const result = await apiClient.get('/api/test');

      expect(result).toEqual(mockData);
      expect(mockAxiosInstance.get).toHaveBeenCalledWith('/api/test', undefined);
    });
  });

  describe('post', () => {
    it('POST ?�청 ?�공', async () => {
      const requestData = { name: '?�스?? };
      const mockData = { id: 1, name: '?�스?? };
      mockAxiosInstance.post.mockResolvedValue({ data: mockData });

      const result = await apiClient.post('/api/test', requestData);

      expect(result).toEqual(mockData);
      expect(mockAxiosInstance.post).toHaveBeenCalledWith('/api/test', requestData, undefined);
    });
  });

  describe('?�러 처리', () => {
    it('?�버 ?�러 ?�답 ??ApiError�?변??, () => {
      const errorResponse = {
        code: 'VALIDATION_ERROR',
        message: '?�효??검???�패',
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
          expect(error.message).toBe('?�효??검???�패');
          expect(error.statusCode).toBe(400);
        }
      }
    });

    it('?�트?�크 ?�러 ??NETWORK_ERROR 발생', () => {
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
          expect(error.message).toBe('?�트?�크 ?�류가 발생?�습?�다.');
        }
      }
    });

    it('?????�는 ?�러 ??UNKNOWN_ERROR 발생', () => {
      const axiosError = {} as any;

      const responseInterceptor = mockAxiosInstance.interceptors.response.use.mock.calls[0][1];

      try {
        responseInterceptor(axiosError);
      } catch (error) {
        expect(error).toBeInstanceOf(ApiError);
        if (error instanceof ApiError) {
          expect(error.code).toBe('UNKNOWN_ERROR');
          expect(error.message).toBe('?????�는 ?�류가 발생?�습?�다.');
        }
      }
    });
  });

  describe('?�증 ?�더 추�?', () => {
    it('localStorage???�증 ?�보가 ?�을 ???�더??추�?', () => {
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

    it('localStorage???�증 ?�보가 ?�을 ???�더??추�??��? ?�음', () => {
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

