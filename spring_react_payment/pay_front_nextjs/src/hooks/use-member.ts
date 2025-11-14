import { useCallback } from 'react';
import { memberService } from '@/application/services/member.service';
import { useMemberStore } from '@/store/member.store';
import { useAsync } from '@/hooks/use-async';
import type {
  RegisterMemberRequest,
  RegisterMemberResponse,
  FindMemberResponse,
  SearchMemberResponse,
  ResetPasswordRequest,
  ResetPasswordResponse,
  LoginRequest,
  LoginResponse,
  PageResponse,
  MemberRole,
} from '@/domain/types/member.types';
import { ApiError } from '@/domain/types/error.types';

function useAsyncOperation<T, Args extends unknown[]>(
  asyncFn: (...args: Args) => Promise<T>
) {
  const { loading, error, execute } = useAsync(asyncFn);
  return { loading, error, execute };
}

export const useMember = () => {
  const { setMember, clearMember } = useMemberStore();

  const loginAsync = useAsync(
    (request: LoginRequest) => memberService.login(request),
    {
      onSuccess: (response: LoginResponse) => {
        const { userId, email, name, role } = response.data;
        setMember({
          id: userId,
          email,
          name,
          role: role as MemberRole,
        });
      },
    }
  );

  const registerAsync = useAsync(
    (request: RegisterMemberRequest) => memberService.register(request),
    {
      onSuccess: (response: RegisterMemberResponse) => {
        setMember({
          id: response.id,
          email: response.email,
          name: response.name,
          role: response.role,
        });
      },
    }
  );

  const findByEmailAsync = useAsyncOperation((email: string) =>
    memberService.findByEmail(email)
  );

  const findByIdAsync = useAsyncOperation((id: number) =>
    memberService.findById(id)
  );

  const searchByNameAsync = useAsyncOperation((name: string) =>
    memberService.searchByName(name)
  );

  const searchByEmailAsync = useAsyncOperation((email: string) =>
    memberService.searchByEmail(email)
  );

  const searchAllAsync = useAsyncOperation(() => memberService.searchAll());

  const searchByNamePageAsync = useAsyncOperation(
    (name: string, page: number = 0, size: number = 20) =>
      memberService.searchByNamePage(name, page, size)
  );

  const searchByEmailPageAsync = useAsyncOperation(
    (email: string, page: number = 0, size: number = 20) =>
      memberService.searchByEmailPage(email, page, size)
  );

  const searchAllPageAsync = useAsyncOperation(
    (page: number = 0, size: number = 20) =>
      memberService.searchAllPage(page, size)
  );

  const resetPasswordAsync = useAsyncOperation(
    (request: ResetPasswordRequest) => memberService.resetPassword(request)
  );

  const logout = useCallback(() => {
    memberService.logout();
    clearMember();
  }, [clearMember]);

  return {
    login: loginAsync.execute,
    loginLoading: loginAsync.loading,
    loginError: loginAsync.error,
    register: registerAsync.execute,
    registerLoading: registerAsync.loading,
    registerError: registerAsync.error,
    findByEmail: findByEmailAsync.execute,
    findByEmailLoading: findByEmailAsync.loading,
    findByEmailError: findByEmailAsync.error,
    findById: findByIdAsync.execute,
    findByIdLoading: findByIdAsync.loading,
    findByIdError: findByIdAsync.error,
    searchByName: searchByNameAsync.execute,
    searchByNameLoading: searchByNameAsync.loading,
    searchByNameError: searchByNameAsync.error,
    searchByEmail: searchByEmailAsync.execute,
    searchByEmailLoading: searchByEmailAsync.loading,
    searchByEmailError: searchByEmailAsync.error,
    searchAll: searchAllAsync.execute,
    searchAllLoading: searchAllAsync.loading,
    searchAllError: searchAllAsync.error,
    searchByNamePage: searchByNamePageAsync.execute,
    searchByNamePageLoading: searchByNamePageAsync.loading,
    searchByNamePageError: searchByNamePageAsync.error,
    searchByEmailPage: searchByEmailPageAsync.execute,
    searchByEmailPageLoading: searchByEmailPageAsync.loading,
    searchByEmailPageError: searchByEmailPageAsync.error,
    searchAllPage: searchAllPageAsync.execute,
    searchAllPageLoading: searchAllPageAsync.loading,
    searchAllPageError: searchAllPageAsync.error,
    resetPassword: resetPasswordAsync.execute,
    resetPasswordLoading: resetPasswordAsync.loading,
    resetPasswordError: resetPasswordAsync.error,
    logout,
  };
};

