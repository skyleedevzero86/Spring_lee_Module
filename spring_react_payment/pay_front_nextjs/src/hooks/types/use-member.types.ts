import type { ApiError } from '@/src/domain/types/error.types';
import type {
  RegisterMemberRequest,
  FindMemberResponse,
  SearchMemberResponse,
  ResetPasswordRequest,
  PageResponse,
} from '@/src/domain/types/member.types';

export interface UseMemberReturn {
  loading: boolean;
  error: ApiError | null;
  register: (request: RegisterMemberRequest) => Promise<unknown>;
  findByEmail: (email: string) => Promise<FindMemberResponse>;
  findById: (id: number) => Promise<FindMemberResponse>;
  searchByName: (name: string) => Promise<SearchMemberResponse[]>;
  searchByEmail: (email: string) => Promise<SearchMemberResponse[]>;
  searchAll: () => Promise<SearchMemberResponse[]>;
  searchByNamePage: (name: string, page?: number, size?: number) => Promise<PageResponse<SearchMemberResponse>>;
  searchByEmailPage: (email: string, page?: number, size?: number) => Promise<PageResponse<SearchMemberResponse>>;
  searchAllPage: (page?: number, size?: number) => Promise<PageResponse<SearchMemberResponse>>;
  resetPassword: (request: ResetPasswordRequest) => Promise<unknown>;
  logout: () => void;
}

