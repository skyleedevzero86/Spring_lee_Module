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
    it('?¸ì¦ ?•ë³´ë¥?localStorage???€?¥í•´????, () => {
      const userId = 1;
      const role = 'USER';

      apiClient.setAuth(userId, role);

      expect(localStorage.getItem(STORAGE_KEYS.USER_ID)).toBe('1');
      expect(localStorage.getItem(STORAGE_KEYS.USER_ROLE)).toBe(role);
    });
  });

  describe('clearAuth', () => {
    it('?¸ì¦ ?•ë³´ë¥?localStorage?ì„œ ?œê±°?´ì•¼ ??, () => {
      localStorage.setItem(STORAGE_KEYS.USER_ID, '1');
      localStorage.setItem(STORAGE_KEYS.USER_ROLE, 'USER');

      apiClient.clearAuth();

      expect(localStorage.getItem(STORAGE_KEYS.USER_ID)).toBeNull();
      expect(localStorage.getItem(STORAGE_KEYS.USER_ROLE)).toBeNull();
    });
  });

  describe('get', () => {
    it('GET ?”ì²­ ?±ê³µ', async () => {
      const mockData = { id: 1, name: '?ŒìŠ¤?? };
      mockAxiosInstance.get.mockResolvedValue({ data: mockData });

      const result = await apiClient.get('/api/test');

      expect(result).toEqual(mockData);
      expect(mockAxiosInstance.get).toHaveBeenCalledWith('/api/test', undefined);
    });
  });

  describe('post', () => {
    it('POST ?”ì²­ ?±ê³µ', async () => {
      const requestData = { name: '?ŒìŠ¤?? };
      const mockData = { id: 1, name: '?ŒìŠ¤?? };
      mockAxiosInstance.post.mockResolvedValue({ data: mockData });

      const result = await apiClient.post('/api/test', requestData);

      expect(result).toEqual(mockData);
      expect(mockAxiosInstance.post).toHaveBeenCalledWith('/api/test', requestData, undefined);
    });
  });

  describe('?ëŸ¬ ì²˜ë¦¬', () => {
    it('?œë²„ ?ëŸ¬ ?‘ë‹µ ??ApiErrorë¡?ë³€??, () => {
      const errorResponse = {
        code: 'VALIDATION_ERROR',
        message: '? íš¨??ê²€???¤íŒ¨',
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
          expect(error.message).toBe('? íš¨??ê²€???¤íŒ¨');
          expect(error.statusCode).toBe(400);
        }
      }
    });

    it('?¤íŠ¸?Œí¬ ?ëŸ¬ ??NETWORK_ERROR ë°œìƒ', () => {
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
          expect(error.message).toBe('?¤íŠ¸?Œí¬ ?¤ë¥˜ê°€ ë°œìƒ?ˆìŠµ?ˆë‹¤.');
        }
      }
    });

    it('?????†ëŠ” ?ëŸ¬ ??UNKNOWN_ERROR ë°œìƒ', () => {
      const axiosError = {} as any;

      const responseInterceptor = mockAxiosInstance.interceptors.response.use.mock.calls[0][1];

      try {
        responseInterceptor(axiosError);
      } catch (error) {
        expect(error).toBeInstanceOf(ApiError);
        if (error instanceof ApiError) {
          expect(error.code).toBe('UNKNOWN_ERROR');
          expect(error.message).toBe('?????†ëŠ” ?¤ë¥˜ê°€ ë°œìƒ?ˆìŠµ?ˆë‹¤.');
        }
      }
    });
  });

  describe('?¸ì¦ ?¤ë” ì¶”ê?', () => {
    it('localStorage???¸ì¦ ?•ë³´ê°€ ?ˆì„ ???¤ë”??ì¶”ê?', () => {
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

    it('localStorage???¸ì¦ ?•ë³´ê°€ ?†ì„ ???¤ë”??ì¶”ê??˜ì? ?ŠìŒ', () => {
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

