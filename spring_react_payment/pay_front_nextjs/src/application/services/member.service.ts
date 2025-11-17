import { memberApi } from '@/infrastructure/api/member.api';
import apiClient from '@/infrastructure/http/api-client';
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
} from '@/domain/types/member.types';
import {
  handleServiceCall,
  handleServiceCallWithPostProcess,
} from '@/lib/utils';

class MemberService {
  async login(request: LoginRequest): Promise<LoginResponse> {
    return handleServiceCallWithPostProcess(
      () => memberApi.login(request),
      (response) => {
        const { userId, role, token } = response.data;
        const jwtToken = token && token.trim() !== '' ? token : undefined;
        apiClient.setAuth(userId, role, jwtToken);
        return response;
      },
      'LOGIN_FAILED',
      '로그인에 실패했습니다.'
    );
  }

  async register(
    request: RegisterMemberRequest
  ): Promise<RegisterMemberResponse> {
    return handleServiceCallWithPostProcess(
      () => memberApi.register(request),
      (response) => {
        apiClient.setAuth(response.id, response.role);
        return response;
      },
      'REGISTER_FAILED',
      '회원가입에 실패했습니다.'
    );
  }

  async findByEmail(email: string): Promise<FindMemberResponse> {
    return handleServiceCall(
      () => memberApi.findByEmail(email),
      'FIND_MEMBER_FAILED',
      '회원 조회에 실패했습니다.'
    );
  }

  async findById(id: number): Promise<FindMemberResponse> {
    return handleServiceCall(
      () => memberApi.findById(id),
      'FIND_MEMBER_FAILED',
      '회원 조회에 실패했습니다.'
    );
  }

  async searchByName(name: string): Promise<SearchMemberResponse[]> {
    return handleServiceCall(
      () => memberApi.searchByName(name),
      'SEARCH_MEMBER_FAILED',
      '회원 검색에 실패했습니다.'
    );
  }

  async searchByEmail(email: string): Promise<SearchMemberResponse[]> {
    return handleServiceCall(
      () => memberApi.searchByEmail(email),
      'SEARCH_MEMBER_FAILED',
      '회원 검색에 실패했습니다.'
    );
  }

  async searchAll(): Promise<SearchMemberResponse[]> {
    return handleServiceCall(
      () => memberApi.searchAll(),
      'SEARCH_MEMBER_FAILED',
      '회원 검색에 실패했습니다.'
    );
  }

  async searchByNamePage(
    name: string,
    page: number = 0,
    size: number = 20
  ): Promise<PageResponse<SearchMemberResponse>> {
    return handleServiceCall(
      () => memberApi.searchByNamePage(name, page, size),
      'SEARCH_MEMBER_FAILED',
      '회원 검색에 실패했습니다.'
    );
  }

  async searchByEmailPage(
    email: string,
    page: number = 0,
    size: number = 20
  ): Promise<PageResponse<SearchMemberResponse>> {
    return handleServiceCall(
      () => memberApi.searchByEmailPage(email, page, size),
      'SEARCH_MEMBER_FAILED',
      '회원 검색에 실패했습니다.'
    );
  }

  async searchAllPage(
    page: number = 0,
    size: number = 20
  ): Promise<PageResponse<SearchMemberResponse>> {
    return handleServiceCall(
      () => memberApi.searchAllPage(page, size),
      'SEARCH_MEMBER_FAILED',
      '회원 검색에 실패했습니다.'
    );
  }

  async resetPassword(
    request: ResetPasswordRequest
  ): Promise<ResetPasswordResponse> {
    return handleServiceCall(
      () => memberApi.resetPassword(request),
      'RESET_PASSWORD_FAILED',
      '비밀번호 재설정에 실패했습니다.'
    );
  }

  logout(): void {
    apiClient.clearAuth();
  }
}

export const memberService = new MemberService();
