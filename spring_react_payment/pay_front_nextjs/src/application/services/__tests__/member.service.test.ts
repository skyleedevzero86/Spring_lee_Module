import { memberService } from '../member.service';
import { memberApi } from '@/src/infrastructure/api/member.api';
import apiClient from '@/src/infrastructure/http/api-client';
import { ApiError } from '@/src/domain/types/error.types';
import { MemberRole } from '@/src/domain/types/member.types';

jest.mock('@/src/infrastructure/api/member.api');
jest.mock('@/src/infrastructure/http/api-client');

const mockMemberApi = memberApi as jest.Mocked<typeof memberApi>;
const mockApiClient = apiClient as jest.Mocked<typeof apiClient>;

describe('MemberService', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    localStorage.clear();
  });

  describe('register', () => {
    const mockRequest = {
      email: 'test@example.com',
      password: 'password123',
      name: '테스트',
    };

    const mockResponse = {
      id: 1,
      email: 'test@example.com',
      name: '테스트',
      role: MemberRole.USER,
    };

    it('회원가입 성공 시 인증 정보를 저장해야 함', async () => {
      mockMemberApi.register.mockResolvedValue(mockResponse);

      const result = await memberService.register(mockRequest);

      expect(result).toEqual(mockResponse);
      expect(mockMemberApi.register).toHaveBeenCalledWith(mockRequest);
      expect(mockApiClient.setAuth).toHaveBeenCalledWith(1, MemberRole.USER);
    });

    it('API 에러 발생 시 ApiError를 그대로 전파해야 함', async () => {
      const apiError = new ApiError('EMAIL_ALREADY_EXISTS', 400, '이미 존재하는 이메일입니다.');
      mockMemberApi.register.mockRejectedValue(apiError);

      await expect(memberService.register(mockRequest)).rejects.toThrow(apiError);
      expect(mockApiClient.setAuth).not.toHaveBeenCalled();
    });

    it('예상치 못한 에러 발생 시 ApiError로 래핑해야 함', async () => {
      const unexpectedError = new Error('Network error');
      mockMemberApi.register.mockRejectedValue(unexpectedError);

      await expect(memberService.register(mockRequest)).rejects.toThrow(ApiError);
      await expect(memberService.register(mockRequest)).rejects.toThrow('회원가입에 실패했습니다.');
    });
  });

  describe('findByEmail', () => {
    const email = 'test@example.com';
    const mockResponse = {
      id: 1,
      email: 'test@example.com',
      name: '테스트',
      role: MemberRole.USER,
    };

    it('이메일로 회원 조회 성공', async () => {
      mockMemberApi.findByEmail.mockResolvedValue(mockResponse);

      const result = await memberService.findByEmail(email);

      expect(result).toEqual(mockResponse);
      expect(mockMemberApi.findByEmail).toHaveBeenCalledWith(email);
    });

    it('회원이 존재하지 않을 때 ApiError 발생', async () => {
      const apiError = new ApiError('MEMBER_NOT_FOUND', 404, '회원을 찾을 수 없습니다.');
      mockMemberApi.findByEmail.mockRejectedValue(apiError);

      await expect(memberService.findByEmail(email)).rejects.toThrow(apiError);
    });
  });

  describe('findById', () => {
    const id = 1;
    const mockResponse = {
      id: 1,
      email: 'test@example.com',
      name: '테스트',
      role: MemberRole.USER,
    };

    it('ID로 회원 조회 성공', async () => {
      mockMemberApi.findById.mockResolvedValue(mockResponse);

      const result = await memberService.findById(id);

      expect(result).toEqual(mockResponse);
      expect(mockMemberApi.findById).toHaveBeenCalledWith(id);
    });
  });

  describe('searchByName', () => {
    const name = '테스트';
    const mockResponse = [
      {
        id: 1,
        email: 'test1@example.com',
        name: '테스트',
        role: MemberRole.USER,
      },
      {
        id: 2,
        email: 'test2@example.com',
        name: '테스트2',
        role: MemberRole.ADMIN,
      },
    ];

    it('이름으로 회원 검색 성공', async () => {
      mockMemberApi.searchByName.mockResolvedValue(mockResponse);

      const result = await memberService.searchByName(name);

      expect(result).toEqual(mockResponse);
      expect(result).toHaveLength(2);
      expect(mockMemberApi.searchByName).toHaveBeenCalledWith(name);
    });

    it('검색 결과가 없을 때 빈 배열 반환', async () => {
      mockMemberApi.searchByName.mockResolvedValue([]);

      const result = await memberService.searchByName(name);

      expect(result).toEqual([]);
      expect(result).toHaveLength(0);
    });
  });

  describe('searchByNamePage', () => {
    const name = '테스트';
    const page = 0;
    const size = 20;
    const mockResponse = {
      content: [
        {
          id: 1,
          email: 'test1@example.com',
          name: '테스트',
          role: MemberRole.USER,
        },
      ],
      page: 0,
      size: 20,
      totalElements: 1,
      totalPages: 1,
      hasNext: false,
      hasPrevious: false,
    };

    it('페이지네이션으로 회원 검색 성공', async () => {
      mockMemberApi.searchByNamePage.mockResolvedValue(mockResponse);

      const result = await memberService.searchByNamePage(name, page, size);

      expect(result).toEqual(mockResponse);
      expect(result.content).toHaveLength(1);
      expect(mockMemberApi.searchByNamePage).toHaveBeenCalledWith(name, page, size);
    });

    it('기본 페이지/사이즈 값 사용', async () => {
      mockMemberApi.searchByNamePage.mockResolvedValue(mockResponse);

      await memberService.searchByNamePage(name);

      expect(mockMemberApi.searchByNamePage).toHaveBeenCalledWith(name, 0, 20);
    });
  });

  describe('resetPassword', () => {
    const mockRequest = {
      email: 'test@example.com',
      newPassword: 'newPassword123',
    };

    const mockResponse = {
      message: '비밀번호가 재설정되었습니다.',
      email: 'test@example.com',
    };

    it('비밀번호 재설정 성공', async () => {
      mockMemberApi.resetPassword.mockResolvedValue(mockResponse);

      const result = await memberService.resetPassword(mockRequest);

      expect(result).toEqual(mockResponse);
      expect(mockMemberApi.resetPassword).toHaveBeenCalledWith(mockRequest);
    });

    it('존재하지 않는 이메일로 재설정 시도 시 에러', async () => {
      const apiError = new ApiError('MEMBER_NOT_FOUND', 404, '회원을 찾을 수 없습니다.');
      mockMemberApi.resetPassword.mockRejectedValue(apiError);

      await expect(memberService.resetPassword(mockRequest)).rejects.toThrow(apiError);
    });
  });

  describe('logout', () => {
    it('인증 정보를 제거해야 함', () => {
      memberService.logout();

      expect(mockApiClient.clearAuth).toHaveBeenCalled();
    });
  });
});

