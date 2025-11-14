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
      name: '?ŒìŠ¤??,
    };

    const mockResponse = {
      id: 1,
      email: 'test@example.com',
      name: '?ŒìŠ¤??,
      role: MemberRole.USER,
    };

    it('?Œì›ê°€???±ê³µ ???¸ì¦ ?•ë³´ë¥??€?¥í•´????, async () => {
      mockMemberApi.register.mockResolvedValue(mockResponse);

      const result = await memberService.register(mockRequest);

      expect(result).toEqual(mockResponse);
      expect(mockMemberApi.register).toHaveBeenCalledWith(mockRequest);
      expect(mockApiClient.setAuth).toHaveBeenCalledWith(1, MemberRole.USER);
    });

    it('API ?ëŸ¬ ë°œìƒ ??ApiErrorë¥?ê·¸ë?ë¡??„íŒŒ?´ì•¼ ??, async () => {
      const apiError = new ApiError('EMAIL_ALREADY_EXISTS', 400, '?´ë? ì¡´ìž¬?˜ëŠ” ?´ë©”?¼ìž…?ˆë‹¤.');
      mockMemberApi.register.mockRejectedValue(apiError);

      await expect(memberService.register(mockRequest)).rejects.toThrow(apiError);
      expect(mockApiClient.setAuth).not.toHaveBeenCalled();
    });

    it('?ˆìƒì¹?ëª»í•œ ?ëŸ¬ ë°œìƒ ??ApiErrorë¡??˜í•‘?´ì•¼ ??, async () => {
      const unexpectedError = new Error('Network error');
      mockMemberApi.register.mockRejectedValue(unexpectedError);

      await expect(memberService.register(mockRequest)).rejects.toThrow(ApiError);
      await expect(memberService.register(mockRequest)).rejects.toThrow('?Œì›ê°€?…ì— ?¤íŒ¨?ˆìŠµ?ˆë‹¤.');
    });
  });

  describe('findByEmail', () => {
    const email = 'test@example.com';
    const mockResponse = {
      id: 1,
      email: 'test@example.com',
      name: '?ŒìŠ¤??,
      role: MemberRole.USER,
    };

    it('?´ë©”?¼ë¡œ ?Œì› ì¡°íšŒ ?±ê³µ', async () => {
      mockMemberApi.findByEmail.mockResolvedValue(mockResponse);

      const result = await memberService.findByEmail(email);

      expect(result).toEqual(mockResponse);
      expect(mockMemberApi.findByEmail).toHaveBeenCalledWith(email);
    });

    it('?Œì›??ì¡´ìž¬?˜ì? ?Šì„ ??ApiError ë°œìƒ', async () => {
      const apiError = new ApiError('MEMBER_NOT_FOUND', 404, '?Œì›??ì°¾ì„ ???†ìŠµ?ˆë‹¤.');
      mockMemberApi.findByEmail.mockRejectedValue(apiError);

      await expect(memberService.findByEmail(email)).rejects.toThrow(apiError);
    });
  });

  describe('findById', () => {
    const id = 1;
    const mockResponse = {
      id: 1,
      email: 'test@example.com',
      name: '?ŒìŠ¤??,
      role: MemberRole.USER,
    };

    it('IDë¡??Œì› ì¡°íšŒ ?±ê³µ', async () => {
      mockMemberApi.findById.mockResolvedValue(mockResponse);

      const result = await memberService.findById(id);

      expect(result).toEqual(mockResponse);
      expect(mockMemberApi.findById).toHaveBeenCalledWith(id);
    });
  });

  describe('searchByName', () => {
    const name = '?ŒìŠ¤??;
    const mockResponse = [
      {
        id: 1,
        email: 'test1@example.com',
        name: '?ŒìŠ¤??,
        role: MemberRole.USER,
      },
      {
        id: 2,
        email: 'test2@example.com',
        name: '?ŒìŠ¤??',
        role: MemberRole.ADMIN,
      },
    ];

    it('?´ë¦„?¼ë¡œ ?Œì› ê²€???±ê³µ', async () => {
      mockMemberApi.searchByName.mockResolvedValue(mockResponse);

      const result = await memberService.searchByName(name);

      expect(result).toEqual(mockResponse);
      expect(result).toHaveLength(2);
      expect(mockMemberApi.searchByName).toHaveBeenCalledWith(name);
    });

    it('ê²€??ê²°ê³¼ê°€ ?†ì„ ??ë¹?ë°°ì—´ ë°˜í™˜', async () => {
      mockMemberApi.searchByName.mockResolvedValue([]);

      const result = await memberService.searchByName(name);

      expect(result).toEqual([]);
      expect(result).toHaveLength(0);
    });
  });

  describe('searchByNamePage', () => {
    const name = '?ŒìŠ¤??;
    const page = 0;
    const size = 20;
    const mockResponse = {
      content: [
        {
          id: 1,
          email: 'test1@example.com',
          name: '?ŒìŠ¤??,
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

    it('?˜ì´ì§€?¤ì´?˜ìœ¼ë¡??Œì› ê²€???±ê³µ', async () => {
      mockMemberApi.searchByNamePage.mockResolvedValue(mockResponse);

      const result = await memberService.searchByNamePage(name, page, size);

      expect(result).toEqual(mockResponse);
      expect(result.content).toHaveLength(1);
      expect(mockMemberApi.searchByNamePage).toHaveBeenCalledWith(name, page, size);
    });

    it('ê¸°ë³¸ ?˜ì´ì§€/?¬ì´ì¦?ê°??¬ìš©', async () => {
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
      message: 'ë¹„ë?ë²ˆí˜¸ê°€ ?¬ì„¤?•ë˜?ˆìŠµ?ˆë‹¤.',
      email: 'test@example.com',
    };

    it('ë¹„ë?ë²ˆí˜¸ ?¬ì„¤???±ê³µ', async () => {
      mockMemberApi.resetPassword.mockResolvedValue(mockResponse);

      const result = await memberService.resetPassword(mockRequest);

      expect(result).toEqual(mockResponse);
      expect(mockMemberApi.resetPassword).toHaveBeenCalledWith(mockRequest);
    });

    it('ì¡´ìž¬?˜ì? ?ŠëŠ” ?´ë©”?¼ë¡œ ?¬ì„¤???œë„ ???ëŸ¬', async () => {
      const apiError = new ApiError('MEMBER_NOT_FOUND', 404, '?Œì›??ì°¾ì„ ???†ìŠµ?ˆë‹¤.');
      mockMemberApi.resetPassword.mockRejectedValue(apiError);

      await expect(memberService.resetPassword(mockRequest)).rejects.toThrow(apiError);
    });
  });

  describe('logout', () => {
    it('?¸ì¦ ?•ë³´ë¥??œê±°?´ì•¼ ??, () => {
      memberService.logout();

      expect(mockApiClient.clearAuth).toHaveBeenCalled();
    });
  });
});

