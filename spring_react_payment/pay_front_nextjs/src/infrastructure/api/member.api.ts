import apiClient from '../http/api-client';
import { API_ENDPOINTS } from '@/src/constants/api.constants';
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
} from '@/src/domain/types/member.types';

class MemberApi {
  async login(request: LoginRequest): Promise<LoginResponse> {
    return apiClient.post<LoginResponse>(API_ENDPOINTS.AUTH.LOGIN, request);
  }

  async register(
    request: RegisterMemberRequest
  ): Promise<RegisterMemberResponse> {
    return apiClient.post<RegisterMemberResponse>(API_ENDPOINTS.MEMBERS.REGISTER, request);
  }

  async findByEmail(email: string): Promise<FindMemberResponse> {
    return apiClient.get<FindMemberResponse>(API_ENDPOINTS.MEMBERS.FIND_BY_EMAIL(email));
  }

  async findById(id: number): Promise<FindMemberResponse> {
    return apiClient.get<FindMemberResponse>(API_ENDPOINTS.MEMBERS.FIND_BY_ID(id));
  }

  async searchByName(name: string): Promise<SearchMemberResponse[]> {
    return apiClient.get<SearchMemberResponse[]>(API_ENDPOINTS.MEMBERS.SEARCH_BY_NAME(name));
  }

  async searchByEmail(email: string): Promise<SearchMemberResponse[]> {
    return apiClient.get<SearchMemberResponse[]>(API_ENDPOINTS.MEMBERS.SEARCH_BY_EMAIL(email));
  }

  async searchAll(): Promise<SearchMemberResponse[]> {
    return apiClient.get<SearchMemberResponse[]>(API_ENDPOINTS.MEMBERS.SEARCH_ALL);
  }

  async searchByNamePage(
    name: string,
    page: number = 0,
    size: number = 20
  ): Promise<PageResponse<SearchMemberResponse>> {
    return apiClient.get<PageResponse<SearchMemberResponse>>(
      API_ENDPOINTS.MEMBERS.SEARCH_BY_NAME_PAGE(name, page, size)
    );
  }

  async searchByEmailPage(
    email: string,
    page: number = 0,
    size: number = 20
  ): Promise<PageResponse<SearchMemberResponse>> {
    return apiClient.get<PageResponse<SearchMemberResponse>>(
      API_ENDPOINTS.MEMBERS.SEARCH_BY_EMAIL_PAGE(email, page, size)
    );
  }

  async searchAllPage(
    page: number = 0,
    size: number = 20
  ): Promise<PageResponse<SearchMemberResponse>> {
    return apiClient.get<PageResponse<SearchMemberResponse>>(
      API_ENDPOINTS.MEMBERS.SEARCH_ALL_PAGE(page, size)
    );
  }

  async resetPassword(
    request: ResetPasswordRequest
  ): Promise<ResetPasswordResponse> {
    return apiClient.post<ResetPasswordResponse>(
      API_ENDPOINTS.MEMBERS.RESET_PASSWORD,
      request
    );
  }
}

export const memberApi = new MemberApi();

