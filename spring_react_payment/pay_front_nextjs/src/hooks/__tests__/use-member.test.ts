import { renderHook, waitFor } from '@testing-library/react';
import { useMember } from '../use-member';
import { memberService } from '@/src/application/services/member.service';
import { useMemberStore } from '@/src/store/member.store';
import { ApiError } from '@/src/domain/types/error.types';
import { MemberRole } from '@/src/domain/types/member.types';

jest.mock('@/src/application/services/member.service');
jest.mock('@/src/store/member.store');

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
      name: '테스트',
    };

    const mockResponse = {
      id: 1,
      email: 'test@example.com',
      name: '테스트',
      role: MemberRole.USER,
    };

    it('회원가입 성공 시 스토어에 회원 정보 저장', async () => {
      mockMemberService.register.mockResolvedValue(mockResponse);

      const { result } = renderHook(() => useMember());

      await result.current.register(mockRequest);

      await waitFor(() => {
        expect(mockMemberService.register).toHaveBeenCalledWith(mockRequest);
        expect(mockSetMember).toHaveBeenCalledWith({
          id: 1,
          email: 'test@example.com',
          name: '테스트',
          role: MemberRole.USER,
        });
      });
    });

    it('회원가입 실패 시 에러 상태 설정', async () => {
      const apiError = new ApiError('EMAIL_ALREADY_EXISTS', 400, '이미 존재하는 이메일입니다.');
      mockMemberService.register.mockRejectedValue(apiError);

      const { result } = renderHook(() => useMember());

      await expect(result.current.register(mockRequest)).rejects.toThrow(apiError);

      await waitFor(() => {
        expect(result.current.error).toBe(apiError);
        expect(result.current.loading).toBe(false);
      });
    });

    it('회원가입 중 loading 상태가 true여야 함', async () => {
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
      name: '테스트',
      role: MemberRole.USER,
    };

    it('이메일로 회원 조회 성공', async () => {
      mockMemberService.findByEmail.mockResolvedValue(mockResponse);

      const { result } = renderHook(() => useMember());

      const response = await result.current.findByEmail(email);

      expect(response).toEqual(mockResponse);
      expect(mockMemberService.findByEmail).toHaveBeenCalledWith(email);
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
          email: 'test@example.com',
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
      mockMemberService.searchByNamePage.mockResolvedValue(mockResponse);

      const { result } = renderHook(() => useMember());

      const response = await result.current.searchByNamePage(name, page, size);

      expect(response).toEqual(mockResponse);
      expect(mockMemberService.searchByNamePage).toHaveBeenCalledWith(name, page, size);
    });

    it('기본 페이지/사이즈 값 사용', async () => {
      mockMemberService.searchByNamePage.mockResolvedValue(mockResponse);

      const { result } = renderHook(() => useMember());

      await result.current.searchByNamePage(name);

      expect(mockMemberService.searchByNamePage).toHaveBeenCalledWith(name, 0, 20);
    });
  });

  describe('logout', () => {
    it('로그아웃 시 인증 정보 제거', () => {
      const { result } = renderHook(() => useMember());

      result.current.logout();

      expect(mockMemberService.logout).toHaveBeenCalled();
      expect(mockClearMember).toHaveBeenCalled();
    });
  });

  describe('에러 처리', () => {
    it('여러 함수 중 하나가 에러 발생 시 error 상태에 반영', async () => {
      const apiError = new ApiError('MEMBER_NOT_FOUND', 404, '회원을 찾을 수 없습니다.');
      mockMemberService.findByEmail.mockRejectedValue(apiError);

      const { result } = renderHook(() => useMember());

      await expect(result.current.findByEmail('test@example.com')).rejects.toThrow();

      await waitFor(() => {
        expect(result.current.error).toBe(apiError);
      });
    });
  });

  describe('loading 상태', () => {
    it('여러 함수 중 하나가 실행 중이면 loading이 true', async () => {
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

