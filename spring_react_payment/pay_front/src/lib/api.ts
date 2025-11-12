import axios, { AxiosError } from 'axios';
import { redirectToLogin } from './navigation';

const baseURL = import.meta.env.VITE_API_BASE_URL || (import.meta.env.DEV ? '' : 'http://localhost:9000');

const api = axios.create({
  baseURL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
  timeout: 30000,
  timeoutErrorMessage: '요청 시간이 초과되었습니다. 잠시 후 다시 시도해주세요.',
});

api.interceptors.request.use(
  (config) => {
    try {
      const token = localStorage.getItem('token');
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
        console.debug('토큰이 요청 헤더에 추가되었습니다:', config.url);
      } else {
        console.warn('토큰이 없습니다. 요청 URL:', config.url);
      }
    } catch (error) {
      console.error('토큰 조회 중 오류 발생:', error);
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

api.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    if (error.response?.status === 401 || error.response?.status === 403) {
      console.warn('인증 실패 (401/403). 토큰을 제거하고 로그인 페이지로 이동합니다.');
      try {
        localStorage.removeItem('token');
        localStorage.removeItem('auth-storage');
      } catch (storageError) {
        console.error('스토리지 정리 중 오류 발생:', storageError);
      }
      if (window.location.pathname !== '/login') {
        redirectToLogin();
      }
    }
    if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
      return Promise.reject(new Error('요청 시간이 초과되었습니다. 네트워크 연결을 확인해주세요.'));
    }
    if (!error.response && error.request) {
      return Promise.reject(new Error('서버에 연결할 수 없습니다. 네트워크 연결을 확인해주세요.'));
    }
    return Promise.reject(error);
  }
);

export default api;
