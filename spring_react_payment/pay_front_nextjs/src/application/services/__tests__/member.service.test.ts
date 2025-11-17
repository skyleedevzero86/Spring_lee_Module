import { memberService } from '../member.service';
import { memberApi } from '@/infrastructure/api/member.api';
import apiClient from '@/infrastructure/http/api-client';
import { ApiError } from '@/domain/types/error.types';
import { MemberRole } from '@/domain/types/member.types';

jest.mock('@/infrastructure/api/member.api');
jest.mock('@/infrastructure/http/api-client');

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
      name: '?�스??,
    };

    const mockResponse = {
      id: 1,
      email: 'test@example.com',
      name: '?�스??,
      role: MemberRole.USER,
    };

    it('?�원가???�공 ???�증 ?�보�??�?�해????, async () => {
      mockMemberApi.register.mockResolvedValue(mockResponse);

      const result = await memberService.register(mockRequest);

      expect(result).toEqual(mockResponse);
      expect(mockMemberApi.register).toHaveBeenCalledWith(mockRequest);
      expect(mockApiClient.setAuth).toHaveBeenCalledWith(1, MemberRole.USER);
    });

    it('API ?�러 발생 ??ApiError�?그�?�??�파?�야 ??, async () => {
      const apiError = new ApiError('EMAIL_ALREADY_EXISTS', 400, '?��? 존재?�는 ?�메?�입?�다.');
      mockMemberApi.register.mockRejectedValue(apiError);

      await expect(memberService.register(mockRequest)).rejects.toThrow(apiError);
      expect(mockApiClient.setAuth).not.toHaveBeenCalled();
    });

    it('?�상�?못한 ?�러 발생 ??ApiError�??�핑?�야 ??, async () => {
      const unexpectedError = new Error('Network error');
      mockMemberApi.register.mockRejectedValue(unexpectedError);

      await expect(memberService.register(mockRequest)).rejects.toThrow(ApiError);
      await expect(memberService.register(mockRequest)).rejects.toThrow('?�원가?�에 ?�패?�습?�다.');
    });
  });

  describe('findByEmail', () => {
    const email = 'test@example.com';
    const mockResponse = {
      id: 1,
      email: 'test@example.com',
      name: '?�스??,
      role: MemberRole.USER,
    };

    it('?�메?�로 ?�원 조회 ?�공', async () => {
      mockMemberApi.findByEmail.mockResolvedValue(mockResponse);

      const result = await memberService.findByEmail(email);

      expect(result).toEqual(mockResponse);
      expect(mockMemberApi.findByEmail).toHaveBeenCalledWith(email);
    });

    it('?�원??존재?��? ?�을 ??ApiError 발생', async () => {
      const apiError = new ApiError('MEMBER_NOT_FOUND', 404, '?�원??찾을 ???�습?�다.');
      mockMemberApi.findByEmail.mockRejectedValue(apiError);

      await expect(memberService.findByEmail(email)).rejects.toThrow(apiError);
    });
  });

  describe('findById', () => {
    const id = 1;
    const mockResponse = {
      id: 1,
      email: 'test@example.com',
      name: '?�스??,
      role: MemberRole.USER,
    };

    it('ID�??�원 조회 ?�공', async () => {
      mockMemberApi.findById.mockResolvedValue(mockResponse);

      const result = await memberService.findById(id);

      expect(result).toEqual(mockResponse);
      expect(mockMemberApi.findById).toHaveBeenCalledWith(id);
    });
  });

  describe('searchByName', () => {
    const name = '?�스??;
    const mockResponse = [
      {
        id: 1,
        email: 'test1@example.com',
        name: '?�스??,
        role: MemberRole.USER,
      },
      {
        id: 2,
        email: 'test2@example.com',
        name: '?�스??',
        role: MemberRole.ADMIN,
      },
    ];

    it('?�름?�로 ?�원 검???�공', async () => {
      mockMemberApi.searchByName.mockResolvedValue(mockResponse);

      const result = await memberService.searchByName(name);

      expect(result).toEqual(mockResponse);
      expect(result).toHaveLength(2);
      expect(mockMemberApi.searchByName).toHaveBeenCalledWith(name);
    });

    it('검??결과가 ?�을 ??�?배열 반환', async () => {
      mockMemberApi.searchByName.mockResolvedValue([]);

      const result = await memberService.searchByName(name);

      expect(result).toEqual([]);
      expect(result).toHaveLength(0);
    });
  });

  describe('searchByNamePage', () => {
    const name = '?�스??;
    const page = 0;
    const size = 20;
    const mockResponse = {
      content: [
        {
          id: 1,
          email: 'test1@example.com',
          name: '?�스??,
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

    it('?�이지?�이?�으�??�원 검???�공', async () => {
      mockMemberApi.searchByNamePage.mockResolvedValue(mockResponse);

      const result = await memberService.searchByNamePage(name, page, size);

      expect(result).toEqual(mockResponse);
      expect(result.content).toHaveLength(1);
      expect(mockMemberApi.searchByNamePage).toHaveBeenCalledWith(name, page, size);
    });

    it('기본 ?�이지/?�이�?�??�용', async () => {
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
      message: '비�?번호가 ?�설?�되?�습?�다.',
      email: 'test@example.com',
    };

    it('비�?번호 ?�설???�공', async () => {
      mockMemberApi.resetPassword.mockResolvedValue(mockResponse);

      const result = await memberService.resetPassword(mockRequest);

      expect(result).toEqual(mockResponse);
      expect(mockMemberApi.resetPassword).toHaveBeenCalledWith(mockRequest);
    });

    it('존재?��? ?�는 ?�메?�로 ?�설???�도 ???�러', async () => {
      const apiError = new ApiError('MEMBER_NOT_FOUND', 404, '?�원??찾을 ???�습?�다.');
      mockMemberApi.resetPassword.mockRejectedValue(apiError);

      await expect(memberService.resetPassword(mockRequest)).rejects.toThrow(apiError);
    });
  });

  describe('logout', () => {
    it('?�증 ?�보�??�거?�야 ??, () => {
      memberService.logout();

      expect(mockApiClient.clearAuth).toHaveBeenCalled();
    });
  });
});

