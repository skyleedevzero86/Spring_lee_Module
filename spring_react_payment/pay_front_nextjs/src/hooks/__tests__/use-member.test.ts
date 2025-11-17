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
      name: '?�스??,
    };

    const mockResponse = {
      id: 1,
      email: 'test@example.com',
      name: '?�스??,
      role: MemberRole.USER,
    };

    it('?�원가???�공 ???�토?�에 ?�원 ?�보 ?�??, async () => {
      mockMemberService.register.mockResolvedValue(mockResponse);

      const { result } = renderHook(() => useMember());

      await result.current.register(mockRequest);

      await waitFor(() => {
        expect(mockMemberService.register).toHaveBeenCalledWith(mockRequest);
        expect(mockSetMember).toHaveBeenCalledWith({
          id: 1,
          email: 'test@example.com',
          name: '?�스??,
          role: MemberRole.USER,
        });
      });
    });

    it('?�원가???�패 ???�러 ?�태 ?�정', async () => {
      const apiError = new ApiError('EMAIL_ALREADY_EXISTS', 400, '?��? 존재?�는 ?�메?�입?�다.');
      mockMemberService.register.mockRejectedValue(apiError);

      const { result } = renderHook(() => useMember());

      await expect(result.current.register(mockRequest)).rejects.toThrow(apiError);

      await waitFor(() => {
        expect(result.current.error).toBe(apiError);
        expect(result.current.loading).toBe(false);
      });
    });

    it('?�원가??�?loading ?�태가 true?�야 ??, async () => {
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
      name: '?�스??,
      role: MemberRole.USER,
    };

    it('?�메?�로 ?�원 조회 ?�공', async () => {
      mockMemberService.findByEmail.mockResolvedValue(mockResponse);

      const { result } = renderHook(() => useMember());

      const response = await result.current.findByEmail(email);

      expect(response).toEqual(mockResponse);
      expect(mockMemberService.findByEmail).toHaveBeenCalledWith(email);
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
          email: 'test@example.com',
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
      mockMemberService.searchByNamePage.mockResolvedValue(mockResponse);

      const { result } = renderHook(() => useMember());

      const response = await result.current.searchByNamePage(name, page, size);

      expect(response).toEqual(mockResponse);
      expect(mockMemberService.searchByNamePage).toHaveBeenCalledWith(name, page, size);
    });

    it('기본 ?�이지/?�이�?�??�용', async () => {
      mockMemberService.searchByNamePage.mockResolvedValue(mockResponse);

      const { result } = renderHook(() => useMember());

      await result.current.searchByNamePage(name);

      expect(mockMemberService.searchByNamePage).toHaveBeenCalledWith(name, 0, 20);
    });
  });

  describe('logout', () => {
    it('로그?�웃 ???�증 ?�보 ?�거', () => {
      const { result } = renderHook(() => useMember());

      result.current.logout();

      expect(mockMemberService.logout).toHaveBeenCalled();
      expect(mockClearMember).toHaveBeenCalled();
    });
  });

  describe('?�러 처리', () => {
    it('?�러 ?�수 �??�나가 ?�러 발생 ??error ?�태??반영', async () => {
      const apiError = new ApiError('MEMBER_NOT_FOUND', 404, '?�원??찾을 ???�습?�다.');
      mockMemberService.findByEmail.mockRejectedValue(apiError);

      const { result } = renderHook(() => useMember());

      await expect(result.current.findByEmail('test@example.com')).rejects.toThrow();

      await waitFor(() => {
        expect(result.current.error).toBe(apiError);
      });
    });
  });

  describe('loading ?�태', () => {
    it('?�러 ?�수 �??�나가 ?�행 중이�?loading??true', async () => {
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

