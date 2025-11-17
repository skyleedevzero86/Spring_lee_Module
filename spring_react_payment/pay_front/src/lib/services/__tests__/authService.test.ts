import api from '../../api';
import { authService } from '../authService';
import type { LoginRequest, RegisterRequest } from '@/types/api';

jest.mock('../../api');

describe('authService', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('login', () => {
    it('로그인 요청이 성공하면 응답 데이터를 반환해야 함', async () => {
      // given
      const credentials: LoginRequest = {
        email: 'test@example.com',
        password: 'password123',
      };
      const mockResponse = {
        message: '로그인 성공',
        data: {
          userId: 1,
          email: 'test@example.com',
          name: '테스트 사용자',
          role: 'USER',
          token: 'jwt-token-123',
        },
      };
      (api.post as jest.Mock).mockResolvedValue({ data: mockResponse });

      // when
      const result = await authService.login(credentials);

      // then
      expect(api.post).toHaveBeenCalledWith('/api/v1/users/login', credentials);
      expect(result).toEqual(mockResponse);
    });

    it('로그인 요청이 실패하면 에러를 throw해야 함', async () => {
      // given
      const credentials: LoginRequest = {
        email: 'test@example.com',
        password: 'wrong-password',
      };
      const error = new Error('로그인 실패');
      (api.post as jest.Mock).mockRejectedValue(error);

      // when & then
      await expect(authService.login(credentials)).rejects.toThrow('로그인 실패');
    });
  });

  describe('register', () => {
    it('회원가입 요청이 성공하면 응답 데이터를 반환해야 함', async () => {
      // given
      const registerData: RegisterRequest = {
        email: 'newuser@example.com',
        password: 'password123',
        name: '새 사용자',
      };
      const mockResponse = {
        message: '회원가입 성공',
        data: {
          userId: 2,
          email: 'newuser@example.com',
          name: '새 사용자',
        },
      };
      (api.post as jest.Mock).mockResolvedValue({ data: mockResponse });

      // when
      const result = await authService.register(registerData);

      // then
      expect(api.post).toHaveBeenCalledWith('/api/v1/users/register', registerData);
      expect(result).toEqual(mockResponse);
    });

    it('회원가입 요청이 실패하면 에러를 throw해야 함', async () => {
      // given
      const registerData: RegisterRequest = {
        email: 'existing@example.com',
        password: 'password123',
        name: '기존 사용자',
      };
      const error = new Error('이미 존재하는 이메일입니다');
      (api.post as jest.Mock).mockRejectedValue(error);

      // when & then
      await expect(authService.register(registerData)).rejects.toThrow('이미 존재하는 이메일입니다');
    });
  });
});




