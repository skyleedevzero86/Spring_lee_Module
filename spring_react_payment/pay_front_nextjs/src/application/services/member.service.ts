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
} from '@/lib/utils/service-helper';

class MemberService {
  async login(request: LoginRequest): Promise<LoginResponse> {
    return handleServiceCallWithPostProcess(
      () => memberApi.login(request),
      (response) => {
        const { userId, role, token } = response.data;
        apiClient.setAuth(userId, role, token);
        return response;
      },
      'LOGIN_FAILED',
      'ë¡œê·¸?¸ì— ?¤íŒ¨?ˆìŠµ?ˆë‹¤.'
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
      '?Œì›ê°€?…ì— ?¤íŒ¨?ˆìŠµ?ˆë‹¤.'
    );
  }

  async findByEmail(email: string): Promise<FindMemberResponse> {
    return handleServiceCall(
      () => memberApi.findByEmail(email),
      'FIND_MEMBER_FAILED',
      '?Œì› ì¡°íšŒ???¤íŒ¨?ˆìŠµ?ˆë‹¤.'
    );
  }

  async findById(id: number): Promise<FindMemberResponse> {
    return handleServiceCall(
      () => memberApi.findById(id),
      'FIND_MEMBER_FAILED',
      '?Œì› ì¡°íšŒ???¤íŒ¨?ˆìŠµ?ˆë‹¤.'
    );
  }

  async searchByName(name: string): Promise<SearchMemberResponse[]> {
    return handleServiceCall(
      () => memberApi.searchByName(name),
      'SEARCH_MEMBER_FAILED',
      '?Œì› ê²€?‰ì— ?¤íŒ¨?ˆìŠµ?ˆë‹¤.'
    );
  }

  async searchByEmail(email: string): Promise<SearchMemberResponse[]> {
    return handleServiceCall(
      () => memberApi.searchByEmail(email),
      'SEARCH_MEMBER_FAILED',
      '?Œì› ê²€?‰ì— ?¤íŒ¨?ˆìŠµ?ˆë‹¤.'
    );
  }

  async searchAll(): Promise<SearchMemberResponse[]> {
    return handleServiceCall(
      () => memberApi.searchAll(),
      'SEARCH_MEMBER_FAILED',
      '?Œì› ê²€?‰ì— ?¤íŒ¨?ˆìŠµ?ˆë‹¤.'
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
      '?Œì› ê²€?‰ì— ?¤íŒ¨?ˆìŠµ?ˆë‹¤.'
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
      '?Œì› ê²€?‰ì— ?¤íŒ¨?ˆìŠµ?ˆë‹¤.'
    );
  }

  async searchAllPage(
    page: number = 0,
    size: number = 20
  ): Promise<PageResponse<SearchMemberResponse>> {
    return handleServiceCall(
      () => memberApi.searchAllPage(page, size),
      'SEARCH_MEMBER_FAILED',
      '?Œì› ê²€?‰ì— ?¤íŒ¨?ˆìŠµ?ˆë‹¤.'
    );
  }

  async resetPassword(
    request: ResetPasswordRequest
  ): Promise<ResetPasswordResponse> {
    return handleServiceCall(
      () => memberApi.resetPassword(request),
      'RESET_PASSWORD_FAILED',
      'ë¹„ë?ë²ˆí˜¸ ?¬ì„¤?•ì— ?¤íŒ¨?ˆìŠµ?ˆë‹¤.'
    );
  }

  logout(): void {
    apiClient.clearAuth();
  }
}

export const memberService = new MemberService();

