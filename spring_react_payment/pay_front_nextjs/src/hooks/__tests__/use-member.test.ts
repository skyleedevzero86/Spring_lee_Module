import { renderHook, waitFor } from '@testing-library/react';
import { useMember } from '../use-member';
import { memberService } from '@/application/services/member.service';
import { useMemberStore } from '@/store/member.store';
import { ApiError } from '@/domain/types/error.types';
import { MemberRole } from '@/domain/types/member.types';

jest.mock('@/application/services/member.service');
jest.mock('@/store/member.store');

const mockMemberService = memberService as jest.Mocked<typeof memberService>;
const mockUseMemberStore = useMemberStore as jest.MockedFunction<typeof useMemberStore>;

describe('useMember', () => {
  const mockSetMember = jest.fn();
  const mockClearMember = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    mockUseMemberStore.mockReturnValue({
      member: null,
      isAuthenticated: false,
      setMember: mockSetMember,
      clearMember: mockClearMember,
      isAdmin: jest.fn(() => false),
      isUser: jest.fn(() => false),
    });
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

    it('?Œì›ê°€???±ê³µ ???¤í† ?´ì— ?Œì› ?•ë³´ ?€??, async () => {
      mockMemberService.register.mockResolvedValue(mockResponse);

      const { result } = renderHook(() => useMember());

      await result.current.register(mockRequest);

      await waitFor(() => {
        expect(mockMemberService.register).toHaveBeenCalledWith(mockRequest);
        expect(mockSetMember).toHaveBeenCalledWith({
          id: 1,
          email: 'test@example.com',
          name: '?ŒìŠ¤??,
          role: MemberRole.USER,
        });
      });
    });

    it('?Œì›ê°€???¤íŒ¨ ???ëŸ¬ ?íƒœ ?¤ì •', async () => {
      const apiError = new ApiError('EMAIL_ALREADY_EXISTS', 400, '?´ë? ì¡´ìž¬?˜ëŠ” ?´ë©”?¼ìž…?ˆë‹¤.');
      mockMemberService.register.mockRejectedValue(apiError);

      const { result } = renderHook(() => useMember());

      await expect(result.current.register(mockRequest)).rejects.toThrow(apiError);

      await waitFor(() => {
        expect(result.current.error).toBe(apiError);
        expect(result.current.loading).toBe(false);
      });
    });

    it('?Œì›ê°€??ì¤?loading ?íƒœê°€ true?¬ì•¼ ??, async () => {
      mockMemberService.register.mockImplementation(
        () => new Promise((resolve) => setTimeout(() => resolve(mockResponse), 100))
      );

      const { result } = renderHook(() => useMember());

      const promise = result.current.register(mockRequest);

      expect(result.current.loading).toBe(true);

      await promise;
      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });
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
      mockMemberService.findByEmail.mockResolvedValue(mockResponse);

      const { result } = renderHook(() => useMember());

      const response = await result.current.findByEmail(email);

      expect(response).toEqual(mockResponse);
      expect(mockMemberService.findByEmail).toHaveBeenCalledWith(email);
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
          email: 'test@example.com',
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
      mockMemberService.searchByNamePage.mockResolvedValue(mockResponse);

      const { result } = renderHook(() => useMember());

      const response = await result.current.searchByNamePage(name, page, size);

      expect(response).toEqual(mockResponse);
      expect(mockMemberService.searchByNamePage).toHaveBeenCalledWith(name, page, size);
    });

    it('ê¸°ë³¸ ?˜ì´ì§€/?¬ì´ì¦?ê°??¬ìš©', async () => {
      mockMemberService.searchByNamePage.mockResolvedValue(mockResponse);

      const { result } = renderHook(() => useMember());

      await result.current.searchByNamePage(name);

      expect(mockMemberService.searchByNamePage).toHaveBeenCalledWith(name, 0, 20);
    });
  });

  describe('logout', () => {
    it('ë¡œê·¸?„ì›ƒ ???¸ì¦ ?•ë³´ ?œê±°', () => {
      const { result } = renderHook(() => useMember());

      result.current.logout();

      expect(mockMemberService.logout).toHaveBeenCalled();
      expect(mockClearMember).toHaveBeenCalled();
    });
  });

  describe('?ëŸ¬ ì²˜ë¦¬', () => {
    it('?¬ëŸ¬ ?¨ìˆ˜ ì¤??˜ë‚˜ê°€ ?ëŸ¬ ë°œìƒ ??error ?íƒœ??ë°˜ì˜', async () => {
      const apiError = new ApiError('MEMBER_NOT_FOUND', 404, '?Œì›??ì°¾ì„ ???†ìŠµ?ˆë‹¤.');
      mockMemberService.findByEmail.mockRejectedValue(apiError);

      const { result } = renderHook(() => useMember());

      await expect(result.current.findByEmail('test@example.com')).rejects.toThrow();

      await waitFor(() => {
        expect(result.current.error).toBe(apiError);
      });
    });
  });

  describe('loading ?íƒœ', () => {
    it('?¬ëŸ¬ ?¨ìˆ˜ ì¤??˜ë‚˜ê°€ ?¤í–‰ ì¤‘ì´ë©?loading??true', async () => {
      mockMemberService.findByEmail.mockImplementation(
        () => new Promise((resolve) => setTimeout(() => resolve({} as any), 100))
      );

      const { result } = renderHook(() => useMember());

      const promise = result.current.findByEmail('test@example.com');

      expect(result.current.loading).toBe(true);

      await promise;
      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });
    });
  });
});

