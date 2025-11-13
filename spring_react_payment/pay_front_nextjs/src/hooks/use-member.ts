import { useCallback, useMemo } from 'react';
import { memberService } from '@/src/application/services/member.service';
import { useMemberStore } from '@/src/store/member.store';
import { useAsync } from '@/src/hooks/use-async';
import type {
  RegisterMemberRequest,
  RegisterMemberResponse,
  FindMemberResponse,
  SearchMemberResponse,
  ResetPasswordRequest,
  ResetPasswordResponse,
  PageResponse,
  MemberRole,
} from '@/src/domain/types/member.types';
import { ApiError } from '@/src/domain/types/error.types';

function useAsyncOperation<T, Args extends unknown[]>(
  asyncFn: (...args: Args) => Promise<T>
) {
  const { loading, error, execute } = useAsync(asyncFn);
  return { loading, error, execute };
}

export const useMember = () => {
  const { setMember, clearMember } = useMemberStore();

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

  const loading = useMemo(
    () =>
      registerAsync.loading ||
      findByEmailAsync.loading ||
      findByIdAsync.loading ||
      searchByNameAsync.loading ||
      searchByEmailAsync.loading ||
      searchAllAsync.loading ||
      searchByNamePageAsync.loading ||
      searchByEmailPageAsync.loading ||
      searchAllPageAsync.loading ||
      resetPasswordAsync.loading,
    [
      registerAsync.loading,
      findByEmailAsync.loading,
      findByIdAsync.loading,
      searchByNameAsync.loading,
      searchByEmailAsync.loading,
      searchAllAsync.loading,
      searchByNamePageAsync.loading,
      searchByEmailPageAsync.loading,
      searchAllPageAsync.loading,
      resetPasswordAsync.loading,
    ]
  );

  const error = useMemo(
    () =>
      registerAsync.error ||
      findByEmailAsync.error ||
      findByIdAsync.error ||
      searchByNameAsync.error ||
      searchByEmailAsync.error ||
      searchAllAsync.error ||
      searchByNamePageAsync.error ||
      searchByEmailPageAsync.error ||
      searchAllPageAsync.error ||
      resetPasswordAsync.error,
    [
      registerAsync.error,
      findByEmailAsync.error,
      findByIdAsync.error,
      searchByNameAsync.error,
      searchByEmailAsync.error,
      searchAllAsync.error,
      searchByNamePageAsync.error,
      searchByEmailPageAsync.error,
      searchAllPageAsync.error,
      resetPasswordAsync.error,
    ]
  );

  return {
    loading,
    error,
    register: registerAsync.execute,
    findByEmail: findByEmailAsync.execute,
    findById: findByIdAsync.execute,
    searchByName: searchByNameAsync.execute,
    searchByEmail: searchByEmailAsync.execute,
    searchAll: searchAllAsync.execute,
    searchByNamePage: searchByNamePageAsync.execute,
    searchByEmailPage: searchByEmailPageAsync.execute,
    searchAllPage: searchAllPageAsync.execute,
    resetPassword: resetPasswordAsync.execute,
    logout,
  };
};

