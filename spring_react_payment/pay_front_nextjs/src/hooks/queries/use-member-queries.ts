import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { memberApi } from '@/infrastructure/api/member.api';
import { memberService } from '@/application/services/member.service';
import apiClient from '@/infrastructure/http/api-client';
import type {
  RegisterMemberRequest,
  RegisterMemberResponse,
  FindMemberResponse,
  SearchMemberResponse,
  ResetPasswordRequest,
  ResetPasswordResponse,
  PageResponse,
} from '@/domain/types/member.types';

export const memberQueryKeys = {
  all: ['members'] as const,
  lists: () => [...memberQueryKeys.all, 'list'] as const,
  list: (filters: string) => [...memberQueryKeys.lists(), { filters }] as const,
  details: () => [...memberQueryKeys.all, 'detail'] as const,
  detail: (id: number) => [...memberQueryKeys.details(), id] as const,
  byEmail: (email: string) => [...memberQueryKeys.all, 'email', email] as const,
};

export function useRegisterMember() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (request: RegisterMemberRequest) => memberService.register(request),
    onSuccess: (response: RegisterMemberResponse) => {
      apiClient.setAuth(response.id, response.role);
      queryClient.invalidateQueries({ queryKey: memberQueryKeys.all });
    },
  });
}

export function useFindMemberByEmail(email: string, enabled = true) {
  return useQuery({
    queryKey: memberQueryKeys.byEmail(email),
    queryFn: () => memberApi.findByEmail(email),
    enabled: enabled && !!email,
    staleTime: 5 * 60 * 1000,
  });
}

export function useFindMemberById(id: number, enabled = true) {
  return useQuery({
    queryKey: memberQueryKeys.detail(id),
    queryFn: () => memberApi.findById(id),
    enabled: enabled && !!id,
    staleTime: 5 * 60 * 1000,
  });
}

export function useSearchMembersByName(name: string, enabled = true) {
  return useQuery({
    queryKey: [...memberQueryKeys.lists(), 'name', name],
    queryFn: () => memberApi.searchByName(name),
    enabled: enabled && !!name,
    staleTime: 2 * 60 * 1000,
  });
}

export function useSearchMembersByNamePage(
  name: string,
  page: number = 0,
  size: number = 20,
  enabled = true
) {
  return useQuery({
    queryKey: [...memberQueryKeys.lists(), 'name', name, page, size],
    queryFn: () => memberApi.searchByNamePage(name, page, size),
    enabled: enabled && !!name,
    staleTime: 2 * 60 * 1000,
  });
}

export function useResetPassword() {
  return useMutation({
    mutationFn: (request: ResetPasswordRequest) => memberService.resetPassword(request),
  });
}

